package com.example.readify

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.readify.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    //setup view binding
    private lateinit var binding: ActivityPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Tiến trình
    private lateinit var progressDialog: ProgressDialog

    //arraylist chứa các thể loại
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri pdf
    private var pdfUri: Uri? = null

    //tag
    private val TAG = "PDF_ADD_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategory()

        //setup tien trinh
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Chỉ một chút")
        progressDialog.setCanceledOnTouchOutside(false)

        //xử lí nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lí click hiển thị danh sách thể loại
        binding.categoryTv.setOnClickListener{
            categoryPickDialog()
        }

        //xử lí click pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // xử lí click, upload pdf
        binding.submitBtn.setOnClickListener {
            /*1. xác thực dữ liệu
            * 2. upload pdf lên firebase
            * 3. lấy url của file được upload
            * 4. upload thông tin file lên firebase*/

            validateData()
        }
    }

    private var title = ""
    private var desc = ""
    private var category = ""

    private fun validateData() {
        /*1. xác thực dữ liệu */
        Log.d(TAG, "validateData: Validating data")

        //lấy dữ liệu
        title = binding.titleEt.text.toString().trim()
        desc = binding.descEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //xác thực
        if(title.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập tiêu đề...",Toast.LENGTH_SHORT).show()
        }
        else if (desc.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập mô tả...",Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập thể loại...",Toast.LENGTH_SHORT).show()
        }
        else if(pdfUri == null){
            Toast.makeText(this,"Vui lòng chọn URI...",Toast.LENGTH_SHORT).show()
        }
        else{
            //xác thực thành công, bắt đầu upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        //upload to firebase storage
        Log.d(TAG, "uploadPdfToStorage: upload to storage")

        //show tiến trình
        progressDialog.setMessage("Tải lên tệp PDF...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //địa chỉ file pdf trong firebase storage
        val filePathAndName = "Books/$timestamp"

        //tham chiếu lưu trữ
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                /*lấy url*/
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfToStorage: Failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Tải lên thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        //upload thông tin file lên firebase
        Log.d(TAG, "uploadPdfInfoToDb: upload to db")
        progressDialog.setMessage("Tải lên thông tin file...")

        //uid của người dùng hiện tại
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["desc"] = "$desc"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: upload to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Đã tải lên...",Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "uploadPdfInfoToDb: Failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Tải lên thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadPdfCategory() {
        Log.d(TAG, "loadPdfCategory: Loading pdf category")
        categoryArrayList = ArrayList()

        //ref
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list truoc khi nhap du lieu
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //lay du lieu
                    val model = ds.getValue(ModelCategory::class.java)
                    //them vao arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //lấy mảng chuỗi thể loại từ arraylist
        val categoryArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoryArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn Thể Loại")
            .setItems(categoryArray){dialog, which ->
                //xử lí click chuột lên item(lấy item được click)
                selectedCategoryId = categoryArrayList[which].id
                selectedCategoryTitle = categoryArrayList[which].category
                
                //đưa thể loại vào textview thể loại
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Seclected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Seclected Category Title: $selectedCategoryTitle")
            }.show()
    }

    private fun  pdfPickIntent(){
        Log.d(TAG, "pdfPickIntent: Starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this,"Hủy bỏ", Toast.LENGTH_SHORT).show()
            }
        }

    )
}