package com.example.chattingapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.databinding.ItemChatBinding
import com.example.chattingapp.fragments.ForwardFragment
import com.example.chattingapp.fragments.SearchFragment
import com.example.chattingapp.models.Users
import java.util.*

class UsersAdapter(private val fragment: Fragment):RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var binding:ItemChatBinding? = null

    inner class UserViewHolder(itemBinding: ItemChatBinding):
        RecyclerView.ViewHolder(itemBinding.root)

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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return UserViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
             val user = differ.currentList[position]
        holder.itemView.apply {
            binding?.tvName?.text = user.username
            Glide.with(this).load(user.profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding?.ivRound!!)
         //   binding?.ivRound?.setI
            setOnClickListener {
                if(fragment is SearchFragment){
                    fragment.navigateToMessageChatFragment(user.uid)
                }
                if(fragment is ForwardFragment){
                       fragment.toggleSelection(user,holder)
                }
            }

        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
             return differ.currentList.size
    }


}