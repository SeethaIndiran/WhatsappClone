package com.example.chattingapp.utils

import android.content.Intent
import com.example.chattingapp.notifications.DataCall
import com.example.chattingapp.webrtc.MyPeerOberver
import com.example.chattingapp.webrtc.WebRTCClient
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val webRTCClient: WebRTCClient,
    private val gson: Gson
) : WebRTCClient.Listener {

    private var target: String? = null
    private var username: String? = null
    private var remoteView:SurfaceViewRenderer? = null
    var listener: Listener? = null
    var time = System.currentTimeMillis()


    fun initFirebase() {
        firebaseClient.subscribeForLatestEvent(object : FirebaseClient.Listener {
            override fun onLatestEventReceived(type:String,data:String) {

                listener?.onLatestEventReceived(type!!)
                when (type) {
                    "offer"->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                data
                            )
                        )
                        webRTCClient.answer(target!!)
                    }
                    "answer"->{
                        webRTCClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                data
                            )
                        )
                    }
                    "ice_candidate"->{
                        val candidate: IceCandidate? = try {
                            gson.fromJson(data,IceCandidate::class.java)
                        }catch (e:Exception){
                            null
                        }
                        candidate?.let {
                            webRTCClient.addIceCandidateToPeer(it)
                        }
                    }
                    "end_call"->{
                        listener?.endCall()
                    }
                    else -> Unit
                }
            }

        },username!! )
    }
    fun sendConnectionRequest(target: String, isVideoCall: Boolean, success: (Boolean) -> Unit) {
        firebaseClient.sendMessageToOtherClient(
            DataCall(
                type = if (isVideoCall) "start_video_call" else "start_audio_call",
                target = target
            ), success
        )
    }

    fun setUsername(username:String){
        this.username = username
       // firebaseClient.setUsername(username)

    }


    fun setTarget(target:String){
        this.target = target
    }

    interface Listener{
        fun onLatestEventReceived(type: String)
        fun endCall()


    }


    fun initWebrtcClient(username:String){
        webRTCClient.listener = this
        webRTCClient.initializeWebrtcClient(username, object :MyPeerOberver(){
            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                try {
                    p0?.videoTracks?.get(0)?.addSink(remoteView)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!,it)

                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if(newState == PeerConnection.PeerConnectionState.CONNECTED){
                  firebaseClient.clearLatestEvent(username)
                    firebaseClient.clearLatestEvent(target!!)
                }
                if(newState == PeerConnection.PeerConnectionState.CLOSED){
                    firebaseClient.clearLatestStatus(username)
                }
              //  firebaseClient.clearLatestEvent(username)
            }
        })
    }

    fun initLocalSurfaceView(view:SurfaceViewRenderer,isVideoCall:Boolean){
        webRTCClient.initLocalSurfaceView(view,isVideoCall)
    }

    fun initRemoteSurfaceView(view:SurfaceViewRenderer){
        webRTCClient.initRemoteSurfaceView(view)
        this.remoteView = view
    }

    fun startCall(){
        webRTCClient.call(target!!)
    }

    fun endCall(){
        webRTCClient.closeConnection()
    }
    fun sendEndCall(username: String){
        onTransferEventToSocket(DataCall("",0,"","",target!!,"end_call"))
    firebaseClient.clearLatestStatus(username)
    }

    fun toggleAudio(shouldBeMuted:Boolean){
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted:Boolean){
        webRTCClient.toggleVideo(shouldBeMuted)
    }

    fun switchCamera(){
        webRTCClient.switchCamera()
    }

    override fun onTransferEventToSocket(data: DataCall) {
  firebaseClient.sendMessageToOtherClient(data){}
    }


    fun setScreenCaptureIntent(screenPermissionIntent: Intent) {
        webRTCClient.setPermissionIntent(screenPermissionIntent)
    }

    fun toggleScreenShare(isStarting: Boolean) {
        if (isStarting){
            webRTCClient.startScreenCapturing()
        }else{
            webRTCClient.stopScreenCapturing()
        }
    }

    fun logOff(function: () -> Unit) = firebaseClient.logOff(function)


}