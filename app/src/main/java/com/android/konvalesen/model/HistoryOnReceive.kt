package com.android.konvalesen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryOnReceive(
    var tgl_approve: String = "",
    var nama_penerima: String = "",
    var lokasi: String = "",
    var status: String = "",
    var gol_darah_penerima: String = "",
    var nomor_penerima: String = "",
) : Parcelable {}
