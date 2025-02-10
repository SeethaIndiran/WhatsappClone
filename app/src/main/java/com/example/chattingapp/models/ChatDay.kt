package com.example.chattingapp.models

data class ChatDay(
    val chatDay: Long = 0L,
    var chats: List<Chat> = emptyList()
){

}