package com.android.konvalesen.view.onRequest.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.konvalesen.R
import com.android.konvalesen.databinding.ItemOnReqHistoryBinding
import com.android.konvalesen.model.RequestDonor

class OnRequestHistoryAdapter : RecyclerView.Adapter<OnRequestHistoryAdapter.ListViewHolder>() {
    val listHistoryReq = ArrayList<RequestDonor>()

    fun setDataHistoryReq(dataHistory: ArrayList<RequestDonor>) {
        listHistoryReq.clear()
        listHistoryReq.addAll(dataHistory)
        notifyDataSetChanged()
    }

    inner class ListViewHolder(val binding: ItemOnReqHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dataHistory: RequestDonor) {
            //binding data with view
            binding.tvSatusOnReqHistory.text = dataHistory.status.toString()
            binding.tvTanggalOnReqHistory.text = dataHistory.tanggal.toString()
            binding.btnGoldar.text =
                itemView.context.getString(R.string.golongan_darah) + ": " + dataHistory.darahRequester.toString()
            binding.tvLokasiOnReqHistory.text = dataHistory.alamatRequester.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val listDataReq =
            ItemOnReqHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ListViewHolder(listDataReq)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listReq = listHistoryReq[position]
        holder.bind(listReq)
    }

    override fun getItemCount(): Int {
        return listHistoryReq.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}