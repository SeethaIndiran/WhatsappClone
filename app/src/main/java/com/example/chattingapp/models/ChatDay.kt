package com.example.chattingapp.models

data class ChatDay (
    val date: Long = 0L,
    var chats:List<Chat> = emptyList()
){

}