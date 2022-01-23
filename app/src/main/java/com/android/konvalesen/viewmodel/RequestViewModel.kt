package com.android.konvalesen.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.konvalesen.model.NotificationData
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.pushNotification.Constant
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONObject


class RequestViewModel: ViewModel() {
    private val user = MutableLiveData<RequestDonor>()
    private val userRequester = MutableLiveData<RequestDonor>()
    private val allUserRequester = MutableLiveData<ArrayList<RequestDonor>>()
    companion object{
        val TAG = RequestViewModel::class.java.simpleName
    }

    fun createNewRequestDonor(data: RequestDonor, context: Context) {
        val db = Firebase.firestore
        db.collection("requestDonor")
            .add(data)
            .addOnCompleteListener {
                Log.d(TAG, "createNewRequestDonor $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "createNewRequestDonor: $it")
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun setAllDataReqFromFirebase(idRequester: String) {
        val db = Firebase.firestore
        val dataRequester = ArrayList<RequestDonor>()
        db.collection("requestDonor")
            .whereEqualTo("idRequester", idRequester)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<RequestDonor>()) {
                    dataRequester.add(document)
                }
                allUserRequester.postValue(dataRequester)
                Log.d(TAG, "setDataReqFromFirebase-dataRequester:$dataRequester ")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getAllDataReqFromFirebase(): MutableLiveData<ArrayList<RequestDonor>> = allUserRequester

    fun setAllDataReqWithStatusFromFirebase(status: String, darahRequester: String) {
        val db = Firebase.firestore
        val dataRequester = ArrayList<RequestDonor>()
        db.collection("requestDonor")
            .whereEqualTo("status", status)
            .whereEqualTo("darahRequester", darahRequester)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<RequestDonor>()) {
                    dataRequester.add(document)
                }
                allUserRequester.postValue(dataRequester)
                Log.d(TAG, "setDataReqFromFirebase-dataRequester:$dataRequester ")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getAllDataReqWithStatusFromFirebase(): MutableLiveData<ArrayList<RequestDonor>> =
        allUserRequester

    fun updateStatusRequestDonor(docId: String, context: Context, status: String) {
        val db = Firebase.firestore
        db.collection("requestDonor").document(docId)
            .update(
                "status", status,
                "idDoc", docId
            )
            .addOnCompleteListener {
                Log.d(TAG, "createNewRequestDonor $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "createNewRequestDonor: $it")
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun setDataReqFromFirebase(nomorRequester: String, status: String) {
        val db = Firebase.firestore
        val dataRequester = RequestDonor()
        db.collection("requestDonor")
            .whereEqualTo("nomorRequester", nomorRequester)
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    dataRequester.idDoc = document.id
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
                Log.d(TAG, "setDataReqFromFirebase-dataRequester:$dataRequester ")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataReqFromFirebase(): MutableLiveData<RequestDonor> = userRequester

    fun sendNotification(to:String, message:NotificationData,context: Context) {
        val url = Constant.BASE_URL

        val notificationParams = JSONObject()
        notificationParams.put("title", message.title)
        notificationParams.put("body", message.message)

        val sendNotification = JSONObject()
        sendNotification.put("to",to)
        sendNotification.put("notification",notificationParams)

        val params = StringEntity(sendNotification.toString())
        params.setContentType("application/json")

        Log.d(TAG, "sendNotification: $params")

        val client = AsyncHttpClient()
        client.addHeader("Authorization", "key="+Constant.FCM_SERVER_KEY)
        //client.addHeader("Content-Type", Constant.CONTENT_TYPE)
        client.post(context,url,params,Constant.CONTENT_TYPE,object :AsyncHttpResponseHandler(){
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                Log.d(TAG, "onSuccessNotification: $responseBody")
                Toast.makeText(context, "Permintaan Bantuan Berhasil", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.e(TAG, "onFailureNotification: ${error.toString()}", )
                Toast.makeText(context, "Permintaan Bantuan Gagal", Toast.LENGTH_SHORT).show()
            }

        })
    }
}