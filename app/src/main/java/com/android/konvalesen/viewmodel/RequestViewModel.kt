package com.android.konvalesen.viewmodel

import android.content.Context
import android.icu.text.CaseMap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.konvalesen.model.NotificationData
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.pushNotification.Constant
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONObject
import android.os.AsyncTask
import okhttp3.OkHttpClient
import java.lang.Exception


class RequestViewModel: ViewModel() {
    private val user = MutableLiveData<RequestDonor>()
    companion object{
        val TAG = RequestViewModel::class.java.simpleName
    }

    fun createNewRequestDonor(data: RequestDonor, context: Context){
        val db = Firebase.firestore
        db.collection("requestDonor").document(data.idRequester.toString())
            .set(data)
            .addOnCompleteListener {
                Log.d(TAG, "createNewRequestDonor $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "createNewRequestDonor: $it")
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendNotification(to:String, message:NotificationData,context: Context) {
        val url = "https://fcm.googleapis.com/fcm/send"

        val notificationParams = JSONObject()
        notificationParams.put("body", message.message)
        notificationParams.put("title", message.title)
        val sendNotification = JSONObject()
        sendNotification.put("to",to)
        sendNotification.put("data",notificationParams)

        val params = StringEntity(sendNotification.toString())
        params.setContentType("application/json")

        val client = AsyncHttpClient()
        client.addHeader("Authorization", "key=${Constant.FCM_SERVER_KEY}")
        //client.addHeader("Content-Type", Constant.CONTENT_TYPE)
        client.post(null,url,params,"application/json",object :AsyncHttpResponseHandler(){
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                Log.d(TAG, "onSuccess: ${responseBody.toString()}")
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.e(TAG, "onFailure: ${error.toString()}", )
            }

        })
    }
}