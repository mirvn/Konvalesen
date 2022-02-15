package com.android.konvalesen.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.konvalesen.R
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.view.login.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        GlobalScope.launch {
            delay(2000)
            val auth = FirebaseAuth.getInstance()
            // Check if user is signed in (non-null) and update UI accordingly.
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(applicationContext, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}