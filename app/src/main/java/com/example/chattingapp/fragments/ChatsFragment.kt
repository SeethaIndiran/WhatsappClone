package com.example.chattingapp.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.adapters.ChatsFragmentAdapter
import com.example.chattingapp.databinding.FragmentChatsBinding
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatsList
import com.example.chattingapp.models.Users
import com.example.chattingapp.notifications.Token
import com.example.chattingapp.utilities.Constants
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private lateinit var binding:FragmentChatsBinding
    private  lateinit var refreshToken:String
    private  var firebaseUser: FirebaseUser? = null
    private val viewmodel: UserViewmodel by viewModels()
    private lateinit var chatsFragmentAdapter: ChatsFragmentAdapter
    var lastMsg = ""
    private  var userChatsList= mutableListOf<ChatsList>()
    private var usersList = mutableListOf<Users>()

    @Inject
    lateinit var sharedPref:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentChatsBinding.inflate(inflater,container,false)



        FirebaseMessaging.getInstance().token.addOnSuccessListener {
                result->
            result?.let {
                refreshToken = result
                updateToken(refreshToken!!)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userChatsList = ArrayList()

     //   setUpRecyclerView()

        viewmodel.getAllChatsList(firebaseUser!!.uid)
        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.chatsList.collectLatest {
                withContext(Dispatchers.Main){
                    if(!it.data.isNullOrEmpty()){

                        getAllChatsUsers(it.data)
                      //  chatsFragmentAdapter.notifyDataSetChanged()
                    }else{

                    }
                }
            }
        }

    }
    private fun updateToken(refreshToken: String) {
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val tokens = Token(refreshToken!!)

        ref.document(firebaseUser!!.uid).set(tokens)
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

    private fun getAllChatsUsers(list:List<ChatsList>){
        viewmodel.getAllChatUsers(list)
        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.users.collectLatest {
                withContext(Dispatchers.Main){
                 //   getLastChats(FirebaseAuth.getInstance().currentUser!!.uid,it.data!!)
                   setUpRecyclerView()
                    chatsFragmentAdapter.differ.submitList(it.data)
                   chatsFragmentAdapter.notifyDataSetChanged()

                }
            }
        }
    }
    private fun getLastChats(id:String,list:List<Users>){
        viewmodel.retrieveLastMessage(id,list)
        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.lastMsg.collectLatest {
                withContext(Dispatchers.Main){
                   setUpRecyclerView()
                 //   chatsFragmentAdapter.differ.submitList(it.chatDayList)
                    chatsFragmentAdapter.notifyDataSetChanged()
                }
            }
        }
    }
private fun setUpRecyclerView(){
    chatsFragmentAdapter = ChatsFragmentAdapter(this,true,viewmodel)
    binding.rvChatUsers.adapter = chatsFragmentAdapter
    binding.rvChatUsers.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
    binding.rvChatUsers.setHasFixedSize(true)
}
    fun navigateToMessageChatFragment(userIdVisit:String){
        //   onclickListener!!.onClick(userIdVisit)
        val bundle = Bundle().apply {
            putString("id",userIdVisit)
        }
        //   parentFragmentManager.setFragmentResult("userId",bundle)
        val msgFragment = MessageChatFragment()
        val intent  = Intent(requireContext(), MessageChatActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        //  findNavController().navigateUp()
        //  msgFragment.arguments = bundle
        //  fragmentManager!!.beginTransaction().replace(R.id.fragment_container,msgFragment).commit()

    }

}