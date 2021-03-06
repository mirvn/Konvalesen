package com.android.konvalesen.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentHomeBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.model.RequestDonorWithPhoto
import com.android.konvalesen.view.DeleteAccountActivity
import com.android.konvalesen.view.bantuan.BantuanActivity
import com.android.konvalesen.view.dashboard.adapter.PermintaanBantuanAdapter
import com.android.konvalesen.view.login.MainActivity
import com.android.konvalesen.view.onReceive.OnReceiveActivity
import com.android.konvalesen.view.onRequest.OnRequestActivity
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment() {
    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        val TOPIC = "topics/notifDonor"
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var sessionUser: SessionUser
    private lateinit var nomor: String
    private var requesterAdapter: PermintaanBantuanAdapter = PermintaanBantuanAdapter()
    private var data: ArrayList<RequestDonor> = ArrayList()
    private var dataSingleWithPhoto = RequestDonorWithPhoto()

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
        nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
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

        /*binding.refreshRv.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                requesterAdapter.clearDataReqDonor()
                requesterAdapter.notifyDataSetChanged()
                binding.rvListBantuDonor.adapter = requesterAdapter
                data.clear()
                //show all req with same blood type
                requestViewModel.setAllDataReqWithStatusFromFirebase(
                    getString(R.string.status_mencari_pendonor),
                    sessionUser.sharedPreferences.getString("golonganDarah", "").toString()
                )
                binding.refreshRv.isRefreshing = false
            }
        })*/

        binding.toolbar2.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    logout()
                }
                R.id.action_pendonoran_saya -> {
                    val mIntent = Intent(requireContext(), OnReceiveActivity::class.java)
                    startActivity(mIntent)
                    activity?.finish()
                }
                R.id.action_bantuan_donor_saya -> {
                    val mIntent = Intent(requireContext(), OnRequestActivity::class.java)
                    startActivity(mIntent)
                    activity?.finish()
                }
                R.id.delete_account -> {
                    val mIntent = Intent(requireContext(), DeleteAccountActivity::class.java)
                    startActivity(mIntent)
                    activity?.finish()
                }
            }
            true
        }

        requestViewModel.setDataReqFromFirebase(nomor, getString(R.string.status_mencari_pendonor))
        binding.btnBantuan.setOnClickListener {
            requestViewModel.getDataReqFromFirebase().observe({ lifecycle }, { dataRequester ->
                if (dataRequester.nomorRequester != null &&
                    dataRequester.status == getString(R.string.status_mencari_pendonor)
                ) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.apply {
                        setIcon(R.drawable.ic_baseline_warning_24)
                        setTitle(getString(R.string.warning_bantuan_title))
                        setMessage(getString(R.string.warning_bantuan))
                        setCancelable(false)
                        setPositiveButton("OK") { _, _ ->

                        }
                    }.create().show()
                } else {
                    val intent = Intent(requireContext(), BantuanActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    activity?.startActivity(intent)
                    activity?.finish()
                }
            })
        }
    }

    fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionFineLocationResultCallback.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {

        }
    }

    private val permissionFineLocationResultCallback =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            when (it) {
                true -> {
                    checkPermission()
                }
                false -> {
                    checkPermission()
                }
            }
        }

    private fun getUserData() {
        if (firebaseAuth.uid?.isNotEmpty() == true) {
            firebaseAuth.currentUser?.phoneNumber.let {
                userViewModel.getDataUserFromFirebase(it.toString())
            }
            userViewModel.getDataUser().observe(viewLifecycleOwner) {
                binding.progressBar3.visibility = View.GONE
                val firebaseStorage =
                    FirebaseStorage.getInstance()
                        .getReference("profileImages/${it.foto.toString()}")
                firebaseStorage.downloadUrl.addOnSuccessListener { Uri ->
                    Glide.with(requireContext()).load(Uri).into(binding.imgProfile)
                }
                /*firebaseStorage.downloadUrl.addOnFailureListener {
                    val intent = Intent(requireContext(),MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }*/
                Log.d(TAG, "getUserData: ${it.foto.toString()}")
                binding.tvNama.text = "Hai, ${it.nama}"
                binding.btnGolDarProfile.text = it.golongan_darah
                //set session
                if (sessionUser.sharedPreferences.getString("id", "").toString().isNullOrEmpty()) {
                    sessionUser.setUserId(it.id.toString())
                    sessionUser.setUserName(it.nama.toString())
                    sessionUser.setUserNomor(it.nomor.toString())
                    sessionUser.setUserGolonganDarah(it.golongan_darah.toString())
                    sessionUser.setFcmToken(it.fcm_token.toString())
                }
                Log.d(
                    TAG,
                    "getUserData: ${sessionUser.sharedPreferences.getString("id", "").toString()}"
                )
            }
        } else {

        }
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
        //show all req with same blood type
        requestViewModel.setAllDataReqWithStatusFromFirebase(
            getString(R.string.status_mencari_pendonor),
            sessionUser.sharedPreferences.getString("golonganDarah", "").toString()
        )
        requestViewModel.getAllDataReqWithStatusFromFirebase().observe({ lifecycle }, {
            val uidUser = firebaseAuth.currentUser!!.uid
            data.clear()
            data =
                it.filter { it.idRequester != uidUser } as ArrayList //Filter data from Requester exclude current user
            Log.d(TAG, "loadDataOnRv-size: ${data.size}")
            if (data.isNotEmpty()) {
                requesterAdapter.clearDataReqDonor()
                binding.tvKosong.visibility = View.INVISIBLE
                for (i in 0 until data.size) {
                    userViewModel.getAllDataUserWithIdFromFirebase(data[i].idRequester.toString())
                    userViewModel.getAlldataUserWithId().observe({ lifecycle }, { user ->
                        dataSingleWithPhoto.tanggal = data[i].tanggal.toString()
                        dataSingleWithPhoto.status = data[i].status.toString()
                        dataSingleWithPhoto.nomorRequester = data[i].nomorRequester.toString()
                        dataSingleWithPhoto.namaRequester = data[i].namaRequester.toString()
                        dataSingleWithPhoto.lngRequester = data[i].lngRequester
                        dataSingleWithPhoto.latRequester = data[i].latRequester
                        dataSingleWithPhoto.idRequester = data[i].idRequester.toString()
                        dataSingleWithPhoto.idDoc = data[i].idDoc.toString()
                        dataSingleWithPhoto.fotoRequester = user[0].foto.toString()
                        dataSingleWithPhoto.darahRequester = data[i].darahRequester.toString()
                        dataSingleWithPhoto.alamatRequester = data[i].alamatRequester.toString()
                        requesterAdapter.setDataReqDonor(dataSingleWithPhoto)
                    })
                }
                checkPermission()
                showRv()
            } else {
                binding.rvListBantuDonor.visibility = View.INVISIBLE
                binding.tvKosong.visibility = View.VISIBLE
            }
        })
    }

    private fun showRv() {
        val rv = binding.rvListBantuDonor
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = requesterAdapter
    }
}