package com.example.readify.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.readify.R
import com.example.readify.databinding.ActivityCategoryUpdateBinding

class CategoryUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryUpdateBinding

    //id, tên thể loại
    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.categoryEt.setText("$category")


    }
}