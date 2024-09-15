package com.example.chattingapp.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFirebaseInstanceId:FirebaseMessagingService() {

    private  lateinit var refreshToken:String

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
                result->
            result?.let {
                refreshToken = result
                if(firebaseUser!= null){
                    updateToken(refreshToken)
                    Log.i("token",refreshToken)
                }
            }
        }


    }

    private fun updateToken(refreshToken: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val token = Token(refreshToken!!)

        ref.document(firebaseUser!!.uid).set(token)
    }
}