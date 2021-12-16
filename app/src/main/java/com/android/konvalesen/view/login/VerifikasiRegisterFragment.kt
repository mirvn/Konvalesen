package com.android.konvalesen.view.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentVerifikasiRegisterBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.User
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class VerifikasiRegisterFragment : Fragment() {
    private lateinit var binding: FragmentVerifikasiRegisterBinding
    private val TAG = VerifikasiRegisterFragment::class.java.simpleName
    private lateinit var userViewModel: UserViewModel
    private var golDarah: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVerifikasiRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            val sessionUser = SessionUser(requireContext())
            sessionUser.setFcmToken(it)
        }
        userViewModel = ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        setGolonganDarah()
        binding.btnNext.setOnClickListener {
            val mIntent = Intent(context,HomeActivity::class.java)
            createDataUser()
            startActivity(mIntent)
            activity?.finish()
        }
    }

    private fun createDataUser(){
        val sessionUser = SessionUser(requireContext())
        val data = User(
            auth.currentUser?.uid,
            binding.edtNama.text.toString(),
            auth.currentUser?.phoneNumber,
            golDarah,
            sessionUser.sharedPreferences.getString("fcmToken","")
        )
        userViewModel.createDataUser(data,requireContext())
    }

    private fun setGolonganDarah(){
        binding.btnGolA.setOnClickListener{
            golDarah = binding.btnGolA.text as String?
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAplus.setOnClickListener {
            golDarah = binding.btnGolAplus.text as String?
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAmin.setOnClickListener {
            golDarah = binding.btnGolAmin.text as String?
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolB.setOnClickListener {
            golDarah = binding.btnGolB.text as String?
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolBplus.setOnClickListener {
            golDarah = binding.btnGolBplus.text as String?
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolBmin.setOnClickListener {
            golDarah = binding.btnGolBmin.text as String?
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolO.setOnClickListener {
            golDarah = binding.btnGolO.text as String?
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolOplus.setOnClickListener {
            golDarah = binding.btnGolOplus.text as String?
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolOmin.setOnClickListener {
            golDarah = binding.btnGolOmin.text as String?
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAB.setOnClickListener {
            golDarah = binding.btnGolAB.text as String?
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolABmin.setOnClickListener {
            golDarah = binding.btnGolABmin.text as String?
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolABplus.setOnClickListener {
            golDarah = binding.btnGolABplus.text as String?
            binding.btnGolABplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
    }
}