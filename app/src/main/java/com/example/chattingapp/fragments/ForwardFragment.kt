package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.adapters.UsersAdapter
import com.example.chattingapp.databinding.FragmentForwardBinding
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.Users
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_chat.view.cl_chat
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class ForwardFragment : Fragment() {

    private lateinit var binding:FragmentForwardBinding
    private val viewmodel: UserViewmodel by viewModels()
    private lateinit var usersAdapter: UsersAdapter
     private var usersList = mutableListOf<Users>()
    private var frUser =""
    private var selectedChats = mutableListOf<Chat>()
    private val activity:Activity = MessageChatActivity()
    private var newChatsLit = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForwardBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         frUser = FirebaseAuth.getInstance().currentUser!!.uid

            viewmodel.getAllUsers(frUser)
            observeViewModel()



         selectedChats = arguments!!.getSerializable("selectedItems") as MutableList<Chat>

        val fileUriString = arguments?.getString("image_data")
        val fileUri = fileUriString?.let { Uri.parse(it) } // Convert back to Uri if not null

        fileUri?.let {

        }

        binding.backBtn.setOnClickListener {
//            chatsAdapter!!.list.clear()
            activity!!.onBackPressed()
        }



    }
    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("MMM dd,yyyy")
        val fs = sdf.format(date)
        return fs
    }
    private fun setUpRecyclerView(){
        usersAdapter  = UsersAdapter(this@ForwardFragment)
        binding.rvRecent.adapter = usersAdapter
        binding.rvRecent.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL,false)
        binding.rvRecent.setHasFixedSize(true)
    }
    private fun observeViewModel(){

        lifecycle.coroutineScope.launchWhenCreated {
            viewmodel.users.collectLatest {
                if(it.isLoading){

                }
                if(it.error.isNotBlank()){

                }
                it.data.let {
                    setUpRecyclerView()
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    FirebaseMessaging.getInstance().subscribeToTopic("/topic/$userId")
                    usersAdapter.differ.submitList(it)
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleSelection(item: Users, holder:ViewHolder){
        if(usersList.contains(item)){
            usersList.remove(item)
            holder.itemView.cl_chat.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        }else{
            usersList.add(item)
            holder.itemView.cl_chat.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.transparentBlue))
        }
        val namesList = mutableListOf<String>()
        if(usersList.isNotEmpty()){
            for(user in usersList){
                namesList.add(user.username)
            }
            val namesString  = namesList.joinToString { "," }
            binding.clForward.visibility = View.VISIBLE
            binding.tvForward.text = namesString

            binding.forwardBtnBottom.setOnClickListener {

                for (userIndex in usersList.indices) {
                        val user = usersList[userIndex]

                        for (chatIndex in selectedChats.indices) {
                            val chat = selectedChats[chatIndex]

                              val newChat = Chat(frUser,chat.message,user.uid,chat.isSeen,chat.url,chat.messageKey,chat.time,chat.clickedNum,chat.isSelected)
                            newChatsLit.add(newChat)
                             // viewmodel.sendMessage(frUser,user.uid,chat.message,dateConversion(System.currentTimeMillis()),chat.url)
                        }
                        viewmodel.sendMultipleChats(dateConversion(System.currentTimeMillis()),newChatsLit)
                    }




                val size = usersList.size
                val userIdVisit = usersList[size - 1].uid
                val bundle = Bundle().apply {
                    putSerializable("id", userIdVisit)
                }
                val intent = Intent(requireContext(), MessageChatActivity::class.java).apply {
                    putExtras(bundle)
                }
                /*if(activity is MessageChatActivity){
                    activity.observeViewmodel()
                }*/
                startActivity(intent)
            }
          //  (activity as? MainActivity)?.


        }



        }

    }

