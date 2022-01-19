package com.android.konvalesen.view.bantuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.konvalesen.R
import com.android.konvalesen.view.dashboard.HomeActivity

class BantuanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bantuan)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val inten = Intent(this, HomeActivity::class.java)
        inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(inten)
    }
}