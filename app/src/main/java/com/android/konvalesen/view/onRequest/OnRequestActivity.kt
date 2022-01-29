package com.android.konvalesen.view.onRequest

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ActivityOnRequestBinding
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.view.onRequest.adapter.SectionPagerAdapterOnReq
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnRequestActivity : AppCompatActivity() {
    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.tab_text_1,
            R.string.tab_text_3
        )
    }
    private lateinit var binding: ActivityOnRequestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sectionsPagerAdapter = SectionPagerAdapterOnReq(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pagerOnReq)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs_onReq)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()

        binding.toolbar7.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val inten = Intent(this, HomeActivity::class.java)
        inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(inten)
        finish()
    }
}