package com.android.konvalesen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestDonorWithPhoto(
    var idDoc: String? = null,
    var idRequester: String? = null,
    var fotoRequester: String? = null,
    var namaRequester: String? = null,
    var nomorRequester: String? = null,
    var darahRequester: String? = null,
    var alamatRequester: String? = null,
    var latRequester: Double? = null,
    var lngRequester: Double? = null,
    var tanggal: String? = null,
    //  var fcmTokenRequester: String? = null,
    var status: String? = null
) : Parcelable
