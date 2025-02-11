package com.example.chattingapp.notifications

import android.annotation.SuppressLint
import android.app.Notification.*
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.chattingapp.CallsActivity
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.firebasevideocall.service.MainServiceRepository
import com.example.chattingapp.others.Constants.Companion.ACTION_DECLINE
import com.example.chattingapp.others.Constants.Companion.ACTION_FCM
import com.example.chattingapp.others.Constants.Companion.ACTION_FCM_ANSWER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class MyFirebaseMessaging:FirebaseMessagingService() {

private var serviceRepository:MainServiceRepository? = null
    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)

        val target = mRemoteMessage.data["target"]

        val user = mRemoteMessage.data["user"]

        val sharedPrefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        val currentOnlineUser = sharedPrefs.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser!=null && target==firebaseUser.uid){

            if(currentOnlineUser != user){

                val msgType = mRemoteMessage.data["type"]
                if(msgType == "text"){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                        sendOreoNotification(mRemoteMessage)
                    }else{
                        sendNotification(mRemoteMessage)
                    }
                }else if(msgType == "start_video_call" || msgType =="start_audio_call"){

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                        sendOreoNotificationForVideoCall(mRemoteMessage)

                    }else{
                        sendNotificationForVideoCall(mRemoteMessage)
                    }

                }else{

                }

            }
        }

    }

    private fun sendNotification(mRemoteMessage: RemoteMessage) {

        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val j= user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("id",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,j,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if(j>0){
            i = j
        }
        noti.notify(i,builder.build())

    }

    private fun sendOreoNotification(mRemoteMessage: RemoteMessage) {

        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        val j= user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this,MessageChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("id",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,j,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder: Builder = oreoNotification.getOreoNotification(title,body,pendingIntent,defaultSound,icon)
        var i = 0
        if(j>0){
            i = j
        }
        oreoNotification.getManager!!.notify(i,builder.build())
    }


    private fun sendNotificationForVideoCall(mRemoteMessage: RemoteMessage) {

        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]
        val target = mRemoteMessage.data["target"]
        val type = mRemoteMessage.data["type"]
        val time = mRemoteMessage.data["timeStamp"]

  val data = DataCall(user!!,icon!!,body!!,title!!,target!!,type!!)

        val notification = mRemoteMessage.notification
        val j= user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, CallsActivity::class.java)

        val bundle = Bundle()
       // bundle.putSerializable("dataModel",data)
        intent.putExtra("dataModel",data)
        intent.action = "ACTION_FCM"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,j,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val answerIntent = Intent(this, NotificationActionReceiver::class.java)
        answerIntent.putExtra("dataModel",data)
        answerIntent.action = ACTION_FCM_ANSWER
        answerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val answerPendingIntent = PendingIntent.getActivity(this, 0,
            answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val declineIntent = Intent(this, NotificationActionReceiver::class.java)
        declineIntent.action = ACTION_DECLINE
        declineIntent.putExtra("id",j)
        declineIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val declinePendingIntent = PendingIntent.getBroadcast(this, 0,
            declineIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val remoteView = RemoteViews("com.example.chattingapp",R.layout.notification_layout)
        val remoteViewExpanded = RemoteViews("com.example.chattingapp",R.layout.noti_layout_two)

        remoteViewExpanded.setOnClickPendingIntent(R.id.accept_btn, answerPendingIntent)
        remoteViewExpanded.setOnClickPendingIntent(R.id.decline_btn, declinePendingIntent)

        val builder =
            NotificationCompat.Builder(this)
                .setSmallIcon(icon!!.toInt())
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteView)
                .setCustomBigContentView(remoteViewExpanded)
                .setAutoCancel(true)


        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if(j>0){
            i = j
        }
        noti.notify(i,builder.build())

    }

    private fun sendOreoNotificationForVideoCall(mRemoteMessage: RemoteMessage) {



        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val body = mRemoteMessage.data["body"]
        val title = mRemoteMessage.data["title"]
        val target = mRemoteMessage.data["target"]
        val type = mRemoteMessage.data["type"]
        val time = mRemoteMessage.data["timeStamp"]

        val data = DataCall(user!!,
            icon!!,
            body!!,
            title!!,
            target!!,
            type!!)


        val notification = mRemoteMessage.notification
        val j= user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, CallsActivity::class.java)

        val bundle = Bundle()
      //  bundle.putSerializable("dataModel",data)
        intent.putExtra("dataModel",data)
        intent.action = ACTION_FCM
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val answerIntent = Intent(this, NotificationActionReceiver::class.java)
        answerIntent.putExtra("dataModel",data)
        answerIntent.putExtra("id",j)
        answerIntent.action = ACTION_FCM_ANSWER
        answerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val answerPendingIntent = PendingIntent.getBroadcast(this, 0,
            answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val declineIntent = Intent(this, NotificationActionReceiver::class.java)
        declineIntent.action = ACTION_DECLINE
        declineIntent.putExtra("id",j)
        declineIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val declinePendingIntent = PendingIntent.getBroadcast(this, 1,
            declineIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val pendingIntent = PendingIntent.getActivity(this,j,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val remoteView = RemoteViews("com.example.chattingapp",R.layout.notification_layout)
        val remoteViewExpanded = RemoteViews("com.example.chattingapp",R.layout.noti_layout_two)



        remoteViewExpanded.setOnClickPendingIntent(R.id.accept_btn, answerPendingIntent)
        remoteViewExpanded.setOnClickPendingIntent(R.id.decline_btn, declinePendingIntent)

        val oreoNotification = OreoNotification(this)
        val builder: Builder =
            oreoNotification.getOreoNotificationForCalls(title,body,pendingIntent,remoteView,remoteViewExpanded,answerPendingIntent,declinePendingIntent,defaultSound,icon)

        var i = 0
        if(j>0){
            i = j
        }
        oreoNotification.getManager!!.notify(i,builder.build())
    }
}