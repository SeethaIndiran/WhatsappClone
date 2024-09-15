package com.example.chattingapp.notifications

import android.content.Context
import android.util.Log
import com.example.chattingapp.R
import java.io.FileInputStream
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.chattingapp.BuildConfig
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util.Date

object OAuth2Util {

    // Function to generate the access token using service account credentials
  /*  fun getAccessToken(): String {
        val credentials = GoogleCredentials
            .fromStream(FileInputStream("res/raw/service_key.jason"))
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }*/
    fun getAccessToken(context: Context): String? {

            return try {
            generateJWT()
                val jsonString = BuildConfig.JSON_DATA

                // Convert the JSON string to an InputStream
                val inputStream: InputStream = ByteArrayInputStream(jsonString.toByteArray(Charset.forName("UTF-8")))

          //  val inputStream: InputStream = context.resources.openRawResource(R.raw.service_key_new)

            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
             credentials.refresh()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AccessTokenError", "Error retrieving token: ${e.message}")
            null
        }
    }


    fun generateJWT(): String {
        val currentTimeInMillis = System.currentTimeMillis()
        val iat = Date(currentTimeInMillis)  // Issued at time
        val exp = Date(currentTimeInMillis + 60 * 60 * 1000)  // Expiration time (60 minutes later)

        val algorithm = Algorithm.HMAC256("your_secret_key")  // Use the algorithm you're using to sign the JWT

        return JWT.create()
            .withIssuer("firebase-adminsdk-9yuw0@chattingapp-39d34.iam.gserviceaccount.com")  // Firebase service account email or other identifier
            .withIssuedAt(iat)
            .withExpiresAt(exp)
            .sign(algorithm)
    }

}