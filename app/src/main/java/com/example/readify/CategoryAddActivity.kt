package com.example.readify

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.readify.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Chỉ một chút...")
        progressDialog.setCanceledOnTouchOutside(false)

        //xử lý nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lý nút thêm
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        //xác nhận data

        //lấy data
        category = binding.categoryEt.text.toString().trim()


        //xác nhận data
        if(category.isEmpty()){
            Toast.makeText(this,"Vui lòng nhập Thể Loại...",Toast.LENGTH_SHORT).show()
        }
        else{
            addCategoryFirebase()
        }

    }

    private fun addCategoryFirebase() {
        //show tiến trình
        progressDialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //set up dữ liệu để đẩy vào firebase
        val hashMap = HashMap<String,Any>()

        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Thêm vào firebase db: Database Root > Category > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //Thêm thể loại thành công
                progressDialog.dismiss()
                Toast.makeText(this,"Thêm thành công!",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                //thêm thất bại
                progressDialog.dismiss()
                Toast.makeText(this,"Thêm thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()

            }

    }
}