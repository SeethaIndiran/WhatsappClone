package com.example.chattingapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import com.example.chattingapp.databinding.ActivityCallsBinding
import com.example.chattingapp.firebasevideocall.service.MainService
import com.example.chattingapp.firebasevideocall.service.MainServiceRepository
import com.example.chattingapp.models.Users
import com.example.chattingapp.notifications.DataCall
import com.example.chattingapp.notifications.DataCalls
import com.example.chattingapp.others.Constants.Companion.ACTION_FCM
import com.example.chattingapp.others.Constants.Companion.ACTION_FCM_ANSWER
import com.example.chattingapp.others.Constants.Companion.ACTION_MSG_CHAT_ACTIVITY
import com.example.chattingapp.others.Constants.Companion.ACTION_OVERLAY
import com.example.chattingapp.others.Constants.Companion.KEY_IS_CALLER
import com.example.chattingapp.others.Constants.Companion.KEY_IS_VIDEO_CALL
import com.example.chattingapp.others.Constants.Companion.KEY_USERNAME
import com.example.chattingapp.others.Constants.Companion.PREFS_NAME
import com.example.chattingapp.utils.FirebaseClient
import com.example.chattingapp.utils.FirebaseRepository
import com.example.chattingapp.utils.convertToHumanTime
import com.example.chattingapp.viewmodels.UserViewmodel
import com.example.chattingapp.webrtc.RTCAudioManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_calls.localView
import kotlinx.android.synthetic.main.activity_calls.remoteView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class CallsActivity : AppCompatActivity(), MainService.EndCallListener{

    private lateinit var binding:ActivityCallsBinding
  //  private var mainServiceRepository: MainServiceRepository? = null
    private var firebaseRepository:FirebaseRepository? = null
    private val viewModel:UserViewmodel by viewModels()
    private var firebaseClient:FirebaseClient? = null
    private var target:String? = null
    private var isVideoCall:Boolean? = null
    private var isCaller:Boolean? = null
    private var username:String?=null
    private var isCallActive = false
    private var isCallReceived = false



    private var service:MainService? = null

    @Inject
    lateinit var serviceRepository: MainServiceRepository

    private lateinit var requestScreenCaptureLauncher: ActivityResultLauncher<Intent>

    private var currentUser = ""
    private var isMicrophoneMuted = false
    private var isCameraMuted = false
    private var isSpeakerMode = true
    private var isScreenCasting = false

    override fun onStart() {
        super.onStart()
        requestScreenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts
            .StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val intent = result.data
                //its time to give this intent to our service and service passes it to our webrtc client
                MainService.screenPermissionIntent = intent
                isScreenCasting = true
                updateUiToScreenCaptureIsOn()
                serviceRepository.toggleScreenShare(true)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallsBinding.inflate(layoutInflater)

        setContentView(binding.root)



      //  mainServiceRepository = MainServiceRepository(this)
        handleIntent(intent)
 //  service!!.listener = this


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
       handleIntent(intent!!)
    }

    private fun handleIntent(intent: Intent) {
        isCallActive = true

        intent.let {
            when (it.action) {
                ACTION_FCM -> {
                    val dataModel = it.getSerializableExtra("dataModel") as DataCall

                    target = dataModel.target
                    username = dataModel.user
                    //   firebaseRepository!!.setTarget(target!!)
                    isCaller = false
                    isVideoCall = dataModel.type == "start_video_call"
                    Log.i("fcm", it.data.toString())
                      serviceRepository.startService(target!!)
                    init(isVideoCall!!, isCaller!!, username!!)


                    binding.beforeCallCl.visibility = View.VISIBLE
                    binding.tvCallie.text=dataModel.title
                    binding.remoteView.visibility = View.GONE
                    binding.localView.visibility = View.GONE
                    binding.controlPanelLayout.visibility = View.GONE
                    binding.titleLayout.visibility = View.GONE

                    binding.attendCallButton.setOnClickListener {
                        FirebaseFirestore.getInstance().collection("Users")
                            .document(target!!).update("status","received").addOnCompleteListener {

                            }
                        if (!isVideoCall!!) {
                         binding.toggleCameraButton.isVisible = false
                            binding.switchCameraButton.isVisible = false
                            binding.screenShareButton.isVisible = false
                        }


                        binding.beforeCallCl.visibility = View.GONE
                        binding.remoteView.visibility = View.VISIBLE
                        binding.localView.visibility = View.VISIBLE
                        binding.controlPanelLayout.visibility = View.VISIBLE
                        binding.titleLayout.visibility = View.VISIBLE

                    }
                     binding.endCallButton.setOnClickListener {
                         serviceRepository.sendEndCall()


                     }

                }

                ACTION_FCM_ANSWER -> {
                    val dataModel = it.getSerializableExtra("dataModel") as DataCall
                    target = dataModel.target
                    username = dataModel.user
                    isCaller = false

                    isVideoCall = dataModel.type == "start_video_call"
                    Log.i("answer", it.data.toString())
                    val id = it.getIntExtra("id", -1)
                    NotificationManagerCompat.from(this).cancel(null, id)



                        init(isVideoCall!!, isCaller!!, username!!)

                        FirebaseFirestore.getInstance().collection("Users")
                            .document(target!!).update("status", "received").addOnCompleteListener {

                            }



                }

                ACTION_MSG_CHAT_ACTIVITY -> {

                    val dataModel = it.getSerializableExtra("data_model") as DataCall
                    target = dataModel.target
                    username = dataModel.user
                    isCaller = true
                    isVideoCall = dataModel.type == "start_video_call"

                    init(isVideoCall!!, isCaller!!, target!!)

                val db =    FirebaseFirestore.getInstance().collection("Users").document(target!!)
                        db.addSnapshotListener{snapshot,exception->
                            if (exception != null) {

                                return@addSnapshotListener
                            }
                            snapshot?.let {
                                val user = it.toObject(Users::class.java)

                                  if(user!!.status=="received" ){

                                      binding.beforeCallCl.visibility = View.GONE
                                      binding.remoteView.visibility = View.VISIBLE
                                      binding.localView.visibility = View.VISIBLE
                                      binding.controlPanelLayout.visibility = View.VISIBLE
                                      binding.titleLayout.visibility = View.VISIBLE


                                  }else {

                                      binding.beforeCallCl.visibility = View.VISIBLE
                                      binding.tvCallie.text = "calling $target"
                                      binding.attendCallButton.visibility = View.GONE
                                      binding.endCall.visibility = View.GONE
                                      binding.remoteView.visibility = View.GONE
                                      binding.localView.visibility = View.GONE
                                      binding.controlPanelLayout.visibility = View.VISIBLE
                                      binding.titleLayout.visibility = View.GONE



                            }
                            }
                        }




                }




                else -> {}
            }
        }
    }




    private fun init(isVideoCall:Boolean,isCaller:Boolean,target:String) {


        binding.apply {

            callTitleTv.text = "In call with $target"
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0..3600) {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        //convert this int to human readable time
                        callTimerTv.text = i.convertToHumanTime()
                    }
                }
            }

            if (!isVideoCall) {
                toggleCameraButton.isVisible = false
                switchCameraButton.isVisible = false
                screenShareButton.isVisible = false
            }

            MainService.remoteSurfaceView = remoteView
            MainService.localSurfaceView = localView
            serviceRepository.setupViews(isVideoCall, isCaller, target)

            endCallButton.setOnClickListener {
                serviceRepository.sendEndCall()
                serviceRepository.stopService()


            }
            switchCameraButton.setOnClickListener {
                serviceRepository.switchCamera()
            }
        }
        setupCameraToggleClicked()
        setupMicToggleClicked()
        setupToggleAudioDevice()
        setupScreenCasting()
        MainService.endCallListener = this
    }

    private fun setupScreenCasting() {
        binding.apply {
            screenShareButton.setOnClickListener {
                if (!isScreenCasting){
                    //we have to start casting
                    AlertDialog.Builder(this@CallsActivity)
                        .setTitle("Screen Casting")
                        .setMessage("You sure to start casting ?")
                        .setPositiveButton("Yes"){dialog,_ ->
                            //start screen casting process
                            startScreenCapture()
                            dialog.dismiss()
                        }.setNegativeButton("No") {dialog,_ ->
                            dialog.dismiss()
                        }.create().show()
                }else{
                    //we have to end screen casting
                    isScreenCasting = false
                    updateUiToScreenCaptureIsOff()
                    serviceRepository.toggleScreenShare(false)
                }
            }

        }
    }

    private fun startScreenCapture() {
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        requestScreenCaptureLauncher.launch(captureIntent)

    }

    private fun updateUiToScreenCaptureIsOn(){
        binding.apply {
            localView.isVisible = false
            switchCameraButton.isVisible = false
            toggleCameraButton.isVisible = false
            screenShareButton.setImageResource(R.drawable.stop_screen_share)
        }

    }
    private fun updateUiToScreenCaptureIsOff() {
        binding.apply {
            localView.isVisible = true
            switchCameraButton.isVisible = true
            toggleCameraButton.isVisible = true
            screenShareButton.setImageResource(R.drawable.ic_screen_share)
        }
    }

    private fun setupMicToggleClicked(){
        binding.apply {
            toggleMicrophoneButton.setOnClickListener {
                if (!isMicrophoneMuted){
                    //we should mute our mic
                    //1. send a command to repository
                    serviceRepository.toggleAudio(true)
                    //2. update ui to mic is muted
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on)
                }else{
                    //we should set it back to normal
                    //1. send a command to repository to make it back to normal status
                    serviceRepository.toggleAudio(false)
                    //2. update ui
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }

    private fun setupCameraToggleClicked(){
        binding.apply {
            toggleCameraButton.setOnClickListener {
                if (!isCameraMuted){
                    serviceRepository.toggleVideo(true)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_on)
                }else{
                    serviceRepository.toggleVideo(false)
                    toggleCameraButton.setImageResource(R.drawable.ic_camera_off)
                }

                isCameraMuted = !isCameraMuted
            }
        }
    }
    private fun setupToggleAudioDevice(){
        binding.apply {
            toggleAudioDevice.setOnClickListener {
                if (isSpeakerMode){
                    //we should set it to earpiece mode
                    toggleAudioDevice.setImageResource(R.drawable.ic_speaker)
                    //we should send a command to our service to switch between devices
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.EARPIECE.name)

                }else{
                    //we should set it to speaker mode
                    toggleAudioDevice.setImageResource(R.drawable.ic_ear)
                    serviceRepository.toggleAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE.name)

                }
                isSpeakerMode = !isSpeakerMode
            }

        }
    }

    override fun onCallEnded() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainService.remoteSurfaceView!!.release()
        MainService.remoteSurfaceView = null

        MainService.localSurfaceView!!.release()
        MainService.localSurfaceView = null

        serviceRepository.hideOverlay()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        serviceRepository.sendEndCall()
    }
    private fun saveCallInfo(isVideoCall: Boolean, isCaller: Boolean, username: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_IS_VIDEO_CALL, isVideoCall)
            putBoolean(KEY_IS_CALLER, isCaller)
            putString(KEY_USERNAME, username)
            apply()
        }
    }






}