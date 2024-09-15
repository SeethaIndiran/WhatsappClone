package com.example.chattingapp.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.models.Chat
import com.google.firebase.auth.FirebaseUser

class TestChatsAdapter(private val context: Context, private val fireUser: FirebaseUser,
                       private val fragment: Activity,private val chatsList:List<Chat>):RecyclerView.Adapter<TestChatsAdapter.ChatsTestViewHolder>() {

    inner class ChatsTestViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsTestViewHolder {
        return  if(viewType == 1){
            val view:View = LayoutInflater.from(context).inflate(R.layout.item_right_msg,parent,false)
            ChatsTestViewHolder(view)

        }else{
            val view:View = LayoutInflater.from(context).inflate(R.layout.item_left_msg,parent,false)
            ChatsTestViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
             return chatsList.size
    }

    override fun onBindViewHolder(holder: ChatsTestViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {

        return if(chatsList[position].sender == fireUser.uid){
            1
        }else{
            0
        }


    }
}