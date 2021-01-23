package com.example.twittercloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }

    fun btnLoginEvent(view: View) {

    }
    fun btnRegisterEvent(view: View) {
        val intent= Intent(this,Register::class.java)
        startActivity(intent)
    }
}