package com.android.konvalesen.view.onReceive.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.konvalesen.view.onReceive.OnGoingReceiveFragment
import com.android.konvalesen.view.onReceive.OnReceiveHistoryFragment

class SectionPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = OnGoingReceiveFragment()
            1 -> fragment = OnReceiveHistoryFragment()
        }
        return fragment as Fragment
    }

}