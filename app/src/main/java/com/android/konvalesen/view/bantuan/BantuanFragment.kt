package com.android.konvalesen.view.bantuan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentBantuanBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.NotificationData
import com.android.konvalesen.model.PushNotification
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.model.User
import com.android.konvalesen.pushNotification.RetrofitInstance
import com.android.konvalesen.view.onRequest.OnRequestActivity
import com.android.konvalesen.viewmodel.RequestViewModel
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class BantuanFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private lateinit var binding: FragmentBantuanBinding
    private lateinit var mapFragment: SupportMapFragment
    private var golDarah:String? = null
    private lateinit var mMap:GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var addresses : List<Address>
    private lateinit var requestViewModel: RequestViewModel
    private lateinit var userViewModel: UserViewModel
    private var allUser = ArrayList<User>()
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private var latLng: LatLng = LatLng(0.0,0.0)
    //address
    private var alamat = ""
    private var lokasiAlamat = ""

    companion object{
        private val TAG = BantuanActivity::class.java.simpleName
        private const val LOCATION_REQ_CODE = 1000
        const val TOPIC = "topics/notifDonor"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBantuanBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGolonganDarah()
        sessionUser = SessionUser(requireContext())
        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        userViewModel.getAllDataUserFromFirebase()
        requestViewModel = ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())
            .get(RequestViewModel::class.java)
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnNext2.setOnClickListener {
            val nama = sessionUser.sharedPreferences.getString("nama","")
            val dataDonor = RequestDonor(
                UUID.randomUUID().toString(),
                nama,
                sessionUser.sharedPreferences.getString("nomor",""),
                golDarah,
                binding.tvLokasiOnMap.text.toString(),
                latLng.latitude,
                latLng.longitude,
                0
            )
            userViewModel.getAlldataUser().observe({lifecycle},{
                allUser.addAll(it)
            })
            val notificationData = NotificationData("${getString(R.string.bantu_donor)} $nama di $lokasiAlamat",
                "Dia membutuhkan donor plasma darah Golongan '$golDarah'")
            //Filter all user with same blood type
            val allUserSameBloodType = allUser.filter {
                it.golongan_darah.toString() == golDarah.toString()
            }
            val allUserSameBloodTypeExcludeUserFcmToken = allUserSameBloodType.filter {
                it.fcm_token.toString() != sessionUser.sharedPreferences.getString("fcmToken","").toString()
            }
            Log.d(TAG, "onViewCreated-allUserSameBloodTypeExcludeUserFcmToken: $allUserSameBloodTypeExcludeUserFcmToken")
            //start pushNotification and create data in Firebase or just create data in Firebase
            if (allUserSameBloodTypeExcludeUserFcmToken.isEmpty()){
                requestViewModel.createNewRequestDonor(dataDonor,requireContext())
                val intent = Intent(requireContext(),OnRequestActivity::class.java)
                activity?.startActivity(intent)
            }else {
                requestViewModel.createNewRequestDonor(dataDonor,requireContext())
                    for(i in allUserSameBloodTypeExcludeUserFcmToken.indices){
                        requestViewModel.sendNotification(
                            allUserSameBloodTypeExcludeUserFcmToken[i].fcm_token.toString(),
                            notificationData,
                            requireContext()
                        )
                        Log.d(TAG, "onViewCreated- allUserSameBloodTypeExcludeUserFcmToken[i].fcm_token: ${allUserSameBloodTypeExcludeUserFcmToken[i].fcm_token.toString()}")
                    }
                val intent = Intent(requireContext(),OnRequestActivity::class.java)
                activity?.startActivity(intent)
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Log.d(TAG, "pushNotification: ${Gson().toJson(response)}")
            }else Log.e(TAG, "pushNotification: ${response.errorBody().toString()}")
        }catch (e:Exception){
            Log.e(TAG, "pushNotification: ${e}")
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        mMap = gMap
        mMap.uiSettings.isZoomControlsEnabled = true //zoom control
        mMap.setOnMarkerClickListener(this)
        //onClick
        mMap.setOnMapClickListener {
            mMap.clear()
            latLng = it
            val markerOptions = MarkerOptions().position(it).draggable(true)
            geocoder = Geocoder(requireContext(), Locale.getDefault())
            addresses = geocoder.getFromLocation(it.latitude,it.longitude,1)

            alamat = addresses[0].getAddressLine(0).toString()
            lokasiAlamat = addresses[0].locality.toString()
            Log.d("TAG", "onMarkerDragEnd - lokasi:${ addresses[0].locality} ")
            markerOptions.title(alamat)
            mMap.addMarker(markerOptions)
            //
            binding.tvLokasiOnMap.text = alamat
        }
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker) {
                //
            }

            override fun onMarkerDragEnd(marker: Marker) {
                latLng = marker.position
                addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
                alamat = addresses[0].getAddressLine(0).toString()
                if (addresses[0].premises != null){
                    lokasiAlamat = addresses[0].locality.toString()
                }
                Log.d("TAG", "onMarkerDragEnd - lokasi:$lokasiAlamat ")

                marker.title = alamat
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position,12f))
                setProgressBar(false)
                binding.tvLokasiOnMap.text = alamat
            }
            override fun onMarkerDragStart(marker: Marker) {
                binding.tvLokasiOnMap.text = ""
                setProgressBar(true)
            }
        })
        setupMap()
    }

    override fun onMarkerClick(marker: Marker): Boolean = false

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , LOCATION_REQ_CODE)
                return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null){
                lastLocation = location
                latLng = LatLng(location.latitude,location.longitude)
                placeMarkerOnMap(latLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12f))
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong).draggable(true)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude,currentLatLong.longitude,1)

        alamat = addresses[0].getAddressLine(0).toString()
        if (addresses[0].premises != null){
            lokasiAlamat = addresses[0].locality.toString()
            Log.d("TAG", "onMarkerDragEnd - lokasi:$lokasiAlamat ")
        }

        markerOptions.title(alamat)
        mMap.addMarker(markerOptions)
        //
        setProgressBar(false)
        binding.tvLokasiOnMap.text = alamat
    }

    private fun setProgressBar(setOn:Boolean){
        if (setOn){
            binding.progressBar4.visibility = View.VISIBLE
            binding.progressBar5.visibility = View.VISIBLE
        }else{
            binding.progressBar4.visibility = View.INVISIBLE
            binding.progressBar5.visibility = View.INVISIBLE
        }
    }

    private fun setGolonganDarah(){
        binding.btnGolA2.setOnClickListener{
            golDarah = binding.btnGolA2.text as String?
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAplus2.setOnClickListener {
            golDarah = binding.btnGolAplus2.text as String?
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAmin2.setOnClickListener {
            golDarah = binding.btnGolAmin2.text as String?
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolB2.setOnClickListener {
            golDarah = binding.btnGolB2.text as String?
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolBplus2.setOnClickListener {
            golDarah = binding.btnGolBplus2.text as String?
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolBmin2.setOnClickListener {
            golDarah = binding.btnGolBmin2.text as String?
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolO2.setOnClickListener {
            golDarah = binding.btnGolO2.text as String?
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolOplus2.setOnClickListener {
            golDarah = binding.btnGolOplus2.text as String?
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolOmin2.setOnClickListener {
            golDarah = binding.btnGolOmin2.text as String?
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolAB2.setOnClickListener {
            golDarah = binding.btnGolAB2.text as String?
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolABmin2.setOnClickListener {
            golDarah = binding.btnGolABmin2.text as String?
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.btnGolABplus2.setOnClickListener {
            golDarah = binding.btnGolABplus2.text as String?
            binding.btnGolABplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.btn_golongan_darah_clicked))
            binding.btnGolAplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolABmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolAmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolA2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolB2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBmin2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolBplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolO2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.btnGolOplus2.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
    }
}