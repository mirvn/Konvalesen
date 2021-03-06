package com.android.konvalesen.view.dashboard.adapter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.konvalesen.databinding.ItemMembutuhkanDonorBinding
import com.android.konvalesen.model.RequestDonorWithPhoto
import com.android.konvalesen.view.onReceive.OnReceiveConfirmationActivity
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage

class PermintaanBantuanAdapter : RecyclerView.Adapter<PermintaanBantuanAdapter.ListViewHolder>() {
    val listReqDonor = ArrayList<RequestDonorWithPhoto>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun setDataReqDonor(dataReqList: RequestDonorWithPhoto) {
        listReqDonor.add(dataReqList)
        notifyDataSetChanged()
    }

    fun clearDataReqDonor() {
        listReqDonor.clear()
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding: ItemMembutuhkanDonorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reqDonorData: RequestDonorWithPhoto) {
            calculateDistance(reqDonorData)
            binding.tvNamaMembutuhkan.text = reqDonorData.namaRequester.toString()
            binding.tvTanggal.text = reqDonorData.tanggal.toString()
            binding.tvGoldarMembutuhkan.text = reqDonorData.darahRequester.toString()
            val firebaseStorage =
                FirebaseStorage.getInstance()
                    .getReference("profileImages/${reqDonorData.fotoRequester.toString()}")
            Log.d("TAG", "bind foto: ${reqDonorData.fotoRequester.toString()}")
            firebaseStorage.downloadUrl.addOnCompleteListener { taskUri ->
                Glide.with(itemView.context).load(taskUri.result)
                    .into(binding.imgProfileMembutuhkan)
            }

            binding.btnDetailMembutuhkan.setOnClickListener {
                val intent = Intent(itemView.context, OnReceiveConfirmationActivity::class.java)
                intent.putExtra(OnReceiveConfirmationActivity.EXTRA_DATA_REQ, reqDonorData)
                itemView.context.startActivity(intent)
            }
        }

        private fun calculateDistance(reqDonorData: RequestDonorWithPhoto) {
            if (ActivityCompat.checkSelfPermission(
                    itemView.context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    itemView.context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // You can directly ask for the permission.
                //val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                //HomeFragment().checkPermission()
            } else {
                val latLngRequester: LatLng? =
                    reqDonorData.latRequester?.let {
                        reqDonorData.lngRequester?.let { it1 ->
                            LatLng(it, it1)
                        }
                    }
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(itemView.context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
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
                        val dist = location.distanceTo(destinationPoint).toString()
                        Log.d("TAG", "setupMap: $dist")
                        binding.tvJarakMembantu.text = "+-${dist}meter"
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val listDataReq =
            ItemMembutuhkanDonorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ListViewHolder(listDataReq)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val rekapList = listReqDonor[position]
        holder.bind(rekapList)
    }

    override fun getItemCount(): Int {
        return listReqDonor.size
    }

    /*override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }*/
}