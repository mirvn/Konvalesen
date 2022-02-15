package com.android.konvalesen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val title: String,
    val message: String
) : Parcelable {}
