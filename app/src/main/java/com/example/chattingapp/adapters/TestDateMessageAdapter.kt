package com.example.chattingapp.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date

class DateMessageAdapter(private val msgList:List<ChatDay>, private val currentUser: FirebaseUser?,
                         private val context: Context, private val selectionChangeListener: OnSelectionChangeListener,
    private val activity: Activity
):RecyclerView.Adapter<DateMessageAdapter.DateViewHolder>(){

      // Global variable to store selected chats
      val selectedMsgMapTest: MutableMap<String, MutableList<Int>> = mutableMapOf()
    private val selectedMsgMap = mutableMapOf<Long,MutableSet<Int>>()
    private val selectedViewHolders = mutableMapOf<Long, MutableMap<Int, DateViewHolder>>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DateMessageAdapter.DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_day,parent,false)
      return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateMessageAdapter.DateViewHolder, position: Int) {
            val dateMessage = msgList[position]
       holder.bind(dateMessage,position)
    }

    override fun getItemCount(): Int {
           return msgList.size
    }

    inner class DateViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private val dateText = itemView.findViewById<TextView>(R.id.tv_day)
      private val rvDate = itemView.findViewById<RecyclerView>(R.id.rv_chats)

      fun bind(dateMsg:ChatDay,position: Int){
        dateText.text = dateConversion(dateMsg.chatDay)
          dateText?.background = ContextCompat.getDrawable(context,R.drawable.item_chat_date_bgnd)

        val msgAdapter = TestAdapter(dateMsg.chats,currentUser!!,context).apply {
          setOnItemClickListener {msgPosition ->
              toggleSelection(dateMsg.chatDay,msgPosition,this@DateViewHolder)
            if(activity is MessageChatActivity){
                activity.navigateToForwardfragment()
            }
          }
        }

          rvDate.adapter = msgAdapter
          rvDate.layoutManager = LinearLayoutManager(itemView.context)

          val selectedPosotions= selectedMsgMap[dateMsg.chatDay] ?: emptySet()
          msgAdapter.setSelectedMsgs(selectedPosotions)
      }
        private fun toggleSelection(date: Long, msgPosition: Int, viewHolder: DateViewHolder){
            val selectedPos=selectedMsgMap[date]?: mutableSetOf()
            val selectedHolders = selectedViewHolders[date] ?: mutableMapOf()


            if(selectedPos.contains(msgPosition)){
                selectedPos.remove(msgPosition)
                selectedHolders.remove(msgPosition)
            }else{
                selectedPos.add(msgPosition)
                selectedHolders[msgPosition] = viewHolder
            }
            selectedMsgMap[date] = selectedPos
            selectedViewHolders[date] = selectedHolders

            val totalSelectedItems = selectedMsgMap.values.sumOf { it.size }
         //   selectionChangeListener.onSelectionChanged(totalSelectedItems)

            notifyItemChanged(adapterPosition)

        }


    }
    fun dateConversion(time:Long):String{
        val date = Date(time)
        val sdf = SimpleDateFormat("MMM dd,yyyy")
        val fs  = sdf.format(date)
        return fs
    }

    fun clearSelection(list: MutableList<Chat>){

        for(item in list){
            item.isSelected = false
        }
        notifyDataSetChanged()
        selectedMsgMap.clear()  // Clear all selected message positions
        selectedViewHolders.clear()
        // Clear the view holders map
         list.clear()
          // Notify the RecyclerView to refresh all items
        selectionChangeListener.onSelectionChanged(0)
    }


    fun getSelectedChats(): List<Chat> {
        val selectedChatList = mutableListOf<Chat>()
        for ((date, selectedPositions) in selectedMsgMap) {
            val chatDay = msgList.find { it.chatDay == date }
            chatDay?.let {
                val selectedMessages = it.chats.filterIndexed { index, _ -> selectedPositions.contains(index) }
                selectedChatList.addAll(selectedMessages)  // Add selected messages directly to the list
            }
        }


        return selectedChatList  // Return the list of selected Chat objects
    }

    fun changeBackground(){

        // Iterate through all the selected messages stored in selectedMsgMap
        for ((date, selectedPositions) in selectedMsgMap) {
            // Find all chatDays that match the date (in case there are multiple)
            msgList.filter { it.chatDay == date }.forEach { chatDay ->
                // Deselect all chats in the chatDay that were selected
                chatDay.chats.forEachIndexed { index, chat ->
                    if (selectedPositions.contains(index)) {
                        // Update the boolean value of the selected chat
                        chat.isSelected = false
                    }
                }
            }
        }

        selectedMsgMap.clear()  // Clear all selected message positions
        selectedViewHolders.clear()  // Clear the view holders map

        // Notify the RecyclerView to refresh all items
        selectionChangeListener.onSelectionChanged(0)
        notifyDataSetChanged()
    }
    interface OnSelectionChangeListener {
        fun onSelectionChanged(selectedItemCount: Int)
    }

}