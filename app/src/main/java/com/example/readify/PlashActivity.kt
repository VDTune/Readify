package com.example.readify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class PlashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plash)

        Handler().postDelayed(Runnable{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        },2000) //2s
    }
}