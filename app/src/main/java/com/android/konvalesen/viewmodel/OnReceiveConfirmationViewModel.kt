package com.android.konvalesen.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.konvalesen.model.RequestDonor
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OnReceiveConfirmationViewModel:ViewModel() {
    private val userRequester = MutableLiveData<RequestDonor>()
    companion object{
        val TAG = OnReceiveConfirmationViewModel::class.java.simpleName
    }

    fun setDataReqFromFirebase(idRequester: String) {
        val db = Firebase.firestore
        val dataRequester= RequestDonor()
        db.collection("requestDonor")
            .whereEqualTo("idRequester", idRequester)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    dataRequester.idRequester = document.data["idRequester"].toString()
                    dataRequester.namaRequester = document.data["namaRequester"].toString()
                    dataRequester.alamatRequester = document.data["alamatRequester"].toString()
                    dataRequester.nomorRequester = document.data["nomorRequester"].toString()
                    dataRequester.darahRequester = document.data["darahRequester"].toString()
                    dataRequester.tanggal = document.data["tanggal"].toString()
                    dataRequester.latRequester = document.data["latRequester"] as Double?
                    dataRequester.lngRequester = document.data["lngRequester"] as Double?
                    dataRequester.status = document.data["status"].toString()
                }
                userRequester.postValue(dataRequester)
                Log.d(RequestViewModel.TAG, "setDataReqFromFirebase-dataRequester:$dataRequester ")
            }
            .addOnFailureListener { exception ->
                Log.w(RequestViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataReqFromFirebase(): MutableLiveData<RequestDonor> = userRequester
}