package com.android.konvalesen.pushNotification

import com.android.konvalesen.model.PushNotification
import com.android.konvalesen.pushNotification.Constant.Companion.CONTENT_TYPE
import com.android.konvalesen.pushNotification.Constant.Companion.FCM_SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {
/*
    @Headers("Authorization:key=$FCM_SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("/fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>*/
}