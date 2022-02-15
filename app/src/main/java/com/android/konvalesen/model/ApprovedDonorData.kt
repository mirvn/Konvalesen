package com.android.konvalesen.model

data class ApprovedDonorData(
    var docId: String? = null,
    var docIdRequester: String? = null,
    var idApprover: String? = null,
    var idRequester: String? = null,
    var namaApprover: String? = null,
    var nomorApprover: String? = null,
    var jarakApprover: String? = null,
    var tanggalApprove: String? = null,
    // var tanggalSelesai: String? = null,
    var status: String? = null
) {}
