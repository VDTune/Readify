package com.example.readify.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import com.example.readify.R
import com.example.readify.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityForgotPasswordBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    //thông báo tiến trình
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        //khởi tạo firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //progress
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi một lát")
        progressDialog.setCanceledOnTouchOutside(false)

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lí click
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }


    private var email = ""
    private fun validateData() {
        //lấy dữ liệu
        email = binding.emailEt.text.toString().trim()

        //xác thực dữ liệu
        if(email.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập Email...", Toast.LENGTH_SHORT).show()

        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show()
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show tiến trình
        progressDialog.setMessage("Gửi mã xác nhận đến Email: $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //gửi mã thành công
                progressDialog.dismiss()
                Toast.makeText(this, "Đã gửi mã đến Email: $email",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                //lỗi
                progressDialog.dismiss()
                Toast.makeText(this, "Gửi thất bại.. Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()

            }
    }
}