package com.android.konvalesen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PushNotification(
    val data: NotificationData,
    val to: String
):Parcelable{}
