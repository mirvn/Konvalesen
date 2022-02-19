package com.android.konvalesen.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.konvalesen.model.ApprovedDonorData
import com.android.konvalesen.model.RequestDonor
import com.android.konvalesen.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class UserViewModel : ViewModel() {
    private val user = MutableLiveData<User>()
    private val dataSucess = MutableLiveData<User>()
    private val allUser = MutableLiveData<ArrayList<User>>()
    private val allUserRequester = MutableLiveData<ArrayList<RequestDonor>>()
    private val dataHistoryApprover = MutableLiveData<ArrayList<ApprovedDonorData>>()

    companion object {
        val TAG = UserViewModel::class.java.simpleName
    }

    fun createDataUser(data: User, context: Context) {
        val db = Firebase.firestore
        db.collection("users").document(data.id.toString())
            .set(data)
            .addOnCompleteListener {
                Log.d(TAG, "createDataUser - user created: ${it.result}")
                dataSucess.postValue(data)
            }
            .addOnFailureListener {
                Log.d(TAG, "createDataUser: $it")
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun getDataCreated(): MutableLiveData<User> = dataSucess

    fun updateDataFCMUser(docId: String, fcmToken: String, context: Context) {
        val db = Firebase.firestore
        db.collection("users").document(docId)
            .update("fcm_token", fcmToken)
            .addOnCompleteListener {
                Log.d(TAG, "updateDataFCMUser: $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "createDataUser: $it")
                //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun deeleteDataUser(docId: String, context: Context) {
        val db = Firebase.firestore
        db.collection("users").document(docId)
            .delete()
            .addOnCompleteListener { querySnapshot ->
                Log.d(TAG, "deeleteDataUser: ${querySnapshot.getResult()}")
            }
            .addOnFailureListener {
                Log.d(TAG, "createDataUser: $it")
                //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun setAllDataReqWithNumberFromFirebase(idRequester: String, nomorRequester: String) {
        val db = Firebase.firestore
        val dataRequester = ArrayList<RequestDonor>()
        db.collection("requestDonor")
            .whereEqualTo("idRequester", idRequester)
            .whereEqualTo("nomorRequester", nomorRequester)
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

    fun getAllDataReqWithNumberFromFirebase(): MutableLiveData<ArrayList<RequestDonor>> =
        allUserRequester

    fun deleteDataReq(docId: String, context: Context) {
        val db = Firebase.firestore
        db.collection("requestDonor").document(docId)
            .delete()
            .addOnCompleteListener { querySnapshot ->
                Log.d(TAG, "deleteDataReq: ${querySnapshot.getResult()}")
            }
            .addOnFailureListener {
                Log.d(TAG, "createDataUser: $it")
                //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

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
                Log.d(
                    OnReceiveConfirmationViewModel.TAG,
                    "setDataApproverFromFirebase: $dataApproverFirebase"
                )
            }
            .addOnFailureListener { exception ->
                Log.w(OnReceiveConfirmationViewModel.TAG, "Error getting documents: ", exception)
            }
    }

    fun deleteDataApprovedReq(docId: String, context: Context) {
        val db = Firebase.firestore
        db.collection("approvedReqDonor").document(docId)
            .delete()
            .addOnCompleteListener { querySnapshot ->
                Log.d(TAG, "deleteDataApprovedReq: ${querySnapshot.getResult()}")
            }
            .addOnFailureListener {
                Log.d(TAG, "createDataUser: $it")
                //Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    fun getDataHistoryApprovedFromFirebase(): MutableLiveData<ArrayList<ApprovedDonorData>> =
        dataHistoryApprover

    fun getDataUserFromFirebase(nomor: String) {
        val db = Firebase.firestore
        val dataUser = User()
        db.collection("users")
            .whereEqualTo("nomor", nomor)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    dataUser.docid = document.id
                    dataUser.id = document.data["id"].toString()
                    dataUser.nama = document.data["nama"].toString()
                    dataUser.nomor = document.data["nomor"].toString()
                    dataUser.golongan_darah = document.data["golongan_darah"].toString()
                    dataUser.fcm_token = document.data["fcm_token"].toString()
                    dataUser.foto = document.data["foto"].toString()
                }
                user.postValue(dataUser)
                /*Log.d(TAG, "getDataUserFromFirebase-dataUser: $dataUser")
                Log.d(TAG, "getDataUserFromFirebase-user: $user")*/
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getDataUser(): MutableLiveData<User> = user

    fun getAllDataUserFromFirebase() {
        val db = Firebase.firestore
        val userArray = ArrayList<User>()
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<User>()) {
                    userArray.add(document)
                }
                allUser.postValue(userArray)
                Log.d(TAG, "getDataUserFromFirebase-userArray:$userArray")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getAlldataUser(): MutableLiveData<ArrayList<User>> = allUser

    fun getAllDataUserWithIdFromFirebase(id: String) {
        val db = Firebase.firestore
        val userArray = ArrayList<User>()
        db.collection("users")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents.toObjects<User>()) {
                    userArray.add(document)
                }
                allUser.value?.clear()
                allUser.postValue(userArray)
                Log.d(TAG, "getDataUserFromFirebase-userArrayWithid:$userArray")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun getAlldataUserWithId(): MutableLiveData<ArrayList<User>> = allUser
}