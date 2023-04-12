package com.example.readify

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.readify.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi một lát")
        progressDialog.setCanceledOnTouchOutside(false)

        //Xử lí btn back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //Xử lí btn đăng kí
        binding.registerBtn.setOnClickListener {
            /*Các bước thực hiện
            * 1) Nhập dữ liệu
            * 2) Xác thực
            * 3) Tạo tài khoản - Firebase Auth
            * 4) Lưu tài khảon - Firebase Realtime Database*/
            validateData()
        }


    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        //Nhập dữ liệu
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //Xác nhận
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Mật khẩu không trùng...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //Tạo tài khoản
        progressDialog.setMessage("Tạo tài khoản...")
        progressDialog.show()

        //tạo người dùng trong firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //thành công, thêm thông tin người dùng vào db
                updateUserInfo()

                Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //thất bại
                progressDialog.dismiss()
                Toast.makeText(this, "Thất bại... \n Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        // Lưu thông tin người dùng vào realtime db
        progressDialog.setMessage("Lưu thông tin người dùng...")

        // tạo timestamp
        val timestamp = System.currentTimeMillis()

        // lấy id cuả người dùng hiện tại
        val uid = firebaseAuth.uid

        //setup dữ liệu để đưa vào db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user" //2 kiểu người dùng user/admin
        hashMap["timestamp"] = timestamp

        //thiet lap du lieu vao db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //add du lieu thanh cong
                progressDialog.dismiss()
                Toast.makeText(this, "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                //add du lieu that bai
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Thêm dữ liệu thất bại... \n Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }
}