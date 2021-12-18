package com.android.konvalesen.view.onRequest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnRequestBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class OnGoingRequestFragment : Fragment() {
    private lateinit var binding: FragmentOnRequestBinding
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var sessionUser: SessionUser
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnRequestBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(RequestViewModel::class.java)
        sessionUser = SessionUser(requireContext())
        auth = FirebaseAuth.getInstance()
        val nomor = sessionUser.sharedPreferences.getString("nomor","").toString()
        requestViewModel.setDataReqFromFirebase(nomor)
        requestViewModel.getDataReqFromFirebase().observe({lifecycle},{dataRequester ->
            if (dataRequester != null) {
                showLayout(true)
                binding.tvLokasiOnMap2.text = dataRequester.alamatRequester.toString()
                binding.btnGolA3.text = dataRequester.darahRequester.toString()
            }else{
                showLayout(false)
            }
        })
    }
    private fun showLayout(show:Boolean){
        if (show){
            binding.progressBar6.visibility = View.INVISIBLE
            binding.tvMintaBantuan2.visibility =View.VISIBLE
            binding.imageView4.visibility =View.VISIBLE
            binding.imageView9.visibility =View.VISIBLE
            binding.tvLokasiOnMap2.visibility =View.VISIBLE
            binding.tvLokasiPenerima2.visibility =View.VISIBLE
            binding.tvDarahKebutuhan2.visibility =View.VISIBLE
            binding.btnGolA3.visibility =View.VISIBLE
            binding.tvBersediaMendonor.visibility =View.VISIBLE
            binding.rvOnGoingRequest.visibility =View.VISIBLE
            binding.btnAkhiriPermintaanDonor.visibility =View.VISIBLE
            binding.tvDataKosong.visibility =View.INVISIBLE
        }else{
            binding.progressBar6.visibility = View.INVISIBLE
            binding.tvMintaBantuan2.visibility =View.INVISIBLE
            binding.imageView4.visibility =View.INVISIBLE
            binding.imageView9.visibility =View.INVISIBLE
            binding.tvLokasiOnMap2.visibility =View.INVISIBLE
            binding.tvLokasiPenerima2.visibility =View.INVISIBLE
            binding.tvDarahKebutuhan2.visibility =View.INVISIBLE
            binding.btnGolA3.visibility =View.INVISIBLE
            binding.tvBersediaMendonor.visibility =View.INVISIBLE
            binding.rvOnGoingRequest.visibility =View.INVISIBLE
            binding.btnAkhiriPermintaanDonor.visibility =View.INVISIBLE
            binding.tvDataKosong.visibility =View.VISIBLE
        }
    }
}