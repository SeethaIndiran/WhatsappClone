package com.example.chattingapp.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.chattingapp.CallsActivity
import com.example.chattingapp.firebasevideocall.service.MainServiceRepository
import com.example.chattingapp.others.Constants
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class NotificationActionReceiver:  BroadcastReceiver() {

    @Inject
    lateinit var serviceRepository:MainServiceRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_DECLINE") {
            // Handle decline action
            // Dismiss the notification here
            dismissNotification(context, intent.getIntExtra("id", 0))

        }
      else if(intent?.action == "ACTION_FCM_ANSWER"){
            val data = intent!!.getSerializableExtra("dataModel") as DataCall
            val target = data.target
            val username = data.user
            serviceRepository.startService(target)
            val answerIntent = Intent(context, CallsActivity::class.java)
            answerIntent.putExtra("dataModel",data)
            answerIntent.action = Constants.ACTION_FCM_ANSWER
            answerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(answerIntent)




        }
    }
    private  fun dismissNotification(context: Context?, notificationId: Int) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}