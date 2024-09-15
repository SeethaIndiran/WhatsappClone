package com.example.chattingapp.utils

import com.example.chattingapp.models.Users
import com.example.chattingapp.notifications.DataCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(private val gson:Gson) {

    private val db = FirebaseFirestore.getInstance()
   // private  var username = FirebaseAuth.getInstance().currentUser!!.uid
    private var username:String?=null


    fun setUsername(name:String){
        this.username = name
    }

    fun sendMessageToOtherClient(message: DataCall, success:(Boolean) -> Unit){
       // val convertedMessage = gson.toJson(message.copy(user = username))
        val eventHashMap  =HashMap<String,Any>()
        eventHashMap["event"] = message.type
        eventHashMap["user"] = message.type
        db.collection("Users").document(message.target).update("event",message.type,
            "data",message.body)
            .addOnCompleteListener {
                success(true)
            }.addOnFailureListener {
                success(false)
            }
    }



    fun subscribeForLatestEvent(listener: Listener,username: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("Users").document(username!!)

            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val event = try {
                        snapshot.toObject(Users::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

                        val type = event!!.event
                        val data = event.data
                        if (type != null && data!= null) {
                            listener.onLatestEventReceived(type, data)
                        }else{
                            listener.onLatestEventReceived("null","null")
                        }




                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun logOff(function:()->Unit) {

    }

    fun clearLatestEvent(username: String){
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(username).update("event",null)
     //   db.collection("Users").document(username).update("status",null)

    }

    fun clearLatestStatus(username: String){
        val db = FirebaseFirestore.getInstance()
       // db.collection("Users").document(username).update("event",null)
        db.collection("Users").document(username).update("status",null)
    //    db.collection("Users").document(username).update("event",null)
        db.collection("Users").document(username).update("data",null)
    }


    interface Listener {
        fun onLatestEventReceived(type: String,data:String)

    }
}