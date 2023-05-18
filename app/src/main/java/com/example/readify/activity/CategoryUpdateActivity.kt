package com.example.readify.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.readify.databinding.ActivityCategoryUpdateBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryUpdateBinding

    //arraylist để chứa tên thể loại
    private lateinit var categoryTitleArrayList: ArrayList<String>

    //arraylist để chứa id thể loại
    private lateinit var categoryIdArrayList: ArrayList<String>


    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lấy từ intent
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.categoryEt.setText(category)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            val updatedCategory = binding.categoryEt.text.toString()
            updateCategory(categoryId, updatedCategory)
        }

    }

    private fun updateCategory(categoryId: String, updatedCategory: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        val categoryRef = ref.child(categoryId)

        // Kiểm tra tên thể loại đã tồn tại hay chưa
        ref.orderByChild("category").equalTo(updatedCategory)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Tên thể loại đã tồn tại
                    Toast.makeText(this@CategoryUpdateActivity, "Tên thể loại đã tồn tại", Toast.LENGTH_SHORT).show()
                } else {
                    // Tên thể loại không tồn tại, tiến hành cập nhật
                    categoryRef.child("category").setValue(updatedCategory)
                        .addOnSuccessListener {
                            // Cập nhật thành công
                            Toast.makeText(this@CategoryUpdateActivity, "Sửa thành công", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                        .addOnFailureListener { error ->
                            // Xử lý khi cập nhật thất bại
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })
    }


}