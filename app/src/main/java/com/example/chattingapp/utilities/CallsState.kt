package com.example.chattingapp.utilities

import com.example.chattingapp.models.Calls

data class CallsState(
    val data:List<Calls>? = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)