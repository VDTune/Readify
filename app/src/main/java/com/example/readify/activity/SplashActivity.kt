package com.example.readify.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.readify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plash)
        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            startActivity(Intent(this, MainActivity::class.java))

            checkUser()
        }, 1000) //1s
    }

    private fun checkUser() {
        //lấy người dùng hiện tại (đăng nhập hay chưa)
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //chưa đăng nhập, dẫn để mfn hình chính
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //đã đăng nhâp
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // lấy user type
                        val userType = snapshot.child("userType").value
                        if (userType == "user") {
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DashboardUserActivity::class.java
                                )
                            )
                            finish()
                        } else if (userType == "admin") {
                            startActivity(
                                Intent(
                                    this@SplashActivity,
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
}