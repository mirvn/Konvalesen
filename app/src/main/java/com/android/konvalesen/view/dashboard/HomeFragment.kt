package com.android.konvalesen.view.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentHomeBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.view.bantuan.BantuanActivity
import com.android.konvalesen.view.dashboard.adapter.PermintaanBantuanAdapter
import com.android.konvalesen.view.login.MainActivity
import com.android.konvalesen.view.onReceive.OnReceiveActivity
import com.android.konvalesen.view.onRequest.OnRequestActivity
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class HomeFragment : Fragment() {
    companion object{
        private val TAG = HomeFragment::class.java.simpleName
        val TOPIC = "topics/notifDonor"
    }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var sessionUser: SessionUser
    private var requesterAdapter: PermintaanBantuanAdapter = PermintaanBantuanAdapter()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionUser = SessionUser(requireContext())
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        requestViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(RequestViewModel::class.java)
        //get data user from firebase
        firebaseAuth = FirebaseAuth.getInstance()
        getUserData()
        val fcmToken = sessionUser.sharedPreferences.getString("fcmToken", "").toString()
        Firebase.messaging.subscribeToTopic(fcmToken)
        loadDataOnRv()

        binding.toolbar2.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    logout()
                }
                R.id.action_pendonoran_saya -> {
                    val mIntent = Intent(requireContext(), OnReceiveActivity::class.java)
                    startActivity(mIntent)
                }
                R.id.action_bantuan_donor_saya->{
                    val mIntent = Intent(requireContext(),OnRequestActivity::class.java)
                    startActivity(mIntent)
                }
            }
            true
        }
        val nomor = sessionUser.sharedPreferences.getString("nomor","").toString()
        requestViewModel.setDataReqFromFirebase(nomor)

        binding.btnBantuan.setOnClickListener {
            requestViewModel.getDataReqFromFirebase().observe({lifecycle},{dataRequester ->
                if (dataRequester.nomorRequester != null) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.apply {
                        setIcon(R.drawable.ic_baseline_warning_24)
                        setTitle(getString(R.string.warning_bantuan_title))
                        setMessage(getString(R.string.warning_bantuan))
                        setCancelable(false)
                        setPositiveButton("OK"){_,_ ->

                        }
                    }.create().show()
                }else{
                    val intent = Intent(requireContext(),BantuanActivity::class.java)
                    startActivity(intent)
                }
            })
        }
    }

    private fun getUserData(){
        firebaseAuth.currentUser?.phoneNumber.let {
            userViewModel.getDataUserFromFirebase(it.toString())
        }
        userViewModel.getDataUser().observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = View.GONE
            binding.tvNama.text = "Hai, ${it.nama}"
            binding.btnGolDarProfile.text = it.golongan_darah
            //set session
            if (sessionUser.sharedPreferences.all.values == null) {
                sessionUser.setUserId(it.id.toString())
                sessionUser.setUserName(it.nama.toString())
                sessionUser.setUserNomor(it.nomor.toString())
                sessionUser.setUserGolonganDarah(it.golongan_darah.toString())
                sessionUser.setFcmToken(it.fcm_token.toString())
            }
        })
    }

    private fun logout() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        //clear session
        sessionUser.sharedPreferences.edit().clear().apply()

        val mIntent = Intent(activity?.applicationContext, MainActivity::class.java)
        startActivity(mIntent)
        activity?.finish()
    }

    private fun loadDataOnRv() {
        requestViewModel.setAllDataReqFromFirebase(
            getString(R.string.status_mencari_pendonor),
            sessionUser.sharedPreferences.getString("golonganDarah", "").toString()
        )
        requestViewModel.getAllDataReqFromFirebase().observe({ lifecycle }, {
            requesterAdapter.setDataReqDonor(it)
            showRv()
        })
    }

    private fun showRv() {
        val rv = binding.rvListBantuDonor
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = requesterAdapter
    }
}