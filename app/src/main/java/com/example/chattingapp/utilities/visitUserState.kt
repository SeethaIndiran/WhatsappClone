package com.example.chattingapp.utilities

import com.example.chattingapp.models.Users

data class visitUserState(
    val userVisit: Users = Users("","","","","","","","","","",""),
    val image:String = "",
    val error:String = ""
)
