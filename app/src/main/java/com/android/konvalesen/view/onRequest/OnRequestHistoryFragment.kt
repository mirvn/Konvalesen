package com.android.konvalesen.view.onRequest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnRequestHistoryBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.view.onRequest.adapter.OnRequestHistoryAdapter
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class OnRequestHistoryFragment : Fragment() {
    companion object {
        val TAG = OnRequestHistoryFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentOnRequestHistoryBinding
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private var historyAdapter: OnRequestHistoryAdapter = OnRequestHistoryAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnRequestHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionUser = SessionUser(requireContext())
        auth = FirebaseAuth.getInstance()
        requestViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(RequestViewModel::class.java)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
        val id = sessionUser.sharedPreferences.getString("id", "").toString()
        requestViewModel.setAllDataReqWithNumberFromFirebase(id, nomor)
        loadDataHistory()
    }

    private fun loadDataHistory() {
        var historyData: ArrayList<RequestDonor>
        requestViewModel.getAllDataReqWithNumberFromFirebase().observe({ lifecycle }, {
            historyData =
                it.filter { it.status != getString(R.string.status_mencari_pendonor) } as ArrayList
            Log.d(TAG, "loadDataHistory: $historyData")
            if (historyData.isNotEmpty()) {
                binding.tvDataKosong.visibility = View.GONE
                historyAdapter.setDataHistoryReq(historyData)
                showRv()
            } else {
                binding.rvOnReqHistory.visibility = View.GONE
                binding.tvDataKosong.visibility = View.VISIBLE
            }
        })
    }

    private fun showRv() {
        val rv = binding.rvOnReqHistory
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = historyAdapter
    }

}