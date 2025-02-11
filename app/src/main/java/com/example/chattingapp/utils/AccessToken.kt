package com.example.chattingapp.utils

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Properties

object AccessToken {

   // private val firebaseMessageingScope = "https://www.googleapis.com/auth/firebase.messaging"
   private val firebaseMessagingScope: String = "https://www.googleapis.com/auth/firebase.messaging"

    private fun getSecretKey(): String? {
        return try {
            val properties = Properties()
            val inputStream: InputStream =
                javaClass.classLoader?.getResourceAsStream("local.properties") ?: return null
            properties.load(inputStream)
            properties.getProperty("FIREBASE_SECRET_KEY")
        } catch (e: Exception) {
            Log.e("AccessToken", "Error loading secret key: ${e.localizedMessage}")
            null
        }
    }

    fun getAccessToken(): String? {
        return try {
            val jsonString = getSecretKey() ?: return null
            val stream: InputStream =
                ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials =
                GoogleCredentials.fromStream(stream).createScoped(listOf(firebaseMessagingScope))
            googleCredentials.refresh()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("AccessToken", "getAccessToken: ${e.localizedMessage}")
            null
        }
    }

}