package com.example.chattingapp.utilities

sealed class Screen<T>(val data:T?=null, val message:String?=null) {

    class Success<T>(data: T? = null) : Screen<T>(data)

    class Loading<T>(data: T? = null) : Screen<T>(data)

    class Error<T>(message: String, data: T? = null) : Screen<T>(data, message)
}
