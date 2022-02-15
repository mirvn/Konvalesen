package com.android.konvalesen.view.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.konvalesen.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    companion object {
        private val TAG = HomeActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityHomeBinding
    private var bundle = Bundle()
    private lateinit var phoneNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNumber = bundle.getString("phoneNumber").toString()
    }
}