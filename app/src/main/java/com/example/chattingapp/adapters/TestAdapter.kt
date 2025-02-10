package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.models.Chat
import com.google.firebase.auth.FirebaseUser
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class TestAdapter(private val messageList: List<Chat>, private val currentUser: FirebaseUser,
                   private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    private var seekBarUpdateHandler: Handler? = null
    private var runnable: Runnable? = null// Track selected item
    private var onItemClickListener:((Int) -> Unit)? = null

    companion object {
        const val VIEW_TYPE_SENDER = 1
        const val VIEW_TYPE_RECEIVER = 2
    }




    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.sender == currentUser.uid) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_right_msg, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_left_msg, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is SenderViewHolder) {
            holder.bind(message, position)
        } else if (holder is ReceiverViewHolder) {
            holder.bind(message, position)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setOnItemClickListener(listener: ((Int)->Unit)){
        onItemClickListener = listener
    }

    private fun isSelected(position: Int):Boolean{
        return selectedPositions.contains(position)
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainer: ConstraintLayout = itemView.findViewById(R.id.cl_main)
        private val clChatImage: ConstraintLayout = itemView.findViewById(R.id.cl_image)
        private val clChatVideo: ConstraintLayout = itemView.findViewById(R.id.cl_video)
        private val clChatText: ConstraintLayout = itemView.findViewById(R.id.cl_chat_text)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_day)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val ivTick: ImageView = itemView.findViewById(R.id.tick)
        private val chatImage: ImageView = itemView.findViewById(R.id.chat_image)
        private val imageForward: ImageView = itemView.findViewById(R.id.image_forward)
        private val chatVideo: ImageView = itemView.findViewById(R.id.chat_video_image)
        private val chatVideoForward: ImageView = itemView.findViewById(R.id.image_forward_video)
        private val audioChatLayout: ConstraintLayout = itemView.findViewById(R.id.cl_audio)
        private val roundImage: RoundedImageView = itemView.findViewById(R.id.chat_roundedImageView)
        private val chatAudioImage: ImageView = itemView.findViewById(R.id.chat_audio_image)
        private val playPauseButtonSender: ImageButton = itemView.findViewById(R.id.chat_play_btn)
        private val audioSeekBarSender: SeekBar = itemView.findViewById(R.id.chat_seekbar)


        fun bind(message: Chat, position: Int) {
            if (message.message=="sent you an image.") {
                clChatText.visibility = View.GONE
                clChatImage.visibility = View.VISIBLE
                audioChatLayout.visibility =View.GONE
                clChatVideo.visibility = View.GONE

                Glide.with(context)
                    .load(message.url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(chatImage)
                chatImage.setOnClickListener {
                  /*  if (activity is MessageChatActivity) {
                        activity.navigateToImageVideoFragment(message)
                    }*/
                }

            }else if(message.message == "sent you an audio."){
                clChatText.visibility = View.GONE
                clChatImage.visibility = View.GONE
                audioChatLayout.visibility =View.VISIBLE
                clChatVideo.visibility = View.GONE
                setupAudioSender(message,position)
            }else if(message.message == "sent you an video."){
                clChatText.visibility = View.GONE
                clChatImage.visibility = View.GONE
                audioChatLayout.visibility =View.GONE
                clChatVideo.visibility = View.VISIBLE

                loadThumbnailWithGlide(message.url, chatVideo)
               chatVideo.setOnClickListener {
                  /*  if (activity is MessageChatActivity) {
                        activity.navigateToImageVideoFragment(message)
                    }*/
                }
            }
            else{
                clChatText.visibility = View.VISIBLE
                clChatImage.visibility = View.GONE
                audioChatLayout.visibility =View.GONE
                clChatVideo.visibility = View.GONE

                tvName.text = message.message
                tvTime.text = dateConversion(message.time)
                // if(message.sender == currentUser.uid) {
                ivTick.visibility = View.VISIBLE
                if (message.isSeen) {
                    ivTick.setImageResource(R.drawable.baseline_check_24_blue)
                }else{
                    ivTick.setImageResource(R.drawable.check)
                }
            }

            // Set background based on selection
         /*   if (selectedPositions.contains(position)) {
                messageContainer.setBackgroundResource(R.drawable.selected_bgnd)
            } else {
                messageContainer.setBackgroundResource(R.drawable.default_bgnd)
            }*/

            if (message.isSelected) {
                messageContainer.setBackgroundResource(R.drawable.selected_bgnd)
            } else {
                messageContainer.setBackgroundResource(R.drawable.default_bgnd)
            }

            // Handle item click
            itemView.setOnClickListener {
                toggleSelectionTest(position)
                onItemClickListener?.invoke(position)

            }
        }
        private fun setupAudioSender(message: Chat, position: Int) {
            playPauseButtonSender.setOnClickListener {
                if (currentPlayingPosition == position) {
                    // Pause the audio
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        playPauseButtonSender.setImageResource(R.drawable.round_play_arrow_24)
                    } else {
                        // Resume playing the audio
                        mediaPlayer?.start()
                        playPauseButtonSender.setImageResource(R.drawable.baseline_pause_24)
                        updateSeekBar()
                    }
                } else {
                    playNewAudio(message, position)
                }
            }
        }

        private fun playNewAudio(message: Chat, position: Int) {
            if (mediaPlayer != null) {
                releaseMediaPlayer()
            }

            // Initialize new MediaPlayer and play audio
            mediaPlayer = MediaPlayer().apply {
                setDataSource(message.url) // Path to the audio file
                prepare()
                start()
            }

            playPauseButtonSender.setImageResource(R.drawable.baseline_pause_24)
            audioSeekBarSender.max = mediaPlayer?.duration ?: 0
            currentPlayingPosition = position

            updateSeekBar()

            mediaPlayer?.setOnCompletionListener {
                playPauseButtonSender.setImageResource(R.drawable.round_play_arrow_24)
                audioSeekBarSender.progress = 0
                currentPlayingPosition = -1
                stopSeekBarUpdate()
            }
        }

        private fun updateSeekBar() {
            seekBarUpdateHandler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                mediaPlayer?.let {
                    audioSeekBarSender.progress = it.currentPosition
                    seekBarUpdateHandler?.postDelayed(runnable!!, 100)
                }
            }
            seekBarUpdateHandler?.post(runnable!!)
        }

        private fun stopSeekBarUpdate() {
          //  seekBarUpdateHandler?.removeCallbacks(runnable!!)
            seekBarUpdateHandler?.let { handler ->
                runnable?.let { runnableTask ->
                    handler.removeCallbacks(runnableTask)
                }
            }
            runnable = null
        }

        private fun releaseMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
            stopSeekBarUpdate()
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainerLeft: ConstraintLayout = itemView.findViewById(R.id.cl_main_left)
        private val clChatImageLeft: ConstraintLayout = itemView.findViewById(R.id.cl_image_left)
        private val clChatVideoLeft: ConstraintLayout = itemView.findViewById(R.id.cl_video_left)
        private val clChatTextLeft: ConstraintLayout = itemView.findViewById(R.id.cl_chat_text_left)
        private val tvNameLeft: TextView = itemView.findViewById(R.id.tv_name_left)
        private val tvTimeLeft: TextView = itemView.findViewById(R.id.tv_time_left)
        private val ivTickLeft: ImageView = itemView.findViewById(R.id.tick_left)
        private val chatImageLeft: ImageView = itemView.findViewById(R.id.chat_image_left)
        private val imageForwardLeft: ImageView = itemView.findViewById(R.id.image_forward_left)
        private val chatVideoLeft: ImageView = itemView.findViewById(R.id.chat_video_image_left)
        private val chatVideoForwardLeft: ImageView = itemView.findViewById(R.id.image_forward_video_left)
        private val audioChatLayoutLeft: ConstraintLayout = itemView.findViewById(R.id.cl_audio_left)
        private val roundImageLeft: RoundedImageView = itemView.findViewById(R.id.chat_roundedImageView_left)
        private val chatAudioImageLeft: ImageView = itemView.findViewById(R.id.chat_audio_image_left)
        private val playPauseButtonSenderLeft: ImageButton = itemView.findViewById(R.id.chat_play_btn_left)
        private val audioSeekBarSenderLeft: SeekBar = itemView.findViewById(R.id.chat_seekbar_left)


        fun bind(message: Chat, position: Int) {
             if (message.message == "sent you an image.") {
                clChatTextLeft.visibility = View.GONE
                clChatImageLeft.visibility = View.VISIBLE
                audioChatLayoutLeft.visibility =View.GONE
                clChatVideoLeft.visibility = View.GONE

                Glide.with(context)
                    .load(message.url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(chatImageLeft)
                chatImageLeft.setOnClickListener {
                 /*   if (activity is MessageChatActivity) {
                        activity.navigateToImageVideoFragment(message)
                    }*/
                }
            }else if(message.message == "sent you an audio."){
                clChatTextLeft.visibility = View.GONE
                clChatImageLeft.visibility = View.GONE
                audioChatLayoutLeft.visibility =View.VISIBLE
                clChatVideoLeft.visibility = View.GONE
                setupAudioReceiver(message,position)
            }else if(message.message == "sent you an video."){
                 clChatTextLeft.visibility = View.GONE
                 clChatImageLeft.visibility = View.GONE
                 audioChatLayoutLeft.visibility =View.GONE
                 clChatVideoLeft.visibility = View.VISIBLE
                 loadThumbnailWithGlide(message.url, chatVideoLeft)
                 chatVideoLeft.setOnClickListener {
                     /*  if (activity is MessageChatActivity) {
                           activity.navigateToImageVideoFragment(message)
                       }*/
                 }

            }else{
                clChatTextLeft.visibility = View.VISIBLE
                clChatImageLeft.visibility = View.GONE
                audioChatLayoutLeft.visibility =View.GONE
                clChatVideoLeft.visibility = View.GONE

                tvNameLeft.text = message.message
                tvTimeLeft.text = dateConversion(message.time)
                // if(message.sender == currentUser.uid) {
                ivTickLeft.visibility = View.VISIBLE
                if (message.isSeen) {
                    ivTickLeft.setImageResource(R.drawable.baseline_check_24_blue)
                }else{
                    ivTickLeft.setImageResource(R.drawable.check)
                }
            }

            // Set background based on selection
            if (message.isSelected) {
                messageContainerLeft.setBackgroundResource(R.drawable.selected_bgnd)
            } else {
                messageContainerLeft.setBackgroundResource(R.drawable.default_bgnd)
            }

            // Handle item click
            itemView.setOnClickListener {
                toggleSelectionTest(position)
                onItemClickListener?.invoke(position)
            }
        }
        private fun setupAudioReceiver(message: Chat, position: Int) {
            playPauseButtonSenderLeft.setOnClickListener {
                if (currentPlayingPosition == position) {
                    // Pause the audio
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        playPauseButtonSenderLeft.setImageResource(R.drawable.round_play_arrow_24)
                    } else {
                        // Resume playing the audio
                        mediaPlayer?.start()
                        playPauseButtonSenderLeft.setImageResource(R.drawable.baseline_pause_24)
                        updateSeekBar()
                    }
                } else {
                    playNewAudio(message, position)
                }
            }
        }

        private fun playNewAudio(message: Chat, position: Int) {
            if (mediaPlayer != null) {
                releaseMediaPlayer()
            }

            // Initialize new MediaPlayer and play audio
            mediaPlayer = MediaPlayer().apply {
                setDataSource(message.url) // Path to the audio file
                prepare()
                start()
            }

            playPauseButtonSenderLeft.setImageResource(R.drawable.baseline_pause_24)
            audioSeekBarSenderLeft.max = mediaPlayer?.duration ?: 0
            currentPlayingPosition = position

            updateSeekBar()

            mediaPlayer?.setOnCompletionListener {
                playPauseButtonSenderLeft.setImageResource(R.drawable.round_play_arrow_24)
                audioSeekBarSenderLeft.progress = 0
                currentPlayingPosition = -1
                stopSeekBarUpdate()
            }
        }

        private fun updateSeekBar() {
            seekBarUpdateHandler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                mediaPlayer?.let {
                    audioSeekBarSenderLeft.progress = it.currentPosition
                    seekBarUpdateHandler?.postDelayed(runnable!!, 100)
                }
            }
            seekBarUpdateHandler?.post(runnable!!)
        }

        private fun stopSeekBarUpdate() {
            seekBarUpdateHandler?.let { handler ->
                runnable?.let { runnableTask ->
                    handler.removeCallbacks(runnableTask)
                }
            }
            runnable = null
        }

        private fun releaseMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
            stopSeekBarUpdate()
        }
    }
    private fun toggleSelection(position: Int) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.add(position)
        }
        notifyItemChanged(position)


    }

    private fun toggleSelectionTest(position: Int) {
        val message = messageList[position]
       // val updatedMessage = message.copy(isSelected = !message.isSelected)
          message.isSelected = !message.isSelected
       // messageList[position] = updatedMessage // Use the set method to update the list
        notifyItemChanged(position)
    }

fun setSelectedMsgs(selectedPosition:Set<Int>){
    this.selectedPositions.clear()
    this.selectedPositions.addAll(selectedPosition)
    notifyDataSetChanged()
}

    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("hh:mm a")
        val fs = sdf.format(date)
        return fs
    }

    private fun loadThumbnailWithGlide(videoUrl: String, imageView: ImageView) {
        GlobalScope.launch(Dispatchers.Main) {
            val thumbnail = getVideoThumbnail(videoUrl)

            // Load thumbnail using Glide
            Glide.with(context)
                .load(thumbnail)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView)
        }
    }
    private suspend fun getVideoThumbnail(videoUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoUrl, HashMap<String, String>())
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val timeMicroseconds = 1000000L
                val frameAtTime = retriever.getFrameAtTime(timeMicroseconds)

                retriever.release()

                frameAtTime
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }



}
