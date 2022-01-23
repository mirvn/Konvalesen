package com.android.konvalesen.view.onReceive

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.konvalesen.R
import com.android.konvalesen.databinding.FragmentOnGoingReceiveBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.viewmodel.OnReceiveConfirmationViewModel
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


class OnGoingReceiveFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private var TAG = OnGoingReceiveFragment::class.java.simpleName
        // private const val LOCATION_REQ_CODE = 1000
    }

    private lateinit var binding: FragmentOnGoingReceiveBinding
    private lateinit var mapFragment: SupportMapFragment
    private var golDarah: String? = null
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var addresses: List<Address>
    private lateinit var receiveViewModel: OnReceiveConfirmationViewModel

    //private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionUser: SessionUser
    private lateinit var uidRequester: String
    private lateinit var dist: String
    private var dataApprover = ArrayList<ApprovedDonorData>()
    private var latLng: LatLng = LatLng(0.0, 0.0)

    //address
    private var alamat = ""
    private var lokasiAlamat = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnGoingReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionUser = SessionUser(requireContext())
        auth = FirebaseAuth.getInstance()
        receiveViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(OnReceiveConfirmationViewModel::class.java)
        loadDataOnGoingReceive()

        Log.d(TAG, "onCreate: $dataApprover")
        mapFragment =
            childFragmentManager.findFragmentById(R.id.mapOnGoingReceive) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.btnAkhiriDonorOnGoingReceive.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            val v: View = View.inflate(requireContext(), R.layout.layout_akhiri_pendonoran, null)
            val cbKebutuhanTidakTerpenuhi =
                v.findViewById<CheckBox>(R.id.cb_kebutuhanTidakTerpenuhi)
            val cbKebutuhanTerpenuhi = v.findViewById<CheckBox>(R.id.cb_kebutuhanTerpenuhi)

            cbKebutuhanTidakTerpenuhi.setOnClickListener {
                cbKebutuhanTerpenuhi.isChecked = false
            }
            cbKebutuhanTerpenuhi.setOnClickListener {
                cbKebutuhanTidakTerpenuhi.isChecked = false
            }

            alert.apply {
                setView(v)
                setTitle(getString(R.string.akhiri_pendonoran))
                setCancelable(false)
                setPositiveButton(getString(R.string.akhiri_pendonoran)) { _, _ ->
                    val alertDialog = AlertDialog.Builder(requireContext())
                    val viewLoading = View.inflate(requireContext(), R.layout.layout_loading, null)
                    val setAlert = alertDialog.apply {
                        setView(viewLoading)
                        setCancelable(false)
                    }.create()
                    receiveViewModel.getDataApprovedFromFirebase().observe({ lifecycle }, {
                        if (cbKebutuhanTidakTerpenuhi.isChecked) {
                            setAlert.show()
                            receiveViewModel.updateDataApprovedToDoneFirebase(
                                it.docId.toString(),
                                cbKebutuhanTidakTerpenuhi.text.toString()
                            )
                            receiveViewModel.getDataApprovedFromFirebase()
                                .removeObservers(requireActivity())
                            loadDataOnGoingReceive()
                            setAlert.dismiss()
                        } else if (cbKebutuhanTerpenuhi.isChecked) {
                            setAlert.show()
                            receiveViewModel.updateDataApprovedToDoneFirebase(
                                it.docId.toString(),
                                cbKebutuhanTerpenuhi.text.toString()
                            )
                            receiveViewModel.getDataApprovedFromFirebase()
                                .removeObservers(requireActivity())
                            loadDataOnGoingReceive()
                            setAlert.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.permintaan_mengakhiri_gagal),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
                setNegativeButton("CANCEL") { _, _ ->
                }
            }.create().show()
        }

        binding.btnDirectionToGmap.setOnClickListener {
            receiveViewModel.getAllDataReqWithIdFromFirebase().observe({ lifecycle }, {
                val lat = it[0].latRequester.toString()
                val long = it[0].lngRequester.toString()
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$long")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            })
        }
    }

    private fun loadDataOnGoingReceive() {
        binding.progressBar10.visibility = View.VISIBLE
        val nomor = sessionUser.sharedPreferences.getString("nomor", "").toString()
        receiveViewModel.setDataApprovedFromFirebase(nomor, getString(R.string.status_approve))
        receiveViewModel.getDataApprovedFromFirebase().observe({ lifecycle }, {
            binding.progressBar10.visibility = View.GONE
            if (it.nomorApprover == nomor &&
                it.status == getString(R.string.status_approve)
            ) {
                //dataApprover.addAll(listOf(it))
                receiveViewModel.setAllDataReqWithIdFromFirebase(
                    it.idRequester.toString(), getString(R.string.status_mencari_pendonor)
                )
                receiveViewModel.getAllDataReqWithIdFromFirebase()
                    .observe({ lifecycle }, { dataRequester ->
                        binding.tvTglOnGoingReceive.text =
                            dataRequester[0].tanggal.toString()
                        binding.tvNamaOnGoingReceive.text =
                            dataRequester[0].namaRequester.toString()
                        setProgressBar(false)
                        binding.tvLokasiOnMapOnGoingReceive.text =
                            dataRequester[0].alamatRequester.toString()
                        binding.btnGolDarOnGoingReceive.text =
                            dataRequester[0].darahRequester.toString()

                        binding.btnChatWa.setOnClickListener {
                            val url =
                                "https://api.whatsapp.com/send?phone=${dataRequester[0].nomorRequester}"
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            startActivity(i)
                        }
                    })
            } else {
                disableLayout(true)
            }
        })
    }

    override fun onMapReady(gMap: GoogleMap) {
        mMap = gMap
        mMap.uiSettings.isZoomControlsEnabled = true //zoom control
        setupMap()
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionResultCallback.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            mMap.isMyLocationEnabled = true
            var latLngRequester: LatLng?
            receiveViewModel.getAllDataReqWithIdFromFirebase()
                .observe({ lifecycle }, { dataRequester ->
                    latLngRequester = dataRequester[0].latRequester?.let {
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
                            Log.d(TAG, "setupMap- Distance2points: $dist")
                            binding.tvJarakOnGoingReceive.text = "+-${dist}meter"
                        }
                    }
                })
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong).draggable(true)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        addresses = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)

        alamat = addresses[0].getAddressLine(0).toString()
        if (addresses[0].premises != null) {
            lokasiAlamat = addresses[0].locality.toString()
            Log.d(TAG, "onMarkerDragEnd - lokasi:$lokasiAlamat ")
        }

        markerOptions.title(alamat)
        mMap.addMarker(markerOptions)
    }

    private val permissionResultCallback =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            when (it) {
                true -> {
                    setupMap()
                }
                false -> {
                    setupMap()
                }
            }
        }

    private fun setProgressBar(setOn: Boolean) {
        if (setOn) {
            binding.progressBarOnGoingRec.visibility = View.VISIBLE
            binding.progressBarOnGoingRec1.visibility = View.VISIBLE
        } else {
            binding.progressBarOnGoingRec.visibility = View.INVISIBLE
            binding.progressBarOnGoingRec1.visibility = View.INVISIBLE
        }
    }

    private fun disableLayout(setOff: Boolean) {
        if (setOff) {
            binding.progressBarOnGoingRec.visibility = View.GONE
            binding.progressBarOnGoingRec1.visibility = View.GONE
            binding.btnAkhiriDonorOnGoingReceive.visibility = View.GONE
            binding.btnChatWa.visibility = View.GONE
            binding.btnDirectionToGmap.visibility = View.GONE
            binding.btnGolDarOnGoingReceive.visibility = View.GONE
            binding.imgProfileConfirmation.visibility = View.GONE
            binding.imageView11.visibility = View.GONE
            binding.imageView12.visibility = View.GONE
            binding.tvDarahKebutuhan3.visibility = View.GONE
            binding.tvJarakOnGoingReceive.visibility = View.GONE
            binding.tvLokasiOnMapOnGoingReceive.visibility = View.GONE
            binding.tvLokasiPenerima3.visibility = View.GONE
            binding.tvNamaOnGoingReceive.visibility = View.GONE
            binding.tvTglOnGoingReceive.visibility = View.GONE
            binding.textView2.visibility = View.GONE
            binding.textView6.visibility = View.GONE
            binding.mapOnGoingReceive.visibility = View.GONE
            binding.tvDataOnGoingReceiveKosong.visibility = View.VISIBLE
        } else {
            binding.progressBarOnGoingRec.visibility = View.VISIBLE
            binding.progressBarOnGoingRec1.visibility = View.VISIBLE
            binding.btnAkhiriDonorOnGoingReceive.visibility = View.VISIBLE
            binding.btnChatWa.visibility = View.VISIBLE
            binding.btnDirectionToGmap.visibility = View.VISIBLE
            binding.btnGolDarOnGoingReceive.visibility = View.VISIBLE
            binding.imgProfileConfirmation.visibility = View.VISIBLE
            binding.imageView11.visibility = View.VISIBLE
            binding.imageView12.visibility = View.VISIBLE
            binding.tvDarahKebutuhan3.visibility = View.VISIBLE
            binding.tvJarakOnGoingReceive.visibility = View.VISIBLE
            binding.tvLokasiOnMapOnGoingReceive.visibility = View.VISIBLE
            binding.tvLokasiPenerima3.visibility = View.VISIBLE
            binding.tvNamaOnGoingReceive.visibility = View.VISIBLE
            binding.tvTglOnGoingReceive.visibility = View.VISIBLE
            binding.textView2.visibility = View.VISIBLE
            binding.textView6.visibility = View.VISIBLE
            binding.mapOnGoingReceive.visibility = View.VISIBLE
            binding.tvDataOnGoingReceiveKosong.visibility = View.GONE
        }
    }
}