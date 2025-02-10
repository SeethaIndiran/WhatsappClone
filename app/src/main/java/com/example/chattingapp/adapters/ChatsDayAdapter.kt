package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ItemChatDayBinding
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date

class ChatsDayAdapte(
    private val contexts: Context,private val fireUser: FirebaseUser,
   private val fragment: Activity):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var binding: ItemChatDayBinding? = null
    private val chatsList = ArrayList<Chat>()
    val chatsAdapter = ChatsAdapter(contexts, fireUser, fragment)
    val chatDays = mutableListOf<Chat>()
     val selectedChats = mutableListOf<Chat>()
    val selectedHolders = mutableListOf<ChatsAdapter.ChatViewHolderInterface>()
    private var chatsClickListener:ChatClickListener? = null


    inner class ChatsDayViewHolder(itemBinding: ItemChatDayBinding):
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object: DiffUtil.ItemCallback<ChatDay>(){
        override fun areItemsTheSame(oldItem: ChatDay, newItem: ChatDay): Boolean {
            return oldItem.chatDay == newItem.chatDay
        }


        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ChatDay, newItem: ChatDay): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = ItemChatDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ChatsDayViewHolder(binding!!)
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
     val chatDay = differ.currentList[position]

        holder.itemView.apply {
            binding!!.tvDay.text = dateConversion(chatDay.chatDay)
            binding?.tvDay?.background = ContextCompat.getDrawable(context,R.drawable.item_chat_date_bgnd)

            binding!!.rvChats.adapter = chatsAdapter
            binding!!.rvChats.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            binding!!.rvChats.setHasFixedSize(true)
            submitList(chatDay.chats)





          //  chatsAdapter.differ.submitList(chatDay.chats)
           // chatsAdapter.list.clear()
           // chatsAdapter.notifyDataSetChanged()
        }

holder.setIsRecyclable(false)
    }

    fun submitList(newList: List<Chat>) {
        chatDays.clear()
        chatDays.addAll(newList)
        chatsAdapter.differ.submitList(chatDays)
      //  notifyDataSetChanged()
    }

    fun dateConversion(time:Long):String{
        val date = Date(time)
        val sdf =SimpleDateFormat("MMM dd,yyyy")
        val fs  = sdf.format(date)
        return fs
    }

    interface ChatClickListener {
        fun onChatClick(chat: Chat, position: Int)
    }

}
