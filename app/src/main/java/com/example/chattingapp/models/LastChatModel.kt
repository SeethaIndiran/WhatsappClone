package com.example.chattingapp.models

data class LastChatModel (
    val id:String = "",
    val userName:String = "",
    val message:String = "",
    val time:Long = 0L,
    val isSeen:Boolean = false
)