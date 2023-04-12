package com.example.readify

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.readify.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi một lát")
        progressDialog.setCanceledOnTouchOutside(false)

        //Xử lí text "dang ki ngay"
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //xử lí btn login
        binding.loginBtn.setOnClickListener {
            /*1) Nhập dữ liệu
            * 2) Xác nhận dữ liệu
            * 3) Đăng nhập (Firebase Auth)
            * 4) kiểm trả kiểu người dùng
            *       - user -> user dashboard
            *       - admin -> admin dashboard */

            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //Nhập dữ liệu
        email = binding.emailEt.text.toString().trim()
        password = binding.passwprdEt.text.toString().trim()

        //Kiểm tra dữ liệu
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không đúng định dạng...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu...", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        //Đăng nhâp

        progressDialog.setMessage("Đang đăng nhập...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //đăng nhập thành công
                checkUserType()
            }
            .addOnFailureListener { e ->
                //đăng nhập thất bại
                progressDialog.dismiss()
                Toast.makeText(this, "Đăng nhập thất bại... Lỗi: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun checkUserType() {
        //Kiểm tra kiểu người dùng
        progressDialog.setMessage("Kiểm tra người dùng")
        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    // lấy user type
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    } else if (userType == "admin") {
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                DashboardAdminActivity::class.java
                            )
                        )
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}