package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentMainBinding
import com.example.chattingapp.utilities.Constants
import com.example.chattingapp.utilities.Constants.GALLERY
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: UserViewmodel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var firebaseUser: FirebaseUser? = null
    private var unreadMsges: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "WhatsApp"
        firebaseUser = FirebaseAuth.getInstance().currentUser
        viewModel.getUnreadMessages(firebaseUser!!.uid)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.unreadMsg.collectLatest {
                withContext(Dispatchers.Main) {

                    unreadMsges = it.unreadMsgs!!

                    setUpTabBar(unreadMsges)

                }
            }
        }


        binding.flBtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                findNavController().navigate(R.id.action_mainFragment_to_searchFragment)
            }
        }


        // getToken()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.search -> {

            }

            R.id.camera -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(intent,
                    GALLERY
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpTabBar(unreadMsg: Int) {
        if (unreadMsg == 0) {
            val adapter = FragmentPagerItemAdapter(
                childFragmentManager,
                FragmentPagerItems.with(activity)
                    .add("Chats", ChatsFragment::class.java)
                    .add("Status", StatusFragment::class.java)
                    .add("Calls", CallsFragment::class.java)
                    .create()
            )
            binding.viewPager.adapter = adapter
            binding.smartTab.setViewPager(binding.viewPager)

        } else {
            val adapter = FragmentPagerItemAdapter(
                childFragmentManager,
                FragmentPagerItems.with(activity)
                    .add("($unreadMsg)Chats", ChatsFragment::class.java)
                    .add("Status", StatusFragment::class.java)
                    .add("Calls", CallsFragment::class.java)
                    .create()
            )
            binding.viewPager.adapter = adapter
            binding.smartTab.setViewPager(binding.viewPager)

        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                val fileUri = data!!.data
                fileUri?.let {
                    val bundle = Bundle().apply {
                        putString("image_data",fileUri.toString())
                    }
                    val fragment = ForwardFragment()
                    fragment.arguments=bundle
                    val transaction =   activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.root_view,fragment)
                    transaction.addToBackStack("myStatusFragment")
                    transaction.commit()
                }


            }

        }

    }


    }
    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("MMM dd,yyyy")
        val fs = sdf.format(date)
        return fs
    }



