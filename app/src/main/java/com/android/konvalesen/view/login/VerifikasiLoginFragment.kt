package com.android.konvalesen.view.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentVerifikasiLoginBinding
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class VerifikasiLoginFragment : Fragment() {
    private lateinit var binding: FragmentVerifikasiLoginBinding
    private val args: VerifikasiLoginFragmentArgs by navArgs()
    private lateinit var userViewModel: UserViewModel

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth

    // [END declare_auth]
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var TAG = VerifikasiLoginFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVerifikasiLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alertLoading = AlertDialog.Builder(requireContext())
        val alert = alertLoading.apply {
            setView(R.layout.layout_loading_otp)
            setCancelable(false)
        }.create()
        alert.show()

        binding.toolbar4.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvPerintahVerifikasiLogin.text =
            "${getString(com.android.konvalesen.R.string.silahkan_memasukkan_kode_verifikasi_yang_telah_kami_kirimkan_ke_nomor_anda)}" +
                    "${args.phoneNumber}"
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // signInWithPhoneAuthCredential(credential)
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                // get verification code from sms
                val code = credential.smsCode
                binding.pinKodeVerifRegistrasiLogin.setText(code)
                if (binding.pinKodeVerifRegistrasiLogin.text?.isNotEmpty() == true) {
                    signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                }
                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                alert.cancel()
                countDown()
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                Toast.makeText(
                    requireContext(),
                    "Kode verifikasi telah dikirim",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnNextLogin.setOnClickListener {
            val code = binding.pinKodeVerifRegistrasiLogin.text
            if (code.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Kode OTP belum dimasukkan", Toast.LENGTH_SHORT)
                    .show()
            } else verifyPhoneNumberWithCode(storedVerificationId, code.toString())
        }
        //resend otp code
        binding.tvResendCodeLogin.setOnClickListener {
            if (binding.tvResendCodeLogin.text == getString(R.string.kirim_ulang_kode_verifikasi)) {
                resendVerificationCode(args.phoneNumber, resendToken)
            }
        }
        //Verifikasi nomor
        startPhoneNumberVerification(args.phoneNumber)
    }

    private fun countDown() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendCodeLogin.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.countdown_text
                    )
                )
                binding.tvResendCodeLogin.text =
                    "${getString(R.string.kirim_ulang_kode_verifikasi)}: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                binding.tvResendCodeLogin.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.tvResendCodeLogin.text =
                    getString(R.string.kirim_ulang_kode_verifikasi)
            }
        }.start()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        //auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
        // [END verify_with_code]
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                //login success
                val phoneNumber = auth.currentUser?.phoneNumber.toString()
                Toast.makeText(requireContext(), "Logged in as $phoneNumber", Toast.LENGTH_SHORT)
                    .show()
                //start fragment Verifikasi register or home
                userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
                    .get(UserViewModel::class.java)
                userViewModel.getDataUserFromFirebase(args.phoneNumber)
                userViewModel.getDataUser().observe(viewLifecycleOwner, {
                    if (args.phoneNumber == it.nomor) {
                        val mIntent = Intent(context, HomeActivity::class.java)
                        startActivity(mIntent)
                        activity?.finish()
                    } else {
                        val action =
                            VerifikasiLoginFragmentDirections.actionVerifikasiLoginFragmentToVerifikasiFragment(
                                phoneNumber
                            )
                        Navigation.findNavController(binding.root).navigate(action)
                    }
                })
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}