package com.android.konvalesen.view.onRequest.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.konvalesen.databinding.ItemOnReqPenyediaDonorBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.AcceptedDonor
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import java.net.URLEncoder

class OnGoingRequestAdapter : RecyclerView.Adapter<OnGoingRequestAdapter.ListViewHolder>() {
    val listReceived = ArrayList<AcceptedDonor>()
/*    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var addresses: List<Address>*/

    fun setDataHistory(dataHistory: AcceptedDonor) {
        listReceived.add(dataHistory)
        notifyDataSetChanged()
    }

    fun clearDataHistory() {
        listReceived.clear()
    }

    inner class ListViewHolder(val binding: ItemOnReqPenyediaDonorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dataHistory: AcceptedDonor) {
            //binding data with view
            binding.tvNamaPendonor.text = dataHistory.nama
            binding.tvJarakPendonor.text = "+- ${dataHistory.jarak} Meter"
            val firebaseStorage =
                FirebaseStorage.getInstance()
                    .getReference("profileImages/${dataHistory.foto}")
            firebaseStorage.downloadUrl.addOnCompleteListener { taskUri ->
                Glide.with(itemView.context).load(taskUri.result)
                    .into(binding.imgProfilePendonor)
            }
            binding.btnWaOnGoingReq.setOnClickListener {
                val session = SessionUser(itemView.context)
                val nama = session.sharedPreferences.getString("nama", "").toString()
                val messege =
                    "Hai, saya $nama lagi mencari pendonor plasma dari Aplikasi Konvalesen. Apa kamu mau membantu?"
                val url =
                    "https://api.whatsapp.com/send?phone=${dataHistory.nomor}&text=${
                        URLEncoder.encode(
                            messege,
                            "UTF-8"
                        )
                    }"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                itemView.context.startActivity(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val listDataReq =
            ItemOnReqPenyediaDonorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ListViewHolder(listDataReq)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listRec = listReceived[position]
        holder.bind(listRec)
    }

    override fun getItemCount(): Int {
        return listReceived.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}