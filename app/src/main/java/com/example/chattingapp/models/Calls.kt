package com.example.chattingapp.models

import java.io.Serializable

data class Calls (
    val sender:String="",
    val receiver:String="",
    val senderName:String="",
    val receiverName:String = "",
    val type:String="",
    val time:Long=0L,
    var callKey:String=""
):Serializable