package com.android.konvalesen.helper

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.konvalesen.R
import com.android.konvalesen.model.HistoryOnReceive

class SessionUser(context: Context) {
    //Menyimpan data Shared Preference
    /*
     val editor = sharedPref.edit()
     editor.putInt(getString(R.string.saved_high_score), newHighScore)
     editor.commit() / .apply()
     */
    /*fun getUsename() {
        sharedPreferences.getString("email","")
    }*/

    var masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    var sharedPreferences = EncryptedSharedPreferences.create(
        context,
        R.string.session_encrypted_user.toString(),
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val historyOnReceive = ArrayList<HistoryOnReceive>().apply {
        ensureCapacity(1)
    }

    var editor = sharedPreferences.edit()

    fun setUserId(id: String) {
        editor.putString("id", id)
        editor.apply()
    }

    fun setVerificationId(verificationId: String) {
        editor.putString("verificationId", verificationId)
        editor.apply()
    }

    fun setUserName(nama: String) {
        editor.putString("nama", nama)
        editor.apply()
    }

    fun setUserNomor(nomor: String) {
        editor.putString("nomor", nomor)
        editor.apply()
    }

    fun setUserGolonganDarah(golonganDarah: String) {
        editor.putString("golonganDarah", golonganDarah)
        editor.apply()
    }

    fun setFcmToken(fcmToken: String) {
        editor.putString("fcmToken", fcmToken)
        editor.apply()
    }

    fun setFoto(foto: String) {
        editor.putString("foto", foto)
        editor.apply()
    }
}