package com.example.chattingapp.notifications

import com.example.chattingapp.utils.AccessToken
import com.example.chattingapp.utils.GetAccessToken
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
  /*  @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA-huN9zk:APA91bHPxk46GJ_PB4gl852Ukwiv6uOe84XMHBFVCArZ_PaFEovQo-RG8ASKR0ncENefnToDJB3tsBYS3ZQ2a-qbb7ElzGJnb4B6eRwsAz1fsUPcHYo0LyivCIBGMFMyRm-jCS9eeCMV"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?*/

    // FCM v1 API for sending notifications

    @POST("v1/projects/chattingapp-39d34/messages:send")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json")
    fun sendNotification(
        @Body body: Sender,
        @Header("Authorization") authToken: String   // OAuth token
    ): Call<MyResponse>
}