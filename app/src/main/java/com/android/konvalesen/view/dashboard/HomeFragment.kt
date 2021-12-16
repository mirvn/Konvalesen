package com.android.konvalesen.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentHomeBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.view.bantuan.BantuanActivity
import com.android.konvalesen.view.login.MainActivity
import com.android.konvalesen.view.onReceive.OnReceiveActivity
import com.android.konvalesen.view.onRequest.OnRequestActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging

class HomeFragment : Fragment() {
    companion object{
        private val TAG = HomeFragment::class.java.simpleName
    }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var sessionUser: SessionUser

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
        //get data user from firebase
        firebaseAuth = FirebaseAuth.getInstance()
        getUserData()
        FirebaseMessaging.getInstance().subscribeToTopic(
            sessionUser.sharedPreferences.getString("fcmToken","").toString()
        ) //subscribe topic notification
        binding.toolbar2.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_logout ->{
                    logout()
                }
                R.id.action_pendonoran_saya->{
                    val mIntent = Intent(requireContext(),OnReceiveActivity::class.java)
                    startActivity(mIntent)
                }
                R.id.action_bantuan_donor_saya->{
                    val mIntent = Intent(requireContext(),OnRequestActivity::class.java)
                    startActivity(mIntent)
                }
            }
            true
        }
        binding.btnBantuan.setOnClickListener {
            val intent = Intent(requireContext(),BantuanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getUserData(){
        firebaseAuth.currentUser?.phoneNumber.let {
            userViewModel.getDataUserFromFirebase(it.toString())
        }
        userViewModel.getDataUser().observe(viewLifecycleOwner,{
            binding.progressBar3.visibility = View.GONE
            binding.tvNama.text = "Hai, ${it.nama}"
            //set session
            sessionUser.setUserId(it.id.toString())
            sessionUser.setUserName(it.nama.toString())
            sessionUser.setUserNomor(it.nomor.toString())
            sessionUser.setUserGolonganDarah(it.golongan_darah.toString())
            sessionUser.setFcmToken(it.fcm_token.toString())
            Log.d(TAG, "getUserData: ${sessionUser.sharedPreferences.getString("fcmToken","")}")
        })
    }

    private fun logout(){
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        //clear session
        sessionUser.sharedPreferences.edit().clear().apply()

        val mIntent = Intent(activity?.applicationContext,MainActivity::class.java)
        startActivity(mIntent)
        activity?.finish()
    }
}