package com.example.chattingapp.utilities

import com.example.chattingapp.models.Users

data class SearchUserState (
    val data:List<Users> = emptyList(),
    val error:String = "",
    val isLoading:Boolean = false
)