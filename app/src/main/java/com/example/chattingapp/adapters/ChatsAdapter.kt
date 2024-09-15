package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ItemLeftMsgBinding
import com.example.chattingapp.databinding.ItemRightMsgBinding
import com.example.chattingapp.models.Chat
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ChatsAdapter( private val context:Context, private val fireUser:FirebaseUser,
                   private val fragment:Activity):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemBindingLeft: ItemLeftMsgBinding? = null
    private var itemBindingRight: ItemRightMsgBinding? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handlerss: Handler
    private lateinit var runnable: Runnable
    private var delay = 1000L
    private var isPaused = true
    var chats: Chat? = null

    private var handlerMap: MutableMap<Int, Handler> = HashMap() // To store handlers for each item
    private var runnableMap: MutableMap<Int, Runnable> = HashMap()

    private var listener: onClickListener? = null

    var list = mutableListOf<Chat>()
    var selectedPositions = mutableListOf<Int>()

    private val selectedHolders = mutableListOf<ViewHolder>()
    private val selectedChatViewHolders = mutableListOf<ChatViewHolderInterface>()


    init {

        // mediaPlayer = MediaPlayer()
        //  setHasStableIds(true)


    }

   // inner class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

      inner class ChatsViewHolderLeft(binding: ItemLeftMsgBinding)
        :RecyclerView.ViewHolder(binding.root){
      /*  init {

            itemView.setOnClickListener {
                val chat =  differ.currentList[adapterPosition]
                chat.isSelected = !chat.isSelected
                if(chat.isSelected){
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.transparentBlue))
                }else{
                    itemView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
                }
             //   notifyDataSetChanged()
        }
        }*/
        }


    inner class ChatsViewHolderRight(mBinding: ItemRightMsgBinding)
        :RecyclerView.ViewHolder(mBinding.root){
          /*   init {

                 itemView.setOnClickListener {
                     val chat =  differ.currentList[adapterPosition]
                     chat.isSelected = !chat.isSelected
                     if(chat.isSelected){
                         itemView.setBackgroundColor(ContextCompat.getColor(itemView.context,
                             R.color.transparentBlue))
                     }else{
                         itemView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
                     }
                  //   notifyDataSetChanged()
                 }
             }*/
        }


    private val differCallBack = object : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.messageKey == newItem.messageKey
        }


        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)



      override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder{

        return if(position == 0){
            itemBindingLeft = ItemLeftMsgBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
            ChatsViewHolderLeft(itemBindingLeft!!)
        }else{
            itemBindingRight = ItemRightMsgBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
            ChatsViewHolderRight(itemBindingRight!!)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        val chat: Chat = differ.currentList[position]
        val mediaPlayers = HashMap<Int, MediaPlayer>()
        val progressHandlers = HashMap<Int, Handler>()
        val progressRunnables = HashMap<Int, Runnable>()


          holder.itemView.apply {


        if (chat.message == "sent you an image.") {
            //image right
            if (chat.sender == fireUser.uid) {
                itemBindingRight?.clAudio?.visibility = View.GONE
                itemBindingRight?.clVideo?.visibility = View.GONE
                itemBindingRight?.clChatText?.visibility = View.GONE
                itemBindingRight?.clImage?.visibility = View.VISIBLE
                 Glide.with(this)
                            .load(chat.url)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(itemBindingRight?.chatImage!!)
                itemBindingRight?.chatImage?.setOnClickListener {
                    if (fragment is MessageChatActivity) {
                        fragment.navigateToImageVideoFragment(chat)
                    }
                }


            } else {
                //image left
                itemBindingLeft?.clAudio?.visibility = View.GONE
                itemBindingLeft?.clVideo?.visibility = View.GONE
                itemBindingLeft?.clChatText?.visibility = View.GONE
                itemBindingLeft?.clImage?.visibility = View.VISIBLE
                 Glide.with(this)
                            .load(chat.url)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(itemBindingLeft?.chatImage!!)
                itemBindingLeft?.chatImage?.setOnClickListener {
                    if (fragment is MessageChatActivity) {
                        fragment.navigateToImageVideoFragment(chat)
                    }
                }

            }

        } else if (chat.message == "sent you a video.") {
            if (chat.sender == fireUser.uid) {
                itemBindingRight?.clAudio?.visibility = View.GONE
                itemBindingRight?.clVideo?.visibility = View.VISIBLE
                itemBindingRight?.clChatText?.visibility = View.GONE
                itemBindingRight?.clImage?.visibility = View.GONE
                /*  val videoUri = Uri.parse(chat.url)
                    itemBindingRight?.chatVideo?.setVideoURI(videoUri)
                    itemBindingRight?.chatVideo?.start()*/
                loadThumbnailWithGlide(chat.url, itemBindingRight?.chatVideoImage!!)
                itemBindingRight?.chatVideoImage?.setOnClickListener {
                    if (fragment is MessageChatActivity) {
                        fragment.navigateToImageVideoFragment(chat)
                    }
                }
            } else {
                itemBindingLeft?.clAudio?.visibility = View.GONE
                itemBindingLeft?.clVideo?.visibility = View.VISIBLE
                itemBindingLeft?.clChatText?.visibility = View.GONE
                itemBindingLeft?.clImage?.visibility = View.GONE
                itemBindingLeft?.chatVideo?.setVideoURI(chat.url.toUri())
                itemBindingLeft?.chatVideo?.start()
            }


        } else if (chat.message == "sent you an audio.") {


            /*    runnable = object : Runnable {
                    override fun run() {
                        itemBindingRight?.chatSeekbar?.progress = mediaPlayer!!.currentPosition
                        handlerss.postDelayed(this, 1000) // Update seek bar every second (adjust interval as needed)
                    }
                }

                mediaPlayers[position] = mediaPlayer!!
                progressHandlers[position] = handlerss
                progressRunnables[position] = runnable*/


            // Set a default max value, as the duration might not be available immediately

            if (chat.sender == fireUser.uid) {
                mediaPlayer = MediaPlayer()

                handlerss = Handler(Looper.getMainLooper())

                itemBindingRight?.clAudio?.visibility = View.VISIBLE
                itemBindingRight?.clVideo?.visibility = View.GONE
                itemBindingRight?.clChatText?.visibility = View.GONE
                itemBindingRight?.clImage?.visibility = View.GONE

                itemBindingRight?.chatPlayBtn?.setOnClickListener {
                    if (!mediaPlayer!!.isPlaying) {
                        try {
                            mediaPlayer!!.reset()
                            mediaPlayer!!.setDataSource(chat.url)
                            mediaPlayer!!.prepareAsync()

                            mediaPlayer!!.setOnPreparedListener { mp ->
                                mp.start()
                                itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.baseline_pause)

                                startSeekBarProgress(holder.adapterPosition)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        mediaPlayer!!.pause()
                        itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.round_play_arrow_24)
                        stopSeekBarProgress(holder.adapterPosition)
                    }
                }

                itemBindingRight?.chatSeekbar?.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            mediaPlayer?.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        stopSeekBarProgress(holder.adapterPosition) // Stop seek bar update when user starts dragging
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        startSeekBarProgress(holder.adapterPosition) // Resume seek bar update when user stops dragging
                    }
                })

                mediaPlayer?.setOnCompletionListener {
                    itemBindingRight!!.chatPlayBtn.setImageResource(R.drawable.round_play_arrow_24)
                    stopSeekBarProgress(holder.adapterPosition)
                }


            }


            /*     mediaPlayer = MediaPlayer()
                itemBindingRight?.chatSeekbar?.max = mediaPlayer!!.duration

                handlerss = Handler(Looper.getMainLooper())
                runnable = Runnable{

                    itemBindingRight?.chatSeekbar?.progress = mediaPlayer!!.currentPosition
                    handler.postDelayed(runnable,delay)
                }

                if(chat.sender == fireUser.uid) {
                    itemBindingRight?.clAudio?.visibility = View.VISIBLE
                    itemBindingRight?.clVideo?.visibility = View.GONE
                    itemBindingRight?.clChatText?.visibility = View.GONE
                    itemBindingRight?.clImage?.visibility = View.GONE


                    itemBindingRight?.chatPlayBtn?.setOnClickListener {
                        //   mediaPlayer!!.stop()
                     /*   if(fragment is MessageChatActivity){
                            fragment.palyMediaPlayer(chat.url)
                        }*/

                      mediaPlayer!!.reset()

                        mediaPlayer!!.setDataSource(chat.url)
                        mediaPlayer!!.prepare()
                        /*  if(!isPaused){
                               mediaPlayer!!.pause()
                               isPaused = true
                               itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.round_play_arrow_24)
                               handlerss.removeCallbacks(runnable)
                           }else{

                               mediaPlayer!!.start()
                               itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.baseline_pause)

                               handlerss.postDelayed(runnable, delay)
                               isPaused = false
                           }*/


                        if (!mediaPlayer!!.isPlaying) {
                            Log.i("pos", chat.url)
                            mediaPlayer!!.start()


                            itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.baseline_pause)

                            handlerss.postDelayed(runnable, delay)

                        } else {

                            mediaPlayer!!.pause()
                            itemBindingRight?.chatPlayBtn?.setImageResource(R.drawable.round_play_arrow_24)
                            handlerss.removeCallbacks(runnable)



                        }
                        itemBindingRight?.chatSeekbar?.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if (fromUser) mediaPlayer!!.seekTo(progress)

                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {

                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                            }
                        })



                        mediaPlayer!!.setOnCompletionListener {
                            itemBindingRight!!.chatPlayBtn.setImageResource(R.drawable.round_play_arrow_24)
                            //  itemBindingRight?.chatSeekbar?
                            //mediaPlayer!!.stop()
                            // mediaPlayer!!.reset()
                        }
                    }*/




        }

        else {
            if (chat.sender == fireUser.uid) {
                itemBindingRight?.clAudio?.visibility = View.GONE
                itemBindingRight?.clVideo?.visibility = View.GONE
                itemBindingRight?.clChatText?.visibility = View.VISIBLE
                itemBindingRight?.clImage?.visibility = View.GONE

                itemBindingRight?.tvName?.text = chat.message
                itemBindingRight?.tvTime?.text = dateConversion(chat.time)
                if(chat.sender == fireUser.uid) {
                    itemBindingRight?.tick?.visibility = View.VISIBLE
                    if (chat.isSeen) {
                        itemBindingRight?.tick?.setImageResource(R.drawable.baseline_check_24_blue)
                    }else{
                        itemBindingRight?.tick?.setImageResource(R.drawable.check)
                    }
                }

            } else {
                itemBindingLeft?.clAudio?.visibility = View.GONE
                itemBindingLeft?.clVideo?.visibility = View.GONE
                itemBindingLeft?.clChatText?.visibility = View.VISIBLE
                itemBindingLeft?.clImage?.visibility = View.GONE
                itemBindingLeft?.tick?.visibility = View.GONE
                itemBindingLeft?.tvName?.text = chat.message
                itemBindingLeft?.tvTime?.text = dateConversion(chat.time)




            }


        }

   itemBindingRight?.clMain?.setOnClickListener {
       if(fragment is MessageChatActivity){
           fragment.toggleSelection(chat,holder,position)
           fragment.navigateToForwardfragment()
           //  notifyDataSetChanged()
       }
   }
              itemBindingLeft?.clMainLeft?.setOnClickListener {
                  if(fragment is MessageChatActivity){
                      fragment.toggleSelection(chat,holder,position)
                      fragment.navigateToForwardfragment()
                      //  notifyDataSetChanged()
                  }
              }
    }





}




    override fun getItemCount(): Int {
        return differ.currentList.size
    }





    override fun getItemViewType(position: Int): Int {

       return if(differ.currentList[position].sender == fireUser.uid){
            1
        }else{
            0
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
        }}

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

        companion object {
            private const val POSITION_LEFT = 0
            private const val POSITION_RIGHT = 1
        }
    private fun startSeekBarProgress(position: Int) {
        handlerss = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {

                itemBindingRight?.chatSeekbar?.max = mediaPlayer!!.duration
                itemBindingRight?.chatSeekbar?.progress = mediaPlayer!!.currentPosition
                handlerss.postDelayed(this, 1000) // Update seek bar every second (adjust interval as needed)
            }
        }
        handlerss.postDelayed(runnable, 0) // Start updating seek bar
        handlerMap[position] = handlerss // Store handler for this item
        runnableMap[position] = runnable
    }

    private fun stopSeekBarProgress(position: Int) {
       // handlerss.removeCallbacks(runnable) // Stop updating seek bar
        runnableMap[position]?.let { handlerMap[position]?.removeCallbacks(it) }
    }

    fun addChatPosition(position: Int,holder: ChatViewHolderInterface){
        if(!selectedPositions.contains(position)){
            selectedPositions.add(position)
        }
        if(!selectedChatViewHolders.contains(holder)){
            selectedChatViewHolders.add(holder)
        }
    }

    fun getSelectedChats(): List<Pair<Int, ChatViewHolderInterface>> {
        return selectedPositions.zip(selectedChatViewHolders)
      //  return selectedChatViewHolders
    }
    fun getVh():List<ChatViewHolderInterface>{
        return selectedChatViewHolders
    }
    fun dese(){
        for (i in selectedChatViewHolders){
            val vh = i.getItemView()
            vh.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        }
    }
private fun toggleSelection(item:Chat,viewHolder:ViewHolder,position: Int){
    val index = list.indexOf(item)
    if (index != -1) {
        list.removeAt(index)
        selectedHolders.remove(viewHolder)
        viewHolder.itemView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
      //  notifyItemChanged(position)
       // notifyDataSetChanged()

    } else {
        list.add(item)
        selectedHolders.add(viewHolder)
        viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context,
            R.color.transparentBlue))
       // notifyItemChanged(position)
     //   notifyDataSetChanged()
    }
  //  notifyItemChanged(position)
   // notifyDataSetChanged()
}
    fun deselectItems(){
              //   selectedPositions.clear()
        notifyDataSetChanged()
    }

fun getBackToPosition(list:ArrayList<Chat>){
          if(list.isNotEmpty()){

          }
}


    fun getSelectedItems(): List<Chat> {

        return list
    }
fun setChanged(){
    notifyDataSetChanged()
}

    fun getListFromActiivty(listItems:List<Chat>){

    }

    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("hh:mm a")
        val fs = sdf.format(date)
        return fs
    }

    interface ChatViewHolderInterface {
        // Define common methods here, if any
        fun getItemView(): View
    }
interface onClickListener{
    fun onClick(chat:Chat, holder: View, position: Int)
}


}

