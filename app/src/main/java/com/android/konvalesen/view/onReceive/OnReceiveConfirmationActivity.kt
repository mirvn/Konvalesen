package com.android.konvalesen.view.onReceive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ActivityOnReceiveConfirmationBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.view.dashboard.HomeActivity
import com.android.konvalesen.viewmodel.OnReceiveConfirmationViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OnReceiveConfirmationActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private var TAG = OnReceiveConfirmationActivity::class.java.simpleName
        const val UID_EXTRA: String = "uid"
        const val EXTRA_DATA_REQ = "extra_data_req"
    }

    private lateinit var binding: ActivityOnReceiveConfirmationBinding
    private lateinit var mapFragment: SupportMapFragment

    // private var golDarah: String? = null
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var addresses: List<Address>
    private lateinit var receiveViewModel: OnReceiveConfirmationViewModel
    private lateinit var userViewModel: UserViewModel

    //private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private lateinit var uidRequester: String
    private lateinit var dist: String
    private var dataRequester = ArrayList<RequestDonor>()
    private var latLng: LatLng = LatLng(0.0, 0.0)

    //address
    private var alamat = ""
    private var lokasiAlamat = ""
    private var dataReq = RequestDonor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnReceiveConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //check whether Extra from intent exist or not
        if (intent.extras?.containsKey(EXTRA_DATA_REQ) == true) {
            dataReq = intent?.getParcelableExtra(EXTRA_DATA_REQ)!!
        } else {
            uidRequester = intent.getStringExtra(UID_EXTRA).toString()
        }
        Log.d(TAG, "onCreate: $dataReq")
        sessionUser = SessionUser(this)
        auth = FirebaseAuth.getInstance()
        receiveViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(OnReceiveConfirmationViewModel::class.java)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)

        if (dataReq.idRequester.isNullOrEmpty()) {
            //set from notification
            receiveViewModel.setDataReqFromFirebase(
                uidRequester,
                getString(R.string.status_mencari_pendonor)
            )
            receiveViewModel.getDataReqFromFirebase().observe({ lifecycle }, {
                dataRequester.addAll(listOf(it))
                binding.tvNamaRequesterBantuan.text = dataRequester[0].namaRequester.toString()
                setProgressBar(false)
                binding.tvLokasiOnMap3.text = dataRequester[0].alamatRequester.toString()
                binding.btnGolDarRequester.text = dataRequester[0].darahRequester.toString()
                binding.tvTgl.text = dataRequester[0].tanggal.toString()
                userViewModel.getAllDataUserWithIdFromFirebase(dataRequester[0].idRequester.toString())
                userViewModel.getAlldataUserWithId().observe({ lifecycle }, { user ->
                    val firebaseStorage =
                        FirebaseStorage.getInstance()
                            .getReference("profileImages/${user[0].foto.toString()}")
                    firebaseStorage.downloadUrl.addOnCompleteListener { taskUri ->
                        Glide.with(this).load(taskUri.result).into(binding.imgProfileConfirmation)
                    }
                })
            })
        } else {
            //set from RV home
            binding.tvNamaRequesterBantuan.text = dataReq.namaRequester.toString()
            setProgressBar(false)
            binding.tvLokasiOnMap3.text = dataReq.alamatRequester.toString()
            binding.btnGolDarRequester.text = dataReq.darahRequester.toString()
            binding.tvTgl.text = dataReq.tanggal.toString()
            userViewModel.getAllDataUserWithIdFromFirebase(dataReq.idRequester.toString())
            userViewModel.getAlldataUserWithId().observe({ lifecycle }, { user ->
                val firebaseStorage =
                    FirebaseStorage.getInstance()
                        .getReference("profileImages/${user[0].foto.toString()}")
                firebaseStorage.downloadUrl.addOnCompleteListener { taskUri ->
                    Glide.with(this).load(taskUri.result).into(binding.imgProfileConfirmation)
                }
            })

            val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
            receiveViewModel.setDataApprovedFromFirebase(
                nomor,
                getString(R.string.status_approve)
            )
        }
        Log.d(TAG, "onCreate: $dataRequester")
        mapFragment = supportFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnBersediaMendonor.setOnClickListener {
            val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
            receiveViewModel.setDataApprovedFromFirebase(
                nomor,
                getString(R.string.status_approve)
            )
            receiveViewModel.getDataApprovedFromFirebase().observe({ lifecycle }, {
                val alert = AlertDialog.Builder(this)
                val v: View = View.inflate(this, R.layout.layout_beri_bantuan, null)
                val cb = v.findViewById<CheckBox>(R.id.cb_setujuBantu)
                val myFormat = "dd-MM-yyyy/HH:mm:ss" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                sdf.timeZone = TimeZone.getDefault()

                if (it.nomorApprover == nomor && it.status == getString(R.string.status_approve)) {
                    val alert1 = AlertDialog.Builder(this)
                    alert1.apply {
                        setIcon(R.drawable.ic_baseline_warning_24)
                        setTitle(getString(R.string.warning_bantuan_title))
                        setMessage(getString(R.string.warning_menerima_bantuan))
                        setCancelable(false)
                        setPositiveButton("OK") { _, _ ->
                            onBackPressed()
                        }
                    }.create().show()
                } else {
                    //set from notification
                    if (dataReq.idRequester.isNullOrEmpty()) {
                        val dataApprover = ApprovedDonorData(
                            "",
                            auth.currentUser?.uid.toString(),
                            uidRequester,
                            sessionUser.sharedPreferences.getString("nama", "").toString(),
                            sessionUser.sharedPreferences.getString("nomor", "").toString(),
                            dist,
                            sdf.format(System.currentTimeMillis()).toString(),
                            getString(R.string.status_approve)
                        )
                        alert.apply {
                            setView(v)
                            setIcon(R.drawable.ic_baseline_bloodtype_24)
                            setTitle(getString(R.string.bantu_donor))
                            //setMessage(getString(R.string.warning_setuju_donor))
                            setCancelable(false)
                            setPositiveButton("SETUJU") { _, _ ->
                                if (cb.isChecked) {
                                    receiveViewModel.createNewApprovedReq(
                                        dataApprover,
                                        this@OnReceiveConfirmationActivity
                                    )
                                    val mIntent =
                                        Intent(
                                            this@OnReceiveConfirmationActivity,
                                            OnReceiveActivity::class.java
                                        )
                                    startActivity(mIntent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@OnReceiveConfirmationActivity,
                                        getString(R.string.cb_confirmation_not_checked),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            setNegativeButton("CANCEL") { _, _ ->
                            }
                        }.create().show()
                    } else {
                        //set from rv home
                        val dataApprover = ApprovedDonorData(
                            "",
                            auth.currentUser?.uid.toString(),
                            dataReq.idRequester,
                            sessionUser.sharedPreferences.getString("nama", "").toString(),
                            sessionUser.sharedPreferences.getString("nomor", "").toString(),
                            dist,
                            sdf.format(System.currentTimeMillis()).toString(),
                            getString(R.string.status_approve)
                        )
                        alert.apply {
                            setView(v)
                            setIcon(R.drawable.ic_baseline_bloodtype_24)
                            setTitle(getString(R.string.bantu_donor))
                            //setMessage(getString(R.string.warning_setuju_donor))
                            setCancelable(false)
                            setPositiveButton("SETUJU") { _, _ ->
                                if (cb.isChecked) {
                                    receiveViewModel.createNewApprovedReq(
                                        dataApprover,
                                        this@OnReceiveConfirmationActivity
                                    )
                                    val mIntent =
                                        Intent(
                                            this@OnReceiveConfirmationActivity,
                                            OnReceiveActivity::class.java
                                        )
                                    startActivity(mIntent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@OnReceiveConfirmationActivity,
                                        getString(R.string.cb_confirmation_not_checked),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            setNegativeButton("CANCEL") { _, _ ->
                            }
                        }.create().show()
                    }
                }
            })
        }

        binding.btnMenolakMendonor.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            alert.apply {
                setIcon(R.drawable.ic_baseline_warning_24)
                setTitle(getString(R.string.tolak_beri_bantuan))
                setMessage(getString(R.string.messege_tolak_membantu))
                setCancelable(false)
                setPositiveButton("OK") { _, _ ->
                    val mIntent =
                        Intent(this@OnReceiveConfirmationActivity, HomeActivity::class.java)
                    startActivity(mIntent)
                    finish()
                }
                setNegativeButton("CANCEL") { _, _ ->
                }
            }.create().show()
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        mMap = gMap
        mMap.uiSettings.isZoomControlsEnabled = true //zoom control
        setupMap()
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // You can directly ask for the permission.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
            mMap.isMyLocationEnabled = true
            var latLngRequester: LatLng?

            if (dataReq.idRequester.isNullOrEmpty()) {
                //set from notification
                receiveViewModel.getDataReqFromFirebase().observe({ lifecycle }, {
                    latLngRequester =
                        dataRequester[0].latRequester?.let {
                            dataRequester[0].lngRequester?.let { it1 ->
                                LatLng(it, it1)
                            }
                        }
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            lastLocation = location
                            latLng = LatLng(location.latitude, location.longitude)
                            latLngRequester?.let { placeMarkerOnMap(it) }
                            val line = PolylineOptions().add(
                                latLngRequester,
                                latLng
                            ).width(10f).color(R.color.konvalesen)
                            mMap.addPolyline(line)
                            latLngRequester?.let { CameraUpdateFactory.newLatLngZoom(it, 8f) }
                                ?.let { mMap.animateCamera(it) }
                            geocoder = Geocoder(this, Locale.getDefault())

                            //calculate distance between 2 points
                            val destinationPoint = Location("destinationPoint")
                            latLngRequester?.latitude.let {
                                if (it != null) {
                                    destinationPoint.latitude = it
                                }
                            }
                            latLngRequester?.longitude.let {
                                if (it != null) {
                                    destinationPoint.longitude = it
                                }
                            }
                            dist = location.distanceTo(destinationPoint).toString()
                            binding.tvJarak.text = "+-${dist}meter"
                        }
                    }
                })
            } else {
                //set from rv home
                latLngRequester =
                    dataReq.latRequester?.let {
                        dataReq.lngRequester?.let { it1 ->
                            LatLng(it, it1)
                        }
                    }
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        lastLocation = location
                        latLng = LatLng(location.latitude, location.longitude)
                        latLngRequester?.let { placeMarkerOnMap(it) }
                        val line = PolylineOptions().add(
                            latLngRequester,
                            latLng
                        ).width(10f).color(R.color.konvalesen)
                        mMap.addPolyline(line)
                        latLngRequester?.let { CameraUpdateFactory.newLatLngZoom(it, 5f) }
                            ?.let { mMap.animateCamera(it) }

                        //calculate distance between 2 points
                        val destinationPoint = Location("destinationPoint")
                        latLngRequester?.latitude.let {
                            if (it != null) {
                                destinationPoint.latitude = it
                            }
                        }
                        latLngRequester?.longitude.let {
                            if (it != null) {
                                destinationPoint.longitude = it
                            }
                        }
                        dist = location.distanceTo(destinationPoint).toString()
                        binding.tvJarak.text = "+-${dist}meter"
                    }
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    setupMap()
                } else {
                    setupMap()
                }
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong).draggable(false)
        geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)

        alamat = addresses[0].getAddressLine(0).toString()
        if (addresses[0].premises != null) {
            lokasiAlamat = addresses[0].locality.toString()
        }

        markerOptions.title(alamat)
        mMap.addMarker(markerOptions)
    }

    private fun setProgressBar(setOn: Boolean) {
        if (setOn) {
            binding.progressBar7.visibility = View.VISIBLE
            binding.progressBar8.visibility = View.VISIBLE
        } else {
            binding.progressBar7.visibility = View.INVISIBLE
            binding.progressBar8.visibility = View.INVISIBLE
        }
    }
}