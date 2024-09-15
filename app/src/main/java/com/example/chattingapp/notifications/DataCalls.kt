package com.example.chattingapp.notifications

import java.io.Serializable


 class DataCalls:Serializable {
     private  var user:String? = null
     private  var icon = 0
     private  var body:String? = null
     private  var title:String? = null
     private  var sented:String? = null
    private var type:String? = null
     private var timeStamp:Long? = null


     constructor(){}
     constructor(user:String , icon:Int, body:String, title:String, sented:String,type:String,timeStamp:Long){
         this.user = user
         this.icon = icon
         this.body = body
         this.title = title
         this.sented = sented
         this.type = type
         this.timeStamp = timeStamp
     }

     fun getUser():String?{
         return  user
     }

     fun setUser(user:String){
         this.user = user
     }

     fun getIcon():Int?{
         return  icon
     }

     fun setIcon(icon:Int){
         this.icon = icon
     }

     fun getBody():String?{
         return  body
     }

     fun setBody(body:String){
         this.body = body
     }

     fun getTitle():String?{
         return  title
     }

     fun setTitle(title:String){
         this.title = title
     }

     fun getSented():String?{
         return  sented
     }

     fun setSented(sented:String){
         this.sented = sented
     }
     fun getType():String?{
         return  type
     }

     fun setType(type:String){
         this.type = type
     }
}