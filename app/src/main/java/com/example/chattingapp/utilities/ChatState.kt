package com.example.chattingapp.utilities

import com.example.chattingapp.models.ChatDay

data class ChatState(
    val data:List<ChatDay>? = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)