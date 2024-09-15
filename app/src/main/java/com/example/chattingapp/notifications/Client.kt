package com.example.chattingapp.notifications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Client {

    object Client{
        private var retrofit: Retrofit? = null

        fun getCloient(url:String?):Retrofit?{
            if(retrofit == null){
                retrofit = Retrofit.Builder().baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }
}