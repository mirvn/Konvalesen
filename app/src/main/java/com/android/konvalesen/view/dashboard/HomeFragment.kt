package com.android.konvalesen.view.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.databinding.FragmentHomeBinding
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
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        //get data user from firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.phoneNumber?.let { userViewModel.getDataUserFromFirebase(it) }
        userViewModel.getDataUser().observe(viewLifecycleOwner,{
            binding.tvNama.text = it.nama
        })

        binding.toolbar2.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                logout()
                return true
            }
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