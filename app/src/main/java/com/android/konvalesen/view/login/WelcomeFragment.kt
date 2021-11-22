package com.android.konvalesen.view.login

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.konvalesen.databinding.FragmentWelcomeBinding
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private var TAG = WelcomeFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage("Anda yakin keluar dari aplikasi?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Ya") { _, _ ->
                        activity?.finish()
                    }
                    .setNegativeButton("Tidak"){ dialog, _ ->
                        dialog.dismiss()
                    }

                val alert = dialogBuilder.create()
                alert.setTitle("Konvalesen")
                alert.show()
            }
        })

        binding.btnNext4.setOnClickListener {
            val phoneNumber = "${binding.edtNomorPrefix.text}${binding.edtNomor.text}"
            //send number with Arguments
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToVerifikasiLoginFragment(phoneNumber)
            Navigation.findNavController(binding.root).navigate(action)
        }
    }
}