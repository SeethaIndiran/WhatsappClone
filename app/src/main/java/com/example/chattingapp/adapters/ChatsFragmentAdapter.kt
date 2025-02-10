package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ItemChatBinding
import com.example.chattingapp.fragments.ChatsFragment
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.example.chattingapp.models.Users
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class ChatsFragmentAdapter(private val fragment: Fragment, private var isLastCheck:Boolean,private val viewModel: UserViewmodel):RecyclerView.Adapter<ChatsFragmentAdapter.ChatsFragmentViewHolder>() {

    private var itemChatBinding:ItemChatBinding? = null

    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    var lastMsg:Chat? = null
    var lastMsgString =  ""


    inner class ChatsFragmentViewHolder(itemBinding: ItemChatBinding)
        :RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object: DiffUtil.ItemCallback<Users>(){
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.uid == newItem.uid
        }


        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsFragmentViewHolder {

        itemChatBinding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ChatsFragmentViewHolder(itemChatBinding!!)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatsFragmentViewHolder, position: Int) {

        val user = differ.currentList[position]
        holder.itemView.apply {
            itemChatBinding?.tvName?.text = user.username

          //  Glide.with(this).load(File(user.profile)).diskCacheStrategy(DiskCacheStrategy.ALL).into(itemChatBinding?.ivRound!!)
        //    itemChatBinding?.tvLastMsg?.visibility = View.VISIBLE
           // retrieveLastMsg(user.uid,itemChatBinding!!.tvLastMsg,itemChatBinding!!.tvDate)
              getLastMsg(user.uid,itemChatBinding!!.tvLastMsg,itemChatBinding?.tick!!,itemChatBinding?.tvDate!!)
          //  retrieveLastChat(user.uid,itemChatBinding!!.tvLastMsg,position)
       //   itemChatBinding?.tvLastMsg!!.text=lastChat.
        //    itemChatBinding?.tvDate?.text = timeDay(lastChat.time)
         /*   if(lastChat.isSeen){
                itemChatBinding?.tvLastMsg!!.setTextColor(Color.GRAY)
                itemChatBinding?.tvDate!!.setTextColor(Color.GRAY)
            }else{
                itemChatBinding?.tvLastMsg!!.setTextColor(Color.BLACK)
                itemChatBinding?.tvDate!!.setTextColor(Color.BLACK)
            }*/

            setOnClickListener {
                if(fragment is ChatsFragment){
                    fragment.navigateToMessageChatFragment(user.uid)
                }
            }
        }


    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

  @RequiresApi(Build.VERSION_CODES.O)
  private  fun getLastMsg(userIdVisit: String, tv1: TextView, iv:ImageView, tv2:TextView){
        val firestore = FirebaseFirestore.getInstance()
        val chatsDatesRef = firestore.collection("ChatsDates")

// Add a snapshot listener to ChatsDates collection
        chatsDatesRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            // Iterate through each document in the snapshot
            snapshot?.documents?.forEach { document ->
                // Get the date and chats list from the document
                val date = document.id
                val chats = document.toObject(ChatDay::class.java)?.chats ?: emptyList()

                // Find the latest message sent by you
                val latestMessageSentByYou = chats.filter { chat ->
                    chat.receiver == userId && chat.sender == userIdVisit ||
                chat.sender == userId && chat.receiver == userIdVisit}
                    .maxByOrNull { chat -> chat.time }

                // Do something with the latest message
                latestMessageSentByYou?.let { latestMessage ->
                    // Update UI or perform actions
                    tv1.text = latestMessage.message
                    tv2.text = timeDay(latestMessage.time)
                    if(latestMessage.sender == userId){
                        iv.visibility = View.VISIBLE
                        if(latestMessage.isSeen) {
                            iv.setImageResource(R.drawable.baseline_check_24_blue)
                        }else{
                            iv.setImageResource(R.drawable.check)
                        }
                    }else{
                        iv.visibility = View.GONE
                    }
                  //  println("Latest message sent by you on $date: ${latestMessage.text}")
                }
            }
        }

    }
    private fun retrieveLastChat(userIdVisit: String,tv1: TextView,position: Int){
       // viewModel.retrieveLastMessage(userId,userIdVisit)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.lastMsg.collectLatest {
                withContext(Dispatchers.Main){
                    tv1.text = it.chatDayList!![0].message
                }
            }
        }
    }


    }


    fun convertMillisToTime(millis: Long): String {
        // Create a SimpleDateFormat with the desired time format
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Create a Date object using the provided milliseconds
        val date = Date(millis)

        val currentTimeInMillis = System.currentTimeMillis()
        val time2 =  dateFormat.format(date)
        if (isSameTime(currentTimeInMillis, millis)) {
            return "Just now"
        }else{
            return time2
        }
    }

@SuppressLint("SimpleDateFormat")
private fun dateConversion(time: Long): String {
    val date =  Date(time)
    val sdf = SimpleDateFormat("hh:mm a")
    val fs = sdf.format(date)
    return fs
}

    private fun isSameTime(time1: Long, time2: Long): Boolean {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(time1)) == dateFormat.format(Date(time2))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeDay(time:Long):String {
        val yourTimeInMillis: Long = System.currentTimeMillis()

        // Convert the Long value to LocalDateTime
        val yourDateTime = Instant.ofEpochMilli(time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        // Get the current date and time
        val currentDateTime = LocalDateTime.now()

        // Compare dates to determine if it's today, yesterday, or earlier
        val daysBetween = ChronoUnit.DAYS.between(yourDateTime.toLocalDate(), currentDateTime.toLocalDate())

        val result =   when {
            daysBetween == 0L -> convertMillisToTime(time)
            daysBetween == 1L -> "Yesterday"
            else -> dateConversion(time)
        }
        return result
    }

