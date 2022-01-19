package com.android.konvalesen.view.login

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.BuildConfig
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentVerifikasiRegisterBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.User
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VerifikasiRegisterFragment : Fragment() {
    private lateinit var binding: FragmentVerifikasiRegisterBinding
    private val TAG = VerifikasiRegisterFragment::class.java.simpleName
    private lateinit var userViewModel: UserViewModel
    private var golDarah: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser

    //img
    private lateinit var imageUri: Uri
    private lateinit var imgFile: File
    private lateinit var imgName: String
    private var contentUri: Uri = Uri.EMPTY
    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVerifikasiRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        sessionUser = SessionUser(requireContext())
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            sessionUser.setFcmToken(it)
        }
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        setGolonganDarah()

        binding.btnNext.setOnClickListener {
            createDataUser()
        }

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.imgProfileRegister.setImageURI(uri) // Handle the returned Uri
                contentUri = uri
                val cursor: Cursor? = activity?.contentResolver?.query(
                    uri, null, null,
                    null, null
                )
                /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             *//*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
                val fileIndex: Int? = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor?.moveToFirst()
                imgName = fileIndex?.let { it1 -> cursor.getString(it1) }.toString()
                Log.d(TAG, "onCreate: $imgName")
                imageUri = getFilePathFromUri(uri)
                cursor?.close()
            }
        }

        binding.btnPilihFoto.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    //Get Document real file path
    @Throws(IOException::class)
    fun getFilePathFromUri(uri: Uri): Uri {
        val fileNameCopy: String = imgName
        val root =
            File(
                activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                BuildConfig.APPLICATION_ID + File.separator
            )
        root.mkdirs()
        imgFile = File(root, fileNameCopy)
        imgFile.createNewFile()
        FileOutputStream(imgFile).use { outputStream ->
            if (uri != null) {
                activity?.contentResolver?.openInputStream(uri).use { inputStream ->
                    if (inputStream != null) {
                        inputStream.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                    } //Simply reads input to output stream
                    outputStream.flush()
                }
            }
        }
        val uriDoc = Uri.fromFile(imgFile)
        return uriDoc
    }

    private fun createDataUser() {
        val firebaseStorage =
            FirebaseStorage.getInstance().getReference("profileImages/${imgFile.name}")
        //store image to Firebase
        firebaseStorage.putFile(Uri.fromFile(imgFile)).addOnSuccessListener {
            postCreateData(imgFile.name)
        }
    }

    private fun postCreateData(imgName: String) {
        val alert = AlertDialog.Builder(requireContext())
        val v: View = View.inflate(requireContext(), R.layout.layout_loading_login, null)
        val alertD = alert.apply {
            setView(v)
            setCancelable(false)
        }.create()
        val data = User(
            auth.currentUser?.uid,
            binding.edtNama.text.toString(),
            auth.currentUser?.phoneNumber,
            golDarah,
            sessionUser.sharedPreferences.getString("fcmToken", "").toString(),
            imgName
        )
        alertD.show()
        userViewModel.createDataUser(data, requireContext())
        userViewModel.getDataCreated().observe({ lifecycle }, {
            if (it.id != "") {
                alertD.cancel()
                val mIntent = Intent(context, HomeActivity::class.java)
                startActivity(mIntent)
                activity?.finish()
            } else Toast.makeText(requireContext(), "failed", Toast.LENGTH_SHORT).show()
        })
    }

    private fun setGolonganDarah() {
        binding.btnGolA.setOnClickListener {
            golDarah = binding.btnGolA.text as String?
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolAplus.setOnClickListener {
            golDarah = binding.btnGolAplus.text as String?
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolAmin.setOnClickListener {
            golDarah = binding.btnGolAmin.text as String?
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolB.setOnClickListener {
            golDarah = binding.btnGolB.text as String?
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolBplus.setOnClickListener {
            golDarah = binding.btnGolBplus.text as String?
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolBmin.setOnClickListener {
            golDarah = binding.btnGolBmin.text as String?
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolO.setOnClickListener {
            golDarah = binding.btnGolO.text as String?
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolOplus.setOnClickListener {
            golDarah = binding.btnGolOplus.text as String?
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolOmin.setOnClickListener {
            golDarah = binding.btnGolOmin.text as String?
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolAB.setOnClickListener {
            golDarah = binding.btnGolAB.text as String?
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolABmin.setOnClickListener {
            golDarah = binding.btnGolABmin.text as String?
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
        binding.btnGolABplus.setOnClickListener {
            golDarah = binding.btnGolABplus.text as String?
            binding.btnGolABplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.btn_golongan_darah_clicked
                )
            )
            binding.btnGolAplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolABmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolAmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolA.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolB.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBmin.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolBplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolO.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnGolOplus.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
    }
}