package com.android.konvalesen.view.onReceive

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnReceiveHistoryBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.model.HistoryOnReceive
import com.android.konvalesen.model.RequestDonor
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
        receiveViewModel.getDataHistoryApprovedFromFirebase().observe({ lifecycle }, {
            donorDone = it.filter { it.status != getString(R.string.status_approve) } as ArrayList
            Log.d(TAG, "loadDataHistory: $donorDone")
            if (donorDone.isNotEmpty()) {
                binding.textView14.visibility = View.GONE
                historyAdapter.clearDataHistory()
                var dataReq = ArrayList<RequestDonor>()
                var singleDataHistory = HistoryOnReceive()
                for (i in 0 until donorDone.size) {
                    requestViewModel.setAllDataReqFromFirebase(donorDone[i].idRequester.toString())
                    requestViewModel.getAllDataReqFromFirebase()
                        .observe({ lifecycle }, { liveDataReq ->
                            dataReq = liveDataReq.filter { data ->
                                data.status != getString(R.string.status_mencari_pendonor)
                            } as ArrayList<RequestDonor>
                            if (dataReq.isNotEmpty()) {
                                singleDataHistory.tgl_approve =
                                    donorDone[i].tanggalApprove.toString()
                                singleDataHistory.status = donorDone[i].status.toString()
                                singleDataHistory.nama_penerima =
                                    dataReq[i].namaRequester.toString()
                                singleDataHistory.lokasi = dataReq[i].alamatRequester.toString()
                                singleDataHistory.gol_darah_penerima =
                                    dataReq[i].darahRequester.toString()
                                userViewModel.getAllDataUserFromFirebase()
                                userViewModel.getAlldataUser()
                                    .observe({ lifecycle }, { dataUserRequester ->
                                        val data =
                                            dataUserRequester.filter { it.id == donorDone[i].idRequester }
                                        singleDataHistory.foto_penerima = data[i].foto.toString()
                                        historyAdapter.setDataHistoryRec(singleDataHistory)
                                    })
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
}