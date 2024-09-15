package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.R
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.databinding.FragmentMyStatusBinding
import com.example.chattingapp.models.Status
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.options_dialog.view.delete
import kotlinx.android.synthetic.main.options_dialog.view.forward
import kotlinx.android.synthetic.main.options_dialog.view.share
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MyStatusFragment : Fragment() {
    private lateinit var binding:FragmentMyStatusBinding
    private var list:List<String> = ArrayList()
    var status:Status? = null
    private var currentUser:String? = null
    private val viewmodel:UserViewmodel by viewModels()
    private val statusAdapter:RecentUpdatesAdapter? = null
     val args:MyStatusFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentMyStatusBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // (activity as AppCompatActivity).supportActionBar?.title = "My Status"
       // (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        FirebaseAuth.getInstance().currentUser!!.uid.also { this.currentUser = it }
      //  if(arguments!= null && arguments!!.containsKey("my_status")) {
            // status = arguments!!.getSerializable("my_status") as Status
        status = args.status
            list = status!!.imageUrls
            if(list.isNotEmpty()){
            Glide.with(this).load(list[0])
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.roundedImageView)
        }
        //}

        viewmodel.retrieveCurrentUserStatus(currentUser!!)
        CoroutineScope(Dispatchers.IO).launch {

            viewmodel.singleStatus.collectLatest {
                withContext(Dispatchers.Main){
                    if(it.status!= null){
                        val views = it.status.viewedUsers
                        binding.tvUpdateUsername.text = "${views.size} views"
                    }
                }
            }

        }



        binding.clMyStatus.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("my_status",status)
            }
            val fragment = StoriesViewFragment()
            fragment.arguments=bundle
            val transaction =   activity!!.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.root_view,fragment)
            transaction.addToBackStack("myStatusFragment")
            transaction.commit()
        }
        binding.settingsBtn.setOnClickListener {
            showDialog()
        }
    }




    private fun showDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.options_dialog, null)
        val dialog = Dialog(activity!!)

        dialog.setContentView(dialogView)

        // Set onClickListeners for TextViews
        dialogView.forward.setOnClickListener {

            dialog.dismiss()
        }

        dialogView.delete.setOnClickListener {
          viewmodel.deleteStatus(currentUser!!)
       //  deleteList()
            dialog.dismiss()
            navigateBackToStatusFragment()
        }
        dialogView.share.setOnClickListener {

        }
        dialog.setOnShowListener {
            val window = dialog.window
            val layoutParams = window?.attributes
            layoutParams?.gravity = Gravity.TOP or Gravity.END // Position at top-right corner
            layoutParams?.x = resources.getDimensionPixelSize(R.dimen.dialog_margin_horizontal) // Adjust horizontal margin
            layoutParams?.y = resources.getDimensionPixelSize(R.dimen.dialog_margin_vertical) // Adjust vertical margin
            window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.attributes = layoutParams
        }



        dialog.show()
    }

private fun navigateBackToStatusFragment(){

    val bundle = Bundle().apply {
        putString("myStatus","myStatus")
    }
    val statusFragment = StatusFragment()
     statusFragment.arguments = bundle
    val transaction =   activity!!.supportFragmentManager.beginTransaction()
    transaction.replace(R.id.root_view,statusFragment)
    transaction.addToBackStack("myStatusFragment")
    transaction.commit()
  //  activity!!.supportFragmentManager.popBackStack()
    activity!!.supportFragmentManager.popBackStack("statusFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    (activity as AppCompatActivity).supportActionBar?.show()

    //findNavController().navigate(R.id.action_myStatusFragment_to_statusFragment)
}

private fun deleteList(){
    CoroutineScope(Dispatchers.IO).launch{
        viewmodel.status.collectLatest {
            withContext(Dispatchers.Main){
                if(it.data != null){
                   // statusAdapter!!.differ.submitList(it.data)
                    val posi = it.data.indexOfFirst { it.userId == currentUser }
                  //  statusAdapter!!.notifyItemRemoved(posi)

                }else{

                }
            }
        }
    }
}
}

