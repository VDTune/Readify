package com.example.readify.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.readify.MyApplication
import com.example.readify.R
import com.example.readify.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityProfileEditBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    //uri của ảnh
    private var imageUri:Uri ?= null

    //show tien trinh
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi một lát")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        //xử lí nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lí chọn ảnh
        binding.profileIv.setOnClickListener{
            showImageAttachMenu()
        }

        //xử lí nút submit
        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private fun validateData() {
        //lay du lieu
        name = binding.nameEt.text.toString().trim()
        if(name.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
        }
        else{
            if(imageUri == null){
                //cập nhật không cần ảnh
                updateProfile("")

            }else{
                //cập nhật cùng ảnh
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Tải lên ảnh...")
        progressDialog.show()

        //đường dẫn và tên ảnh
        val filePathAndName = "ProfileImage/"+firebaseAuth.uid

        //ref storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot ->
                progressDialog.dismiss()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)

            }
            .addOnFailureListener{e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Tải lên thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Cập nhật hồ sơ...")

        //setup thông tin cho db
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri != null ){
            hashMap["profileImage"] = uploadedImageUrl
        }

        //cập nhật dữ liệu vào db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Cập nhật thành công!",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Cập nhật thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy thông tin người dùng
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //đổi timestamp
                    val formattedDate = MyApplication.formatTimestamp(timestamp.toLong())


                    //set data
                    binding.nameEt.setText(name)


                    //set ảnh
                    try {
                        Glide.with(this@ProfileEditActivity).load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu(){
        //hiển thị popup menu chọn ảnh

        //setu popup menu
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0 , "Máy ảnh")
        popupMenu.menu.add(Menu.NONE, 1, 1 , "Thư viện")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item ->
            //lấy id item được chọn
            val id = item.itemId
            if (id == 0){
                pickImageCamera()

            }else if (id == 1){
                pickImageGallery()
            }

            true
        }
    }
    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }
    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //lấy uri ảnh
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                //set cho imageview
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //huy
                Toast.makeText(this, "Đã hủy", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //lấy uri ảnh
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set cho imageview
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //huy
                Toast.makeText(this, "Đã hủy", Toast.LENGTH_SHORT).show()
            }
        }
    )

}