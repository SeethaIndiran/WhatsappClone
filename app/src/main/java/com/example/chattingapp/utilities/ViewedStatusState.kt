package com.example.chattingapp.utilities

import com.example.chattingapp.models.Status

data class ViewedStatusState (
    val data:List<Status>? = emptyList(),
    val error:String = "",
    val isLoading:Boolean=false
)