package com.example.chattingapp.notifications

import java.io.Serializable

data class DataCall (
    val user:String="",
    val icon:Int = 0,
    val body:String = "",
    val title:String = "",
    val target:String="",
    val type:String = ""
):Serializable