package com.example.chattingapp.firebasevideocall.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.chattingapp.CallsActivity
import com.example.chattingapp.R
import com.example.chattingapp.notifications.DataCall
import com.example.chattingapp.others.Constants.Companion.ACTION_OVERLAY
import com.example.chattingapp.others.OverlayManager
import com.example.chattingapp.utils.FirebaseRepository
import com.example.chattingapp.webrtc.RTCAudioManager
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject


@AndroidEntryPoint
class MainService:Service(), FirebaseRepository.Listener {

    private var isServiceRunning = false
    private var userName:String? = null
    private lateinit var notificationManager:NotificationManager
    private lateinit var rtcAudioManager: RTCAudioManager

    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager
    private lateinit var overlayParams: WindowManager.LayoutParams
    private lateinit var overlayManager: OverlayManager

    private val TAG = "MainService"
   // private var target:String?= null
   private var isPreviousCallStateVideo = true
    private var isVideoCall = true
    private var isCaller = false
    private var target:String? = null

    private val handler = Handler()
    private var secondsElapsed = 0
    private var isRunning = false

    @Inject
    lateinit var mainRepository: FirebaseRepository

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository

    companion object{
        var listener: Listeners? = null
        var endCallListener:EndCallListener?=null
        var localSurfaceView:SurfaceViewRenderer? = null
        var remoteSurfaceView:SurfaceViewRenderer? = null
        var screenPermissionIntent : Intent?=null

    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayManager = OverlayManager(this)
        rtcAudioManager = RTCAudioManager.create(this)
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        notificationManager = getSystemService(NotificationManager::class.java)
     //  startServiceWithNotification()
      //  endCallAndRestartRepository()
   //  handleStopService()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { incomingIntent ->
            when(incomingIntent.action){
                MainServiceActions.START_SERVICE.name -> handleStartService(incomingIntent)
                MainServiceActions.SETUP_VIEWS.name -> handleSetUpViews(incomingIntent)
                MainServiceActions.END_CALL.name -> handleEndCall()
                MainServiceActions.SWITCH_CAMERA.name -> handleSwitchCamera()
                MainServiceActions.TOGGLE_AUDIO.name -> handleToggleAudio(incomingIntent)
                MainServiceActions.TOGGLE_VIDEO.name -> handleToggleVideo(incomingIntent)
                MainServiceActions.TOGGLE_AUDIO_DEVICE.name -> handleToggleAudioDevice(incomingIntent)
               MainServiceActions.TOGGLE_SCREEN_SHARE.name -> handleToggleScreenShare(incomingIntent)
                MainServiceActions.STOP_SERVICE.name -> handleStopService()
                MainServiceActions.SHOW_OVERLAY.name -> showOverlay()
                MainServiceActions.HIDE_OVERLAY.name -> hideOverlay()
              else->Unit
            }
        }
        return START_STICKY
    }

    private fun handleStopService() {
        mainRepository.endCall()
        mainRepository.logOff {
            isServiceRunning = false
            stopTimer()
            stopForeground(true)  // Remove the notification
            notificationManager.cancelAll()
            stopSelf()


        }
    }

    private fun handleToggleScreenShare(incomingIntent: Intent) {
        val isStarting = incomingIntent.getBooleanExtra("isStarting",true)
        if (isStarting){
            // we should start screen share
            //but we have to keep it in mind that we first should remove the camera streaming first
            if (isPreviousCallStateVideo){
                mainRepository.toggleVideo(true)
            }
            mainRepository.setScreenCaptureIntent(screenPermissionIntent!!)
            mainRepository.toggleScreenShare(true)

        }else{
            //we should stop screen share and check if camera streaming was on so we should make it on back again
            mainRepository.toggleScreenShare(false)
            if (isPreviousCallStateVideo){
                mainRepository.toggleVideo(false)
            }
        }
    }


    private fun handleToggleAudioDevice(incomingIntent: Intent) {
        val type = when(incomingIntent.getStringExtra("type")){
            RTCAudioManager.AudioDevice.EARPIECE.name -> RTCAudioManager.AudioDevice.EARPIECE
            RTCAudioManager.AudioDevice.SPEAKER_PHONE.name -> RTCAudioManager.AudioDevice.SPEAKER_PHONE
            else -> null
        }

        type?.let {
            rtcAudioManager.setDefaultAudioDevice(it)
            rtcAudioManager.selectAudioDevice(it)
            Log.d(TAG, "handleToggleAudioDevice: $it")
        }


    }


    private fun handleToggleVideo(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted",true)
       // this.isPreviousCallStateVideo = !shouldBeMuted
        mainRepository.toggleVideo(shouldBeMuted)
    }

    private fun handleToggleAudio(incomingIntent: Intent) {
        val shouldBeMuted = incomingIntent.getBooleanExtra("shouldBeMuted",true)
        mainRepository.toggleAudio(shouldBeMuted)
    }

    private fun handleSwitchCamera(){
        mainRepository.switchCamera()
    }

    private fun handleEndCall() {
        //1. we have to send a signal to other peer that call is ended
        mainRepository.sendEndCall(userName!!)
        //2.end out call process and restart our webrtc client
        endCallAndRestartRepository()

    }

    private fun endCallAndRestartRepository(){
        mainRepository.endCall()
        endCallListener?.onCallEnded()
       handleStopService()
       mainRepository.initWebrtcClient(userName!!)

    }

    private fun handleSetUpViews(incomingIntent: Intent) {
        isCaller = incomingIntent.getBooleanExtra("isCaller", false)
        isVideoCall = incomingIntent.getBooleanExtra("isVideoCall", true)
        target = incomingIntent.getStringExtra("target")
        mainRepository.setTarget(target!!)
        this.isPreviousCallStateVideo = isVideoCall


        //start the video call

    mainRepository.initLocalSurfaceView(localSurfaceView!!, isVideoCall)
    mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)

        if(!isCaller)

        {
            mainRepository.startCall()
        }


    }


    @SuppressLint("SuspiciousIndentation")
    private fun handleStartService(incomingIntent: Intent) {
        //start our foreground service
        if (!isServiceRunning) {
            isServiceRunning = true
           // secondsElapsed=0
            userName = incomingIntent.getStringExtra("username")
          //  target = incomingIntent.getStringExtra("target")

            startServiceWithNotification()
         mainRepository.listener = this
           mainRepository.setUsername(userName!!)
         //   mainRepository.setTarget(target!!)
            mainRepository.initFirebase()
            mainRepository.initWebrtcClient(userName!!)




            //setup my clients


        }
    }

    private fun startServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel1", "foreground", NotificationManager.IMPORTANCE_HIGH)
            val intent = Intent(this,MainServiceReceiver::class.java).apply {
                action = "ACTION_EXIT"
            }
            val pendingIntent : PendingIntent =
                PendingIntent.getBroadcast(this,0 ,intent,PendingIntent.FLAG_IMMUTABLE)

            notificationManager.createNotificationChannel(notificationChannel)


            val notification = NotificationCompat.Builder(
                this, "channel1"
            ).setSmallIcon(R.drawable.baseline_videocam_24)
                .addAction(R.drawable.ic_end_call,"Exit",pendingIntent)
               // .setContentText(" ${formatTime(secondsElapsed)}")

            startForeground(1, notification.build())
           // startTimer()
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
interface Listeners{
    fun onCallReceived(type: String)
}

    override fun onLatestEventReceived(type: String) {
    if(type == "clicked"){
       // mainRepository.initLocalSurfaceView(localSurfaceView!!,isVideoCall)
       //   mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)
    }

    //mainRepository.initLocalSurfaceView(localSurfaceView!!,isVideoCall)
   // mainRepository.initRemoteSurfaceView(remoteSurfaceView!!)
    }

    private fun createNotification(): NotificationCompat.Builder {

        val notificationIntent = Intent(this, CallsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
           putExtra("isVideoCall",isVideoCall)
            putExtra("isCaller",isCaller)
            putExtra("target",target)
            putExtra("username",userName)
            action = ACTION_OVERLAY
        }
     //   mainServiceRepository.startService(userName!!)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "channel1")
          //  .setContentTitle("Ongoing Video Call")
            .setContentText(" ${formatTime(secondsElapsed)}")
            .setSmallIcon(R.drawable.baseline_videocam_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                secondsElapsed++
                updateNotification()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun startTimer() {
        isRunning = true
        secondsElapsed=0
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        isRunning = false
        secondsElapsed=0
        handler.removeCallbacks(timerRunnable)
        cancelNotification(1)
    }

    private fun updateNotification() {
        notificationManager.notify(1, createNotification().build())
    }

    private fun cancelNotification(id:Int){
        notificationManager.cancel(id)
    }

    private fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }
fun setTarget(target:String){
    this.target = target
}


    override fun endCall() {
        //we are receiving end call signal from remote peer
        endCallAndRestartRepository()
    }
    interface EndCallListener {
        fun onCallEnded()
    }
    @SuppressLint("WrongConstant")
    private fun showOverlay() {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.item_overlay_layout, null)

            overlayParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                x = 0
                y = 0
                // Adjust position to the bottom right
                gravity = Gravity.BOTTOM or Gravity.END
            }


         //   target = incomingIntent.getSerializableExtra("target") as String


            val localView = overlayView!!.findViewById<SurfaceViewRenderer>(R.id.localView)
            val remoteView = overlayView!!.findViewById<SurfaceViewRenderer>(R.id.remoteView)
         //   val endCallButton = overlayView!!.findViewById<Button>(R.id.endCallButton)

            MainService.localSurfaceView = localView
            MainService.remoteSurfaceView = remoteView

            // Initialize views for overlay
            mainRepository.initLocalSurfaceView(localView, isVideoCall)
            mainRepository.initRemoteSurfaceView(remoteView)

       /*     endCallButton.setOnClickListener {
                handleEndCall()
                hideOverlay()
            }*/
            overlayView?.setOnClickListener {
                // Start the CallActivity
            /*    val intent = Intent(this, CallsActivity::class.java)
                // Add any extra data if needed
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              //  intent.action = ACTION_OVERLAY
                intent.putExtra("username",userName)
             //   intent.putExtra("target",ta)
                startActivity(intent)
                // Remove the overlay*/


                hideOverlay()
            }

            windowManager.addView(overlayView, overlayParams)
        }
    }

    private fun hideOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    // Call showOverlay() when the app is sent to the background
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        showOverlay()
    }
    override fun onDestroy() {
        super.onDestroy()
       hideOverlay()
        stopTimer()

    }

}