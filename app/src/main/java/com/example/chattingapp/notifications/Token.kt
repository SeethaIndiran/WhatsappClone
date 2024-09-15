package com.example.chattingapp.notifications

class Token {

    private  var token:String = ""



    constructor(token:String){
        this.token = token
    }

    fun getToken():String{
        return token
    }

    fun setToken(token:String){
        this.token = token
    }
}