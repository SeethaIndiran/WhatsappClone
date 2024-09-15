package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.adapters.ViewedUpdatesAdapter
import com.example.chattingapp.databinding.FragmentStatusBinding
import com.example.chattingapp.models.Status
import com.example.chattingapp.others.Constants.Companion.PICK_IMAGES_VIDEOS_REQUEST
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
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


@AndroidEntryPoint
class StatusFragment : Fragment() {

    private lateinit var leftView: ImageView
    private lateinit var rightView: ImageView
    private val viewmodel:UserViewmodel by viewModels ()
    private lateinit var statusAdapter: RecentUpdatesAdapter
    private lateinit var statusViewedAdapter: ViewedUpdatesAdapter
    private var currentUser:String =""
    private var currentUsername = ""
    var imageView:ImageView?  = null
    private var position:Int = 0
    private lateinit var binding:FragmentStatusBinding
    var myStatus:Status? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentStatusBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         imageView = binding.roundedImageView

        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        viewmodel.getAllStatus(currentUser)
        viewmodel.getAllViewedStatus(currentUser)
        viewmodel.retrieveUser(currentUser)
        observeViewmodel()
        setUpRecyclerView()
        userStatus()



        setUpViewedStatus()
        observeUserStatus()

        binding.clClick.setOnClickListener {
           // observeUserStatus()
            if(myStatus == null){
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    startActivityForResult(intent, PICK_IMAGES_VIDEOS_REQUEST)
            }
            else{
                (activity as MainActivity?)?.hideToolbar()
                val bundle = Bundle().apply {
                    putSerializable("my_status",myStatus)
                }
              /*  val fragment = MyStatusFragment()
                fragment.arguments=bundle
                val transaction =   activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.root_view,fragment)
                transaction.addToBackStack("statusFragment")
                transaction.commit()*/
                if(findNavController().currentDestination?.id == R.id.statusFragment){
                findNavController().navigate(R.id.action_statusFragment_to_myStatusFragment,bundle)
                }
            }
        }

        if(arguments!= null && arguments!!.containsKey("myStatus")){
            val args = arguments!!.getSerializable("myStatus")
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun userStatus(){

        CoroutineScope(Dispatchers.IO).launch{
            viewmodel.status.collectLatest {
                withContext(Dispatchers.Main){
                    if(it.data != null){

                       // binding.rvStatus.itemAnimator = null
                      //  val posi = it.data.indexOfFirst { it.userId == currentUser }
                     //   statusAdapter.notifyDataSetChanged()
                        setUpRecyclerView()
                        statusAdapter.differ.submitList(it.data)
                        binding.rvStatus.visibility =View.VISIBLE
                        binding.tvUpdate.visibility =View.VISIBLE

                    }else{
                        binding.rvStatus.visibility =View.GONE
                        binding. tvUpdate.visibility  =View.GONE
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGES_VIDEOS_REQUEST && resultCode ==Activity.RESULT_OK) {
            val selectedFiles = mutableListOf<Uri>()

            // Check if multiple files are selected
            if (data?.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedFiles.add(uri)
                }

                viewmodel.uploadStatusImagesToFirebaseStorage(currentUser,currentUsername,selectedFiles,statusAdapter)

            } else {
                // Single file selected
                val uri = data?.data
                if (uri != null) {
                    selectedFiles.add(uri)
                  //  setDashedBorder(imageView!!,selectedFiles.size)
                    viewmodel.uploadStatusImagesToFirebaseStorage(currentUser,currentUsername,selectedFiles,statusAdapter)

                }
            }

        }
    }
    private fun observeViewmodel(){
        lifecycle.coroutineScope.launchWhenCreated {
            viewmodel.singleUser.collectLatest {

                if(it.error.isNotBlank()){

                }
              currentUsername = it.userVisit.username


            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeUserStatus(){
        viewmodel.retrieveCurrentUserStatus(currentUser)

        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.singleStatus.collectLatest {
                withContext(Dispatchers.Main) {
                    if(it.status!= null) {
                        binding.smallRoundedImageView.visibility = View.GONE
                        binding.tvMyStatus.text ="My status"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.tvTap.text=timeDay(it.status.time)
                        }
                        setDashedBorder(imageView!!, it.status.imageUrls.size)
                       myStatus =  it.status
                        // Set the new ShapeDrawable as the background of the ImageView
                    }else{
                        myStatus = null
                        binding.smallRoundedImageView.visibility = View.GONE
                        binding.roundedImageView.background = ContextCompat.getDrawable(activity!!,R.drawable.rounded_image_bgnd)
                        binding.tvTap.text = "Tap to add status update"
                    }
                }
            }
        }
    }
@SuppressLint("SuspiciousIndentation")
fun navigateToStoriesViewFragment(status: Status, position: Int){
    val bundle = Bundle().apply {
        putSerializable("status",status)
    }
    viewmodel.updateClickedStatus(status.userId,currentUser)
    val  list = statusAdapter.differ.currentList.toMutableList()
    list.removeAt(position)
    statusAdapter.notifyItemRemoved(position)
   //statusAdapter.notifyDataSetChanged()
  //  statusAdapter.differ.submitList(list)
    binding.rvStatus.adapter = statusAdapter
    statusAdapter.differ.submitList(list)

    // parentFragmentManager.setFragmentResult("userId",bundle)
    val storyFragment = StoriesViewFragment()
    val statusFragment = StatusFragment()
   /* if(findNavController().currentDestination?.id == R.id.statusFragment){
        val action = StatusFragmentDirections.actionStatusFragmentToStoriesViewFragment(status)
    findNavController().navigate(action)}*/
       // findNavController().navigate(R.id.action_statusFragment_to_storiesViewFragment,bundle)}
   storyFragment.arguments = bundle
  val transaction =   activity!!.supportFragmentManager.beginTransaction()
       transaction.replace(R.id.root_view,storyFragment)
    transaction.addToBackStack("statusFragment")
    transaction.commit()
  /*  val intent  =Intent(requireContext(), StoryActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)*/
}

    fun navigateToViewedStoriesViewFragment(status: Status, position: Int){
        val bundle = Bundle().apply {
            putSerializable("status",status)
        }


        // parentFragmentManager.setFragmentResult("userId",bundle)
        val storyFragment = StoriesViewFragment()


        storyFragment.arguments = bundle
        val transaction =   activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.root_view,storyFragment)
        transaction.addToBackStack("statusFragment")
        transaction.commit()

    }

    private fun setUpRecyclerView() {
        statusAdapter = RecentUpdatesAdapter(this@StatusFragment)
        binding.rvStatus.adapter = statusAdapter
        binding.rvStatus.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)

        binding.rvStatus.setHasFixedSize(true)
    }

    private fun setUpRecyclerViewForViewedStatus(){
        statusViewedAdapter  = ViewedUpdatesAdapter(this@StatusFragment)
        binding.rvStatusViewed.adapter = statusViewedAdapter
        binding.rvStatusViewed.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL,false)
        binding.rvStatusViewed.setHasFixedSize(true)
    }

    private fun setUpViewedStatus(){
     setUpRecyclerViewForViewedStatus()
        CoroutineScope(Dispatchers.IO).launch{
            viewmodel.statusViewed.collectLatest {
                withContext(Dispatchers.Main){
                    if(it.data != null){
                        binding.rvStatusViewed.visibility = View.VISIBLE
                        binding.tvUpdateViewed.visibility =View.VISIBLE
                        statusViewedAdapter.differ.submitList(it.data)
                    }else{
                        binding.rvStatusViewed.visibility = View.GONE
                        binding.tvUpdateViewed.visibility =View.GONE
                    }
                }


            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setDashedBorder(imageView: ImageView, numberOfDashLines: Int) {
        // Calculate dash length based on the number of items in the list
        val totalDashSpace = imageView.width.toFloat()
        val dashWidth = (totalDashSpace) / ( numberOfDashLines/2.5).toFloat()
        val dashGap = dashWidth/5

        // Create a new ShapeDrawable with the desired path effect
        val shapeDrawable = ShapeDrawable()
        shapeDrawable.shape = OvalShape()
        shapeDrawable.paint.color = Color.parseColor("#128c7e")
        shapeDrawable.paint.style = Paint.Style.STROKE
        shapeDrawable.paint.strokeWidth = 6f
        shapeDrawable.paint.pathEffect = DashPathEffect(
            floatArrayOf(dashWidth, dashGap),
            0f
        )

        // Set the new ShapeDrawable as the background of the ImageView
        imageView.background = shapeDrawable


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

    interface BackNavigationListener {
        fun onBackToA()
    }


}




