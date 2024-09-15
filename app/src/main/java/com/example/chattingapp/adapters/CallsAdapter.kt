package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ItemCallLayoutBinding
import com.example.chattingapp.fragments.CallsFragment
import com.example.chattingapp.models.Calls
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

class CallsAdapter(private val fragment: Fragment):RecyclerView.Adapter<CallsAdapter.CallViewHolder>() {

    private var binding:ItemCallLayoutBinding? = null
    var currentUser = ""
    init {

        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    }

    inner class CallViewHolder(itemBinding: ItemCallLayoutBinding):
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object: DiffUtil.ItemCallback<Calls>(){
        override fun areItemsTheSame(oldItem: Calls, newItem: Calls): Boolean {
            return oldItem.time == newItem.time
        }


        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Calls, newItem: Calls): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {

        binding = ItemCallLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return CallViewHolder(binding!!)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val call = differ.currentList[position]
        holder.itemView.apply {
            if(call.sender == currentUser){
                binding?.tvName?.text = call.receiverName
            }else{
                binding?.tvName?.text = call.senderName
            }

         //   Glide.with(this).load(user.profile).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding?.ivRound!!)
            //   binding?.ivRound?.setI
        binding?.tvCallTime?.text = "${timeDay(call.time)}, ${formatTimeWithSimpleDateFormat(call.time)}"
    if(call.sender != FirebaseAuth.getInstance().currentUser!!.uid){
        binding?.ivArrow?.setImageResource(R.drawable.baseline_arrow_downward)
    }
            binding?.callBtn?.setOnClickListener {
                if(fragment is CallsFragment){
                   // fragment.sendVideoCallNotification(call.receiver,call.sender,call.type)
                }
            }
            if(call.type == "start_video_call"){
                binding?.callBtn?.setImageResource(R.drawable.baseline_videocam)
            }else{
                binding?.callBtn?.setImageResource(R.drawable.baseline_call_24)
            }


        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
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
            daysBetween == 0L -> "Today"  //convertMillisToTime(time)
            daysBetween == 1L -> "Yesterday"
            else -> "Other day"
        }
        return result
    }

    fun formatTimeWithSimpleDateFormat(timestamp: Long): String {
        // Create a Date object from the timestamp
        val date = Date(timestamp)

        // Define the desired date format
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Set the time zone to the system's default time zone
        format.timeZone = TimeZone.getDefault()

        // Format the date into the desired string format
        return format.format(date)
    }

}