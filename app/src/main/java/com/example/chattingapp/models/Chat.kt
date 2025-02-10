package com.example.chattingapp.models

import java.io.Serializable

data class Chat (

    val sender:String = "",
    val message:String = "",
    val receiver:String = "",
    var isSeen:Boolean = false,
    val url:String = "",
    val messageKey:String = "",
    val time:Long = 0L,
    val clickedNum:Int = 0,
    var isSelected:Boolean = false

):Serializable

