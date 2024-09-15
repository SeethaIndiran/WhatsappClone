package com.example.chattingapp.others

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.chattingapp.CallsActivity
import com.example.chattingapp.R
import com.example.chattingapp.firebasevideocall.service.MainService
import com.example.chattingapp.utils.FirebaseRepository
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

class OverlayManager(private val context: Context) {
    private var overlayView: View? = null

     val mainRepository: FirebaseRepository? = null
    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    fun showOverlay() {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(context).inflate(R.layout.activity_calls, null)
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                // Position the overlay in the bottom right corner
                gravity = Gravity.BOTTOM or Gravity.END
                x = 0
                y = 0
            }

            overlayView?.setOnClickListener {
                // Start the call activity when the overlay is clicked
                val intent = Intent(context, CallsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }

            val localView = overlayView!!.findViewById<SurfaceViewRenderer>(R.id.localView)
            val remoteView = overlayView!!.findViewById<SurfaceViewRenderer>(R.id.remoteView)
            //  val endCallButton = overlayView!!.findViewById<Button>(R.id.endCallButton)

            MainService.localSurfaceView = localView
            MainService.remoteSurfaceView = remoteView

            // Initialize views for overlay
            mainRepository!!.initLocalSurfaceView(localView, true)
            mainRepository!!.initRemoteSurfaceView(remoteView)

            windowManager.addView(overlayView, layoutParams)
        }
    }

    fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }
}