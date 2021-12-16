package com.android.konvalesen.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.android.konvalesen.R
import com.android.konvalesen.view.onRequest.OnRequestActivity
import com.android.konvalesen.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

    private const val CHANNEL_ID = "channel_id"
class FirebaseService: FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        val sessionUser = SessionUser(this)
        sessionUser.setFcmToken(newToken)
        updateFcmTokenToServer(newToken)
    }

    private fun updateFcmTokenToServer(newToken: String){
        val auth = FirebaseAuth.getInstance()
        val userViewModel = ViewModelProvider(ViewModelStore(), ViewModelProvider.NewInstanceFactory())
            .get(UserViewModel::class.java)
        userViewModel.updateDataFCMUser(auth.currentUser?.uid.toString(),newToken,this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val intent = Intent(this, OnRequestActivity::class.java) // activity diganti di onRecieveNotificationActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            FLAG_ONE_SHOT
        )
        val notificationId = Random.nextInt()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}