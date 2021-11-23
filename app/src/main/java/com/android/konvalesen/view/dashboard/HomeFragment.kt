package com.android.konvalesen.view.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.databinding.FragmentHomeBinding
import com.android.konvalesen.view.bantuan.BantuanActivity
import com.android.konvalesen.view.login.MainActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var userViewModel: UserViewModel

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
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        //get data user from firebase
        firebaseAuth = FirebaseAuth.getInstance()
        getUserData()

        binding.toolbar2.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                logout()
                return true
            }
        })

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
        })
    }

    private fun logout(){
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        val mIntent = Intent(activity?.applicationContext,MainActivity::class.java)
        startActivity(mIntent)
        activity?.finish()
    }
}