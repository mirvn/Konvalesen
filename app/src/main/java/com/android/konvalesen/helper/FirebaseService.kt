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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.android.konvalesen.R
import com.android.konvalesen.view.onReceive.OnGoingReceiveFragment
import com.android.konvalesen.view.onReceive.OnReceiveConfirmationActivity
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
        val intent = Intent(this, OnReceiveConfirmationActivity::class.java)

        val uidSender = message.notification?.body.toString().substringAfterLast('.')
        intent.putExtra(OnReceiveConfirmationActivity.UID_EXTRA,uidSender)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            FLAG_ONE_SHOT
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val data =  message.notification?.body.toString().substringBeforeLast('.')
        Log.d("TAG", "onMessageReceived: $data")
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
            .setContentTitle(message.notification?.title.toString())
            .setContentText(data)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data))
            .build()

        notificationManager.notify(notificationId, notificationBuilder)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ChannelTitle",
            IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            lightColor=(Color.GREEN)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}