package com.android.konvalesen.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.model.RequestDonor
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class OnReceiveConfirmationViewModel:ViewModel() {
    private val userRequester = MutableLiveData<RequestDonor>()
    private val allUserRequester = MutableLiveData<ArrayList<RequestDonor>>()
    private val dataApprover = MutableLiveData<ApprovedDonorData>()
    private val dataHistoryApprover = MutableLiveData<ArrayList<ApprovedDonorData>>()

    companion object {
        val TAG = OnReceiveConfirmationViewModel::class.java.simpleName
    }

    fun updateDataApprovedToDoneFirebase(docId: String, status: String) {
        val db = Firebase.firestore
        db.collection("approvedReqDonor").document(docId)
            .update("status", status)
            .addOnCompleteListener {
                Log.d(TAG, "updateDataApprovedToDoneFirebase: $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "updateDataApprovedToDoneFirebase: $it")
                //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun setDataHistoryApproveWithIdReqFromFirebase(idRequester: String, status: String) {
        val db = Firebase.firestore
        val dataApproverFirebase = ArrayList<ApprovedDonorData>()
        db.collection("approvedReqDonor")
            .whereEqualTo("idRequester", idRequester)
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<ApprovedDonorData>()) {
                    dataApproverFirebase.add(document)
                }
                dataHistoryApprover.postValue(dataApproverFirebase)
                Log.d(TAG, "setDataApproverFromFirebase: $dataApproverFirebase")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataHistoryApproveWithIdReqFromFirebase(): MutableLiveData<ArrayList<ApprovedDonorData>> =
        dataHistoryApprover

    fun setDataHistoryApprovedFromFirebase(nomorApprover: String) {
        val db = Firebase.firestore
        val dataApproverFirebase = ArrayList<ApprovedDonorData>()
        db.collection("approvedReqDonor")
            .whereEqualTo("nomorApprover", nomorApprover)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<ApprovedDonorData>()) {
                    dataApproverFirebase.add(document)
                }
                dataHistoryApprover.postValue(dataApproverFirebase)
                Log.d(TAG, "setDataApproverFromFirebase: $dataApproverFirebase")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataHistoryApprovedFromFirebase(): MutableLiveData<ArrayList<ApprovedDonorData>> =
        dataHistoryApprover

    fun setDataApprovedFromFirebase(nomorApprover: String, status: String) {
        val db = Firebase.firestore
        val dataApproverFirebase = ApprovedDonorData()
        db.collection("approvedReqDonor")
            .whereEqualTo("nomorApprover", nomorApprover)
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    dataApproverFirebase.docId = document.id
                    dataApproverFirebase.idApprover = document.data["idApprover"].toString()
                    dataApproverFirebase.idRequester = document.data["idRequester"].toString()
                    dataApproverFirebase.jarakApprover = document.data["jarakApprover"].toString()
                    dataApproverFirebase.namaApprover = document.data["namaApprover"].toString()
                    dataApproverFirebase.nomorApprover = document.data["nomorApprover"].toString()
                    dataApproverFirebase.status = document.data["status"].toString()
                }
                dataApprover.postValue(dataApproverFirebase)
                Log.d(TAG, "setDataApproverFromFirebase: $dataApproverFirebase")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataApprovedFromFirebase(): MutableLiveData<ApprovedDonorData> = dataApprover

    fun createNewApprovedReq(data: ApprovedDonorData, context: Context) {
        val db = Firebase.firestore
        db.collection("approvedReqDonor")
            .add(data)
            .addOnCompleteListener {
                Log.d(TAG, "createNewRequestDonor $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "createNewRequestDonor: $it")
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun setDataReqFromFirebase(idRequester: String, status: String) {
        val db = Firebase.firestore
        val dataRequester = RequestDonor()
        db.collection("requestDonor")
            .whereEqualTo("idRequester", idRequester)
            .whereEqualTo("status", status)
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

    fun setAllDataReqWithIdFromFirebase(idRequester: String, status: String) {
        val db = Firebase.firestore
        val dataRequester = ArrayList<RequestDonor>()
        db.collection("requestDonor")
            .whereEqualTo("idRequester", idRequester)
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<RequestDonor>()) {
                    dataRequester.add(document)
                }
                allUserRequester.postValue(dataRequester)
                Log.d(RequestViewModel.TAG, "setDataReqFromFirebase-dataRequester:$dataRequester ")
            }
            .addOnFailureListener { exception ->
                Log.w(RequestViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    fun getAllDataReqWithIdFromFirebase(): MutableLiveData<ArrayList<RequestDonor>> =
        allUserRequester
}