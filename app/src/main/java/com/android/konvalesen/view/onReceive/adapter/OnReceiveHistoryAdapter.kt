package com.android.konvalesen.view.onReceive.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ItemOnReceiveHistoryBinding
import com.android.konvalesen.helper.SessionUser
import com.android.konvalesen.model.HistoryOnReceive
import java.net.URLEncoder

class OnReceiveHistoryAdapter : RecyclerView.Adapter<OnReceiveHistoryAdapter.ListViewHolder>() {
    private val listHistoryRec: MutableList<HistoryOnReceive> = ArrayList()

    fun setDataHistoryRec(dataHistory: ArrayList<HistoryOnReceive>) {
        listHistoryRec.clear()
        listHistoryRec.addAll(dataHistory)
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding: ItemOnReceiveHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dataHistory: HistoryOnReceive) {
            //binding data with view
            binding.tvTanggalOnReceiveHistory.text = dataHistory.tgl_approve
            binding.tvNamaPenerimaOnReceiveHistory.text = dataHistory.nama_penerima
            binding.tvSatusOnReceiveHistory.text = dataHistory.status
            binding.tvLokasiPenerimaOnReceiveHistory.text = dataHistory.lokasi
            binding.button.text =
                itemView.context.getString(R.string.golongan_darah) + ": " + dataHistory.gol_darah_penerima
            binding.btnHubungiLagi.setOnClickListener {
                val session = SessionUser(itemView.context)
                val nama = session.sharedPreferences.getString("nama", "").toString()
                val messege =
                    "Hai, saya $nama yang dulu pernah menerima permintaan donor dari kamu ..."
                val url =
                    "https://api.whatsapp.com/send?phone=${dataHistory.nomor_penerima}&text=${
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