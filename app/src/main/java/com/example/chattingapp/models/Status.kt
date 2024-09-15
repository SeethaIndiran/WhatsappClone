package com.example.chattingapp.models

import java.io.Serializable

data class Status(
    val userId:String= "",
    val userName:String="",
    var clicked:Boolean = false,
    val time:Long = System.currentTimeMillis(),
    val imageUrls:List<String> = emptyList(),
    val viewedUsers:List<String> = emptyList()
):Serializable
