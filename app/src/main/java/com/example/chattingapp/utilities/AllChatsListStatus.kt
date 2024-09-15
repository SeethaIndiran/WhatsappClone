package com.example.chattingapp.utilities

import com.example.chattingapp.models.ChatsList

data class AllChatsListStatus(
    val data:List<ChatsList>? = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)
