package com.example.readify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.readify.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //xử lí nút đăng xuất
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            Toast.makeText(this, "Đăng xuất thành công",Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //chưa đăng nhặp, người dùng có thể ở lại user dashboard khi chưa đăng nhâp
            binding.subTitleTv.text = "Bạn chưa đăng nhập"
        } else {
            val email = firebaseUser.email //lấy email
            binding.subTitleTv.text = email//Hiện email người dùng bằng text view
        }

    }
}