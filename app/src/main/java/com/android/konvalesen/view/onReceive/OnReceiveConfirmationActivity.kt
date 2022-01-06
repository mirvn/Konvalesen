package com.android.konvalesen.view.onReceive

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.GnssAntennaInfo
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ActivityOnReceiveConfirmationBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.viewmodel.OnReceiveConfirmationViewModel
import com.android.konvalesen.viewmodel.UserViewModel
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
import java.util.*
import kotlin.collections.ArrayList


class OnReceiveConfirmationActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private var TAG = OnReceiveConfirmationActivity::class.java.simpleName
        const val UID_EXTRA: String = "uid"
        private const val LOCATION_REQ_CODE = 1000
    }

    private lateinit var binding: ActivityOnReceiveConfirmationBinding
    private lateinit var mapFragment: SupportMapFragment
    private var golDarah: String? = null
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var addresses: List<Address>
    private lateinit var receiveViewModel: OnReceiveConfirmationViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private lateinit var uidRequester: String
    private var dataRequester = ArrayList<RequestDonor>()
    private var latLng: LatLng = LatLng(0.0, 0.0)

    //address
    private var alamat = ""
    private var lokasiAlamat = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnReceiveConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uidRequester = intent.getStringExtra(UID_EXTRA).toString()
        Log.d("TAG", "onCreate: $uidRequester")
        sessionUser = SessionUser(this)
        auth = FirebaseAuth.getInstance()
        receiveViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(OnReceiveConfirmationViewModel::class.java)
        receiveViewModel.setDataReqFromFirebase(uidRequester)
        receiveViewModel.getDataReqFromFirebase().observe({ lifecycle }, {
            dataRequester.addAll(listOf(it))
            binding.tvNamaRequesterBantuan.text = dataRequester[0].namaRequester.toString()
            // binding.tvLokasiOnMap3.text = dataRequester[0].alamatRequester.toString()
            binding.btnGolDarRequester.text = dataRequester[0].darahRequester.toString()
            binding.tvTgl.text = dataRequester[0].tanggal.toString()
        })
        Log.d(TAG, "onCreate: $dataRequester")
        mapFragment = supportFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
        }else {
            mMap.isMyLocationEnabled = true
            var latLngRequester: LatLng?
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        geocoder = Geocoder(this, Locale.getDefault())
                        addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        lokasiAlamat = addresses[0].locality.toString()
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
                        val dist = location.distanceTo(destinationPoint)
                        Log.d(TAG, "setupMap- Distance2points: $dist")
                        binding.tvJarak.text = "+-${dist}meter"
                    }
                }
            })
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
            Log.d("TAG", "onMarkerDragEnd - lokasi:$lokasiAlamat ")
        }

        markerOptions.title(alamat)
        mMap.addMarker(markerOptions)
        //
        setProgressBar(false)
        binding.tvLokasiOnMap3.text = alamat
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