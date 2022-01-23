package com.android.konvalesen.view.onReceive.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.konvalesen.databinding.ItemOnReceiveHistoryBinding
import com.android.konvalesen.model.HistoryOnReceive
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class OnReceiveHistoryAdapter : RecyclerView.Adapter<OnReceiveHistoryAdapter.ListViewHolder>() {
    val listHistoryRec = ArrayList<HistoryOnReceive>()
/*    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var addresses: List<Address>*/

    fun setDataHistoryRec(dataHistory: HistoryOnReceive) {
        listHistoryRec.add(dataHistory)
        notifyDataSetChanged()
    }

    fun clearDataHistory() {
        listHistoryRec.clear()
    }

    inner class ListViewHolder(val binding: ItemOnReceiveHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dataHistory: HistoryOnReceive) {
            //binding data with view
            binding.tvTanggalOnReceiveHistory.text = dataHistory.tgl_approve
            binding.tvNamaPenerimaOnReceiveHistory.text = dataHistory.nama_penerima
            binding.tvSatusOnReceiveHistory.text = dataHistory.status
            binding.tvLokasiPenerimaOnReceiveHistory.text = dataHistory.lokasi
            binding.tvGoldarOnReceiveHistory.text = dataHistory.gol_darah_penerima
            val firebaseStorage =
                FirebaseStorage.getInstance()
                    .getReference("profileImages/${dataHistory.foto_penerima}")
            firebaseStorage.downloadUrl.addOnCompleteListener { taskUri ->
                Glide.with(itemView.context).load(taskUri.result)
                    .into(binding.imgProfileOnReceiveHistory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val listDataReq =
            ItemOnReceiveHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ListViewHolder(listDataReq)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listRec = listHistoryRec[position]
        holder.bind(listRec)
    }

    override fun getItemCount(): Int {
        return listHistoryRec.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}