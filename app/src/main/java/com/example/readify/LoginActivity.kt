package com.example.readify

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.readify.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var googleSignInClient: GoogleSignInClient
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



        //tạo tham số lấy API gmail đăng nhập
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        //khi mở app sẽ tự động đăng nhập bằng gmail đã đn trc đó
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        updateUILogged()

        //tạo sự kiện nhấp vào nút đăng nhập bằng gmail
        binding.loginWithGoogle.setOnClickListener {
            signIn()
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
                Toast.makeText(this, "Email hoặc Mật khẩu không đúng", Toast.LENGTH_SHORT)
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

                }
            })
    }

    //đăng nhập thành công -> trang home
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, DashboardUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //xác minh gmail và cho phép đăng nhập
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //mở trang home bằng gmail đã đăng nhập khi mở app
    private fun updateUILogged() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val intent = Intent(applicationContext, DashboardUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
        const val EXTRA_NAME = "EXTRA_NAME"
    }

    //hàm onActivityResult sẽ truyền vào hàm firebaseAuthWithGoogle idToken của account
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(ContentValues.TAG, "Google sign in failed", e)
            }
        }
    }

    //truyền idToken vào chứng chỉ (credential) thông qua chứng chỉ cho phép đăng nhập và thực hiện hàm updateUI
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Đăng nhập thành công, cập nhật giao diện người dùng với thông tin người dùng đã đăng nhập
                Log.d(ContentValues.TAG, "signInWithCredential:success")
                val user = firebaseAuth.currentUser
                updateUI(user)
            } else {
                Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                updateUI(null)
            }
        }
    }
}