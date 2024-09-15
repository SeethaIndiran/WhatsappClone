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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.databinding.ItemLayoutRecentUpdatesBinding
import com.example.chattingapp.fragments.StatusFragment
import com.example.chattingapp.models.Status
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class ViewedUpdatesAdapter(private val fragment:Fragment):RecyclerView.Adapter<ViewedUpdatesAdapter.ViewedUpdatesViewHolder>() {

    private var binding: ItemLayoutRecentUpdatesBinding? = null

    inner class ViewedUpdatesViewHolder(itemBinding: ItemLayoutRecentUpdatesBinding):
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object: DiffUtil.ItemCallback<Status>(){
        override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem.userId == newItem.userId
        }


        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewedUpdatesViewHolder {

        binding = ItemLayoutRecentUpdatesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewedUpdatesViewHolder(binding!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewedUpdatesViewHolder, position: Int) {
        val user = differ.currentList[position]
//        val image = user.imageUrls[0]
        holder.itemView.apply {
            binding?.tvUpdateUsername?.text = user.userName
            if(user.imageUrls.isNotEmpty()) {
                Glide.with(this).load(user.imageUrls[0])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding?.roundedImageView!!)
            }
               binding?.tvUpdateTime?.text = timeDay(user.time)
            setOnClickListener {
                if(fragment is StatusFragment){
                    fragment.navigateToViewedStoriesViewFragment(user, position)
                }
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
            daysBetween == 0L -> convertMillisToTime(time)
            daysBetween == 1L -> "Yesterday"
            else -> "Other day"
        }
        return result
    }
}