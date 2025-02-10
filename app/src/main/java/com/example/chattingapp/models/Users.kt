package com.example.chattingapp.models

import android.net.Uri
import java.io.Serializable

data class Users(
    val cover:String="",
    val email:String="",
    val  facebook:String="",
    val  instagram:String="",
    val  password:String="",
    val  profile: String="",
    val  search:String="",
    val  status:String="",
    var uid:String="",
    var username:String="",
    val website:String="",
    val event:String? = "",
    val data:String? = ""
):Serializable