package com.android.konvalesen.view.onRequest.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.konvalesen.view.onRequest.OnGoingRequestFragment
import com.android.konvalesen.view.onRequest.OnRequestHistoryFragment

class SectionPagerAdapterOnReq(activity: AppCompatActivity): FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = OnGoingRequestFragment()
            1 -> fragment = OnRequestHistoryFragment()
        }
        return fragment as Fragment
    }
}