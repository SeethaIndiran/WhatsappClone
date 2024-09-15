package com.example.chattingapp.utilities

import com.example.chattingapp.models.Status

data class CurrentUserStatus(
    val status: Status?= Status("","",false,0L, emptyList())

)
