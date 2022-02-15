package com.android.konvalesen.view.onReceive

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnReceiveHistoryBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.model.HistoryOnReceive
import com.android.konvalesen.view.onReceive.adapter.OnReceiveHistoryAdapter
import com.android.konvalesen.viewmodel.OnReceiveConfirmationViewModel
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class OnReceiveHistoryFragment : Fragment() {
    companion object {
        val TAG = OnReceiveHistoryFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentOnReceiveHistoryBinding
    private lateinit var receiveViewModel: OnReceiveConfirmationViewModel
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private var historyAdapter: OnReceiveHistoryAdapter = OnReceiveHistoryAdapter()
    private var dataHistory = ArrayList<HistoryOnReceive>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnReceiveHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionUser = SessionUser(requireContext())
        auth = FirebaseAuth.getInstance()
        receiveViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(OnReceiveConfirmationViewModel::class.java)
        requestViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(RequestViewModel::class.java)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
        receiveViewModel.setDataHistoryApprovedFromFirebase(nomor)
        loadDataHistory()
    }

    private fun loadDataHistory() {
        var donorDone: ArrayList<ApprovedDonorData>
        receiveViewModel.getDataHistoryApprovedFromFirebase().observeListener({ lifecycle }, {
            donorDone = it.filter { it.status != getString(R.string.status_approve) } as ArrayList
            Log.d(TAG, "loadDataHistory-donorDone: ${donorDone}")
            Log.d(TAG, "loadDataHistory-donorDone: ${donorDone.size}")
            if (donorDone.isNotEmpty()) {
                binding.textView14.visibility = View.GONE
                //historyAdapter.clearDataHistory()
                val data = HistoryOnReceive()
                for (i in 0 until donorDone.size) {
                    Log.d(TAG, "loadDataHistory-i: $i")
                    requestViewModel.setAllDataReqWithDocIdFromFirebase(
                        donorDone[i].docIdRequester.toString()
                    )
                    requestViewModel.getAllDataReqWithDocIdFromFirebase()
                        .observe({ lifecycle }, { liveDataReq ->
                            Log.d(TAG, "loadDataHistory-dataReqreal:$liveDataReq ")
                            Log.d(TAG, "loadDataHistory-dataReqreal:${liveDataReq.size} ")
                            if (liveDataReq.isNotEmpty()) {
                                //dataHistory.ensureCapacity(1)
                                data.tgl_approve =
                                    donorDone[i].tanggalApprove.toString()
                                data.status =
                                    donorDone[i].status.toString()
                                data.nama_penerima =
                                    liveDataReq[i].namaRequester.toString()
                                data.lokasi =
                                    liveDataReq[i].alamatRequester.toString()
                                data.gol_darah_penerima =
                                    liveDataReq[i].darahRequester.toString()
                                data.nomor_penerima =
                                    liveDataReq[i].nomorRequester.toString()
                                dataHistory.add(data)
                                Log.d(TAG, "loadDataHistory2: $dataHistory")
                                historyAdapter.setDataHistoryRec(dataHistory)
                            } else {
                                binding.rvHistoryOnRec.visibility = View.GONE
                                binding.textView14.visibility = View.VISIBLE
                            }
                        })
                }
                showRv()
            } else {
                binding.rvHistoryOnRec.visibility = View.GONE
                binding.textView14.visibility = View.VISIBLE
            }
        })
    }

    private fun showRv() {
        val rv = binding.rvHistoryOnRec
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = historyAdapter
    }

    //extention function for observe once
    fun <T> LiveData<T>.observeListener(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}