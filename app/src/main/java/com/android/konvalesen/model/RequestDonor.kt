package com.android.konvalesen.model

data class RequestDonor(
    var idRequester: String? = null,
    var namaRequester: String? = null,
    var nomorRequester: String? = null,
    var darahRequester: String? = null,
    var alamatRequester: String? = null,
    var latRequester: Double? = null,
    var lngRequester: Double? = null,
    var tanggal: String? = null,
  //  var fcmTokenRequester: String? = null,
    var status: String? = null, // 0=onRequest, 1=Success
){
}
