package com.example.chattingapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapters.DateMessageAdapter
import com.example.chattingapp.databinding.FragmentTestBinding
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding
    private lateinit var msgAdapter:DateMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

    }
    private fun setUpRecyclerView() {
        val messages = listOf(
            Chat(FirebaseAuth.getInstance().currentUser!!.uid,"Hello there","",false,"","",0L,0,false),
          //  Message("Hi there!", MessageType.TEXT, false),
           // Message("How are you", MessageType.TEXT, true),
            Chat("","How are you","",false,"","",0L,0,false),
            Chat(FirebaseAuth.getInstance().currentUser!!.uid,"Hello there","",false,"/storage/emulated/0/Android/data/com.example.chattingapp/cache/audio_record_2024.09.262_06.55.52.mp3",
                "",0L,0,false),

            Chat("","see you soon","",false,"","",0L,0,false),
        Chat(FirebaseAuth.getInstance().currentUser!!.uid,"Hello there","",false,"/storage/emulated/0/Android/data/com.example.chattingapp/cache/audio_record_2024.09.262_06.55.52.mp3",
            "",0L,0,false)
          //  Message("/storage/emulated/0/Android/data/com.example.chattingapp/cache/audio_record_2024.09.262_06.55.52.mp3", MessageType.AUDIO, true),
           // Message("good", MessageType.TEXT, false),
          //  Message("/storage/emulated/0/Android/data/com.example.chattingapp/cache/audio_record_2024.09.262_06.55.52.mp3", MessageType.AUDIO, false),
        //    Message("See you soon", MessageType.TEXT, false),
         //   Message("all the bset", MessageType.TEXT, true),
          //  Message("God bless you", MessageType.TEXT, false),
         //   Message("https://example.com/image.jpg", MessageType.IMAGE, true)
        )
        val msgsTwo = listOf(
            Chat(FirebaseAuth.getInstance().currentUser!!.uid,"welcome baby","",false,"","",0L,0,false),
            //  Message("Hi there!", MessageType.TEXT, false),
            // Message("How are you", MessageType.TEXT, true),
            Chat("","bye bye bab","",false,"","",0L,0,false),
            Chat("","Hello there","",false,"/storage/emulated/0/Android/data/com.example.chattingapp/cache/audio_record_2024.09.262_06.55.52.mp3",
                "",0L,0,false),
        )
        val dateMsgList  =listOf( ChatDay(1726617600000,messages),ChatDay(1726704000000,msgsTwo))
      /*  msgAdapter = DateMessageAdapter(dateMsgList,FirebaseAuth.getInstance().currentUser!!,requireContext(),requireActivity(),this)
        binding.rv.adapter = msgAdapter
        binding.rv.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL,false)

        binding.rv.setHasFixedSize(true)*/
    }

}