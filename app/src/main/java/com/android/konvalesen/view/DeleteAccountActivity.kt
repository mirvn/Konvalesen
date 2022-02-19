package com.android.konvalesen.view

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ActivityDeleteAccountBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.view.login.MainActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.chaos.view.PinView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.TimeUnit

class DeleteAccountActivity : AppCompatActivity() {
    val TAG = DeleteAccountActivity::class.java.simpleName
    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var sessionUser: SessionUser
    private lateinit var countDown: CountDownTimer

    private var storedVerificationId: String? = ""
    private lateinit var storedCredential: PhoneAuthCredential
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var viewOTP: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar12.visibility = View.GONE
        viewOTP = View.inflate(this, R.layout.fragment_verifikasi_login, null)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                binding.progressBar12.visibility = View.GONE
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // signInWithPhoneAuthCredential(credential)
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                storedCredential = credential
                // get verification code from sms
                /*val code = credential.smsCode
                val pinCode = findViewById<PinView>(R.id.pin_kodeVerifRegistrasiLogin)*/
                countDown.cancel()
                deleteAccWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(this@DeleteAccountActivity, "${e.message}", Toast.LENGTH_SHORT)
                        .show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(this@DeleteAccountActivity, "${e.message}", Toast.LENGTH_SHORT)
                        .show()
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
                countDown()
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                sessionUser.setVerificationId(storedVerificationId.toString())
                resendToken = token
                Toast.makeText(
                    this@DeleteAccountActivity,
                    "Kode verifikasi telah dikirim",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sessionUser = SessionUser(this)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)

        binding.btnHapusAkun.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            alert.apply {
                setTitle(getString(R.string.konfirmasi_hapus_akun))
                setMessage(getString(R.string.delete_account_warning))
                setPositiveButton(getString(R.string.konfirmasi_hapus_akun)) { _, _ ->
                    binding.progressBar12.visibility = View.VISIBLE
                    startPhoneNumberVerification(
                        sessionUser.sharedPreferences.getString("nomor", "").toString()
                    )
                }
                setNegativeButton(getString(R.string.kembali)) { _, _ ->

                }
            }.create().show()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        //auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private fun countDown() {
        countDown = object : CountDownTimer(60000, 1000) {
            val tvResendCode = viewOTP.findViewById<TextView>(R.id.tv_resendCodeLogin)
            override fun onTick(millisUntilFinished: Long) {
                tvResendCode.text =
                    "${getString(R.string.kirim_ulang_kode_verifikasi)}: ${millisUntilFinished / 1000}" //error
            }

            override fun onFinish() {
                tvResendCode.text = getString(R.string.kirim_ulang_kode_verifikasi)
            }
        }.start()
    }

    // [START resend_verification]
    /*private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }*/

    private fun deleteAccWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val alertOTP = AlertDialog.Builder(this)
        val btnConfirmDelete = viewOTP.findViewById<Button>(R.id.btn_nextLogin)
        btnConfirmDelete.text = "Konfirmasi Hapus Akun"
        btnConfirmDelete.textSize = 12F
        val pinView = viewOTP.findViewById<PinView>(R.id.pin_kodeVerifRegistrasiLogin)
        //val tvResend = viewOTP.findViewById<TextView>(R.id.tv_resendCodeLogin)
        val toolbar = viewOTP.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar4)
        alertOTP.apply {
            setCancelable(false)
            toolbar.visibility = View.GONE
            setView(viewOTP)
            val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
            val id = sessionUser.sharedPreferences.getString("id", "").toString()
            /*tvResend.setOnClickListener {
                resendVerificationCode(nomor,resendToken)
            }*/
            btnConfirmDelete.setOnClickListener {
                val alertLoading = AlertDialog.Builder(this@DeleteAccountActivity)
                val vLoading: View =
                    View.inflate(applicationContext, R.layout.layout_loading_login, null)
                val showLoading = alertLoading.apply {
                    setView(vLoading)
                }.create()
                if (pinView.text?.isNotEmpty() == true) {
                    showLoading.show()
                    userViewModel.getDataUserFromFirebase(nomor)
                    userViewModel.setDataHistoryApprovedFromFirebase(nomor)
                    userViewModel.setAllDataReqWithNumberFromFirebase(id, nomor)
                    userViewModel.getDataUser().observe({ lifecycle }, { user ->
                        userViewModel.getDataHistoryApprovedFromFirebase()
                            .observe({ lifecycle }, { approvedData ->
                                userViewModel.getAllDataReqWithNumberFromFirebase()
                                    .observe({ lifecycle }, { dataReq ->
                                        for (i in 0 until dataReq.size) {
                                            userViewModel.deleteDataReq(
                                                dataReq[i].idDoc.toString(),
                                                this@DeleteAccountActivity
                                            )
                                        }
                                        for (i in 0 until approvedData.size) {
                                            userViewModel.deleteDataApprovedReq(
                                                approvedData[i].docId.toString(),
                                                this@DeleteAccountActivity
                                            )
                                        }
                                        FirebaseStorage.getInstance()
                                            .getReference("profileImages/${user.foto.toString()}")
                                            .delete()
                                        userViewModel.deeleteDataUser(
                                            user.docid.toString(),
                                            this@DeleteAccountActivity
                                        )
                                        FirebaseAuth.getInstance().currentUser?.reauthenticate(
                                            credential
                                        )?.addOnCompleteListener {
                                            FirebaseAuth.getInstance().currentUser?.delete()
                                                ?.addOnCompleteListener { taskDelete ->
                                                    if (taskDelete.isSuccessful) {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "Akun Berhasil Dihapus",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        val intent = Intent(
                                                            applicationContext,
                                                            MainActivity::class.java
                                                        )
                                                        alertOTP.create().cancel()
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }
                                        }
                                    })
                            })
                    })
                }
            }
        }.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}