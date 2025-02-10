package com.example.chattingapp.utils

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object AccessToken {

   // private val firebaseMessageingScope = "https://www.googleapis.com/auth/firebase.messaging"
   private val firebaseMessagingScope: String? = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken(): String? {
        return try {
            val jsonString = """{
  "type": "service_account",
  "project_id": "your_project_id",
  "private_key_id": "your_private_key_id",
  "private_key": "your_private_key",
  "client_email": "your_client_email",
  "client_id": "your_client_id",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x_cert_url": "your_client_x_cert_url",
  "universe_domain": "googleapis.com"
}"""
            val stream: InputStream =
                ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials =
                GoogleCredentials.fromStream(stream).createScoped(arrayListOf(firebaseMessagingScope))
            googleCredentials.refresh()
          googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("AccessToken", "getAccessToken: " + e.localizedMessage)
            null
        }
    }

}