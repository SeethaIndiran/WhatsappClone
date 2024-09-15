package com.example.chattingapp.others

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import com.example.chattingapp.R
import java.io.IOException

class MediaPlayerManager(
    private val context: Context,
    private val mediaPlayer: MediaPlayer,
    private val playBtn: ImageView,
    private val seekBar: SeekBar,
    private val url:String
) : MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private var isPrepared = false
    private var isPlaying = false
    private var isSeeking = false

    init {
        mediaPlayer.setOnCompletionListener(this)
        seekBar.setOnSeekBarChangeListener(this)
        prepareMediaPlayer()
    }

    private fun prepareMediaPlayer() {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            isPrepared = true
            seekBar.max = mediaPlayer.duration
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun togglePlayback() {
        if (isPrepared) {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }
    }

    private fun startPlayback() {
        mediaPlayer.start()
        isPlaying = true
        updatePlayButtonImage()
        updateSeekBar()
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
        isPlaying = false
        updatePlayButtonImage()
    }

    private fun updatePlayButtonImage() {
        val resId = if (isPlaying) R.drawable.baseline_pause else R.drawable.round_play_arrow_24
        playBtn.setImageResource(resId)
    }

    private fun updateSeekBar() {
        if (!isSeeking) {
            seekBar.progress = mediaPlayer.currentPosition
            if (isPlaying) {
                Handler(Looper.getMainLooper()).postDelayed({ updateSeekBar() }, 1000)
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        isPlaying = false
        updatePlayButtonImage()
        seekBar.progress = 0
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mediaPlayer.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        isSeeking = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        isSeeking = false
    }
}
