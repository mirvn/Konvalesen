package com.android.konvalesen.view.bantuan

import android.Manifest
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
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentBantuanBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class BantuanFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private lateinit var binding: FragmentBantuanBinding
    private lateinit var mapFragment: SupportMapFragment
    private var golDarah:String? = null
    private lateinit var mMap:GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var addresses : List<Address>
    //address
    private var alamat = ""

    companion object{
        private const val LOCATION_REQ_CODE = 1000
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
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        mMap = gMap
        mMap.uiSettings.isZoomControlsEnabled = true //zoom control
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(marker: Marker) {
                val latLng = marker.position
                addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                alamat = addresses[0].getAddressLine(0).toString()
                Log.d("TAG", "onMarkerDragEnd - alamat:$alamat ")

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
                val currentLatLong = LatLng(location.latitude,location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,12f))
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong).draggable(true)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude,currentLatLong.longitude,1)

        alamat = addresses[0].getAddressLine(0).toString()

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