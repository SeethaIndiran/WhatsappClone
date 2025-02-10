package com.example.chattingapp

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.adapters.ChatsAdapter
import com.example.chattingapp.adapters.DateMessageAdapter
import com.example.chattingapp.databinding.ActivityNewMessageBinding
import com.example.chattingapp.firebasevideocall.service.MainServiceRepository
import com.example.chattingapp.fragments.ForwardFragment
import com.example.chattingapp.fragments.ImageVideoFragment
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.ChatDay
import com.example.chattingapp.models.Timer
import com.example.chattingapp.models.Users
import com.example.chattingapp.models.WaveformView
import com.example.chattingapp.notifications.ApiService
import com.example.chattingapp.notifications.Client
import com.example.chattingapp.notifications.DataCall
import com.example.chattingapp.notifications.MyResponse
import com.example.chattingapp.notifications.OAuth2Util
import com.example.chattingapp.notifications.Sender
import com.example.chattingapp.notifications.Token
import com.example.chattingapp.others.Constants.Companion.ACTION_MSG_CHAT_ACTIVITY
import com.example.chattingapp.others.MediaPlayerManager
import com.example.chattingapp.utils.AccessToken
import com.example.chattingapp.utils.FirebaseRepository
import com.example.chattingapp.utils.GetAccessToken
import com.example.chattingapp.utils.getCameraAndMicPermission
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_record.play_btn
import kotlinx.android.synthetic.main.bottom_sheet_record.record_btn
import kotlinx.android.synthetic.main.bottom_sheet_record.tv_timer
import kotlinx.android.synthetic.main.bottom_sheet_record.waveform
import kotlinx.android.synthetic.main.fragment_message_chat.audio_recorder_big
import kotlinx.android.synthetic.main.fragment_message_chat.cl_bottom
import kotlinx.android.synthetic.main.fragment_message_chat.cl_drag_ver
import kotlinx.android.synthetic.main.fragment_message_chat.seek_bar
import kotlinx.android.synthetic.main.fragment_message_chat.tv_slide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class MessageChatActivity : AppCompatActivity(),Timer.OnTimerTickListener ,ChatsAdapter.onClickListener, DateMessageAdapter.OnSelectionChangeListener{


    private lateinit var binding: ActivityNewMessageBinding
    private val viewmodel: UserViewmodel by viewModels()
    private var userIdVisit:String = ""
    private var userIdVisitName:String = ""
    private var currentUser:String = ""
    private  var mediaRecorder: MediaRecorder? = null
    private  var audioFilePath: String? = null
    private var dirPath:String = ""
    private var fileName:String = ""
    private var isRecording = false
    private var isPaused = false
    private var files: File? = null
    //val cacheDir: File? = null


    private var initialX = 0f
    private var initialY =0f
    private var offsetX:Float? = null
    private var offsetY:Float? = null
    private var isDragHor = false
    private var isDragVer = false
    private var isDeleteOrSent = false


    private var animator: ValueAnimator? = null
    private lateinit var timer: Timer
    private lateinit var dialogBottom: BottomSheetDialog
    private lateinit var bottomDialogLayout: View
    private lateinit var seekBar: SeekBar
    //  private lateinit var waveform:WaveformView
    private  lateinit var mediaPlayer: MediaPlayer
    private  var handler: Handler? = null
    private  var runnable: Runnable? = null
    private var delay = 1000L

    private var chatsDayAdapter: DateMessageAdapter= DateMessageAdapter(emptyList(),null,this@MessageChatActivity,this,this@MessageChatActivity)
    private lateinit var chatAdapter: ChatsAdapter
    private var chatList = mutableListOf<Chat>()
    private var chatDayList = mutableListOf<ChatDay>()
    private var selectedListSize  = 0

  //  var fragment: Fragment = this

    private var contextWrapper: ContextWrapper? = null
    var apiService: ApiService? = null
    var currentUserName:String =""
    var topic = ""
    var notify = false
    var isFirstMessage = false
    val msgAdapter = ChatsAdapter(this,FirebaseAuth.getInstance().currentUser!!,this)

    private var firebaseRepository:FirebaseRepository?=null
    private lateinit var mediaPlayerManager: MediaPlayerManager
    private var accessToken = ""

    @Inject
    lateinit var serviceRepository: MainServiceRepository
    private var selectedChats = mutableListOf<Pair<Int, ChatsAdapter.ChatViewHolderInterface>>()
   var selectedList = mutableListOf<ViewHolder>()
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        timer = Timer(this)
        chatAdapter = ChatsAdapter(this@MessageChatActivity,FirebaseAuth.getInstance().currentUser!!,this)

        //  waveform  = WaveformView(requireContext())
        dialogBottom = BottomSheetDialog(this)
        bottomDialogLayout = layoutInflater.inflate(R.layout.bottom_sheet_record,null)
        //  seekBar = bottomDialogLayout.findViewById(R.id.seek_bar)
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        getUserId()



        observeViewmodel()

      /*  CoroutineScope(Dispatchers.IO).launch {
            viewmodel.chats.collectLatest { chatState ->
                withContext(Dispatchers.Main) {
                  //  chatDayList.clear()
                    chatState.data?.let { newChats ->
                        // Assuming chatDayList is a list of Chats
                      //  chatDayList.clear()
                        val startPosition = chatDayList.size // Position to insert the new item
                        chatDayList.addAll(newChats) // Add all new chats to chatDayList
                       chatsAdapter.differ.submitList(chatDayList)
                        // Notify adapter about the insertion
                        chatsAdapter.notifyItemRangeInserted(startPosition, newChats.size)
                    }
                }
            }
        }*/

        /*   CoroutineScope(Dispatchers.IO).launch {
               viewmodel.chats.collectLatest {

                     withContext(Dispatchers.Main) {
                   chatDayList.clear()
                   chatDayList.addAll(it.data!!)
                   chatsAdapter.differ.submitList(chatDayList)
                     chatsAdapter.notifyDataSetChanged()
                     }
               }
           }*/

        apiService = Client.Client.getCloient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)


        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid






     /*   viewmodel.getAllChats(currentUser,userIdVisit)
        CoroutineScope(Dispatchers.IO).launch{
            viewmodel.chats.collectLatest {
                withContext(Dispatchers.Main){
                    setUpRecyclerView()
                    chatsAdapter.differ.submitList(it.data)
                }
            }
        }*/





        binding.audioRecorder.setOnClickListener {
            val msg = binding.etSendMsg.text.toString()
            if(msg == ""){
                Toast.makeText(this,"Please type a message..", Toast.LENGTH_SHORT).show()
            }else {
                  val chatDate = dateConversion(System.currentTimeMillis())

                viewmodel.sendMessage(currentUser,userIdVisit,msg,chatDate,"")
              //  chatsAdapter.differ.submitList(chatDayList)
             //   msgAdapter.setChanged()
             //   chatsAdapter.notifyDataSetChanged()
                  // msgAdapter.setChanged()
                binding.etSendMsg.setText("")
                FirebaseFirestore.getInstance().collection("Users")
                    .document(currentUser).get().addOnSuccessListener {
                        if(it.exists()){
                            val user = it.toObject(Users::class.java)
                            val username = user!!.username

                                //sendNotification(userIdVisit,username,msg)

                           }
                    }
            }
        }


        binding.gallery.setOnClickListener {

            Dexter.withContext(this).withPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        val galleryIntent = Intent(Intent.ACTION_PICK)
                        galleryIntent.type = "image/*" // Allow selecting both images and videos

                        startActivityForResult(galleryIntent, GALLERY)


                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        // showRationalDialogForPermissions()
                    }

                }).onSameThread().check()


        }

        binding.audioRecorder.setOnLongClickListener {
            if (!isRecording) {

                binding.clDrag.visibility = View.VISIBLE
                binding.clDragVer.visibility = View.VISIBLE
                binding.audioRecorder.visibility = View.INVISIBLE
                binding.clMsg.visibility = View.INVISIBLE
                binding.tvScroll.visibility = View.VISIBLE
                startRecording()
            }
            true
        }
        /* binding.audioRecorder.setOnTouchListener{ v,event ->
             when(event.action){
                 MotionEvent.ACTION_UP ->{
                     stopRecording()
                     binding.clDrag.visibility = View.INVISIBLE
                     binding.clDragVer.visibility = View.INVISIBLE
                     binding.audioRecorder.visibility = View.VISIBLE
                     binding.clMsg.visibility = View.VISIBLE
                     binding.tvScroll.visibility = View.INVISIBLE
                 }
             }
             false
         }*/



        binding.camera.setOnClickListener {
            // stopRecording()
            Dexter.withContext(this).withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(object:
                MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let{
                        if(report.areAllPermissionsGranted()){
                                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                               startActivityForResult(intent,CAMERA)
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?) {
                    //  showRationalDialogForPermissions()
                }

            }).onSameThread().check()

        }

        audio_recorder_big?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y

                }
                MotionEvent.ACTION_MOVE -> {

                    binding.audioRecorder.visibility = View.INVISIBLE

                    offsetX = event.x - initialX
                    tv_slide?.translationX = offsetX!!
                    audio_recorder_big?.translationX = offsetX!!
                    cl_drag_ver.translationY = -offsetX!!

                    offsetY = event.y - initialY
                    cl_drag_ver.translationY = offsetY!!
                    audio_recorder_big.translationY = offsetY!!

                    if(abs(offsetX!!) > abs(offsetY!!)) {
                        isDragHor = true

                    }else{
                        isDragHor = false
                        isDragVer = true

                    }

                }
                MotionEvent.ACTION_UP -> {

                    tv_slide?.translationX = 0f
                    audio_recorder_big?.translationX = 0f
                    audio_recorder_big.translationY = 0f
                    cl_drag_ver.translationY =0f
                    binding.clDrag.visibility = View.INVISIBLE
                    binding.tvScroll.visibility = View.INVISIBLE
                    binding.clDragVer.visibility = View.INVISIBLE

                    if(isDragHor){
                        binding.clMsg.visibility = View.VISIBLE
                        binding.audioRecorder.visibility = View.VISIBLE
                    }else{
                        binding.clMsg.visibility = View.INVISIBLE
                        binding.audioRecorder.visibility = View.INVISIBLE
                        bottomLayout()


                    }

                    //  isRecording = false
                    isDragHor = false
                    isDragVer = false
                    // stopRecording()
                }
            }
            true
        }








binding.videoCallBtn.setOnClickListener {
    getCameraAndMicPermission{
      //  val cuuser = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("Users").document(currentUser).get().addOnSuccessListener {
                if(it.exists()){
                    val user = it.toObject(Users::class.java)
                     currentUserName = user!!.username

                    sendVideoCallNotification(userIdVisit,currentUserName,"start_video_call")
                    viewmodel.saveCalls(currentUserId,userIdVisit,currentUserName,userIdVisitName,"start_video_call",System.currentTimeMillis())
 Log.i("visitIdName",userIdVisitName)

                }
            }
    }

}
binding.audioCallBtn.setOnClickListener {
    getCameraAndMicPermission{

        FirebaseFirestore.getInstance().collection("Users").document(currentUser).get().addOnSuccessListener {
            if(it.exists()){
                val user = it.toObject(Users::class.java)
                val username = user!!.username

                sendVideoCallNotification(userIdVisit,username,"start_audio_call")
                viewmodel.saveCalls(currentUserId,userIdVisit,currentUserName,userIdVisitName,"start_audio_call",System.currentTimeMillis())

            }
        }
    }
}
binding.chatSettingsBtn.setOnClickListener {

}

        seenMessage(userIdVisit)


    }
    fun playSeekbar(audioFilePaths: String){

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer.currentPosition
            handler!!.postDelayed(runnable!!, delay)
        }
        handler!!.post(runnable!!)

        if (audioFilePath != null) {
            mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(audioFilePath)
                setOnPreparedListener {
                    binding.seekBar.max = it.duration

                    it.start()

                }
                prepareAsync()
            }
        } else {
            Toast.makeText(this@MessageChatActivity, "No audio file found", Toast.LENGTH_SHORT).show()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle actions when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Handle actions when touch stops
            }
        })

        mediaPlayer.setOnCompletionListener {
            binding.playBtn.background = ContextCompat.getDrawable(this, R.drawable.round_play_arrow_24)
            handler!!.removeCallbacks(runnable!!)
        }
    }



     fun playAndPausePlayer(){

        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            binding.playBtn.background = ContextCompat.getDrawable(this,R.drawable.baseline_pause_circle_24)
            handler!!.postDelayed(runnable!!,delay)
        }else{
            mediaPlayer.pause()
            binding.playBtn.background = ContextCompat.getDrawable(this,R.drawable.round_play_arrow_24)
            handler!!.removeCallbacks(runnable!!)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        serviceRepository.stopService()
    }



    fun navigateToForwardfragment(){
        chatList = chatsDayAdapter.getSelectedChats().toMutableList()

        // Count the total number of selected messages across all ChatDays
       // val totalSelectedMessages = selectedChats.sumBy { it.chats.size }

        val selectedItems = chatList.size
      //  val num = selectedList.size
        if(selectedItems>=1 ){
            binding.tvNumberOfMsgs.visibility = View.VISIBLE
            binding.tvNumberOfMsgs.text = chatList.size.toString()
            binding.deleteSelect.visibility = View.VISIBLE
            binding.forwardSelect.visibility = View.VISIBLE
            binding.videoCallBtn.visibility = View.GONE
            binding.audioCallBtn.visibility = View.GONE
            binding.chatSettingsBtn.visibility = View.GONE
            binding.tvToolbarTitle.visibility = View.GONE
            binding.tvToolbarContacts.visibility = View.GONE
            binding.videoCallBtn.visibility = View.GONE
            binding.ivRound.visibility =View.GONE

            binding.forwardSelect.setOnClickListener {
                val bundle = Bundle()
                bundle.apply {
                    changeBackground(chatList)
                    putSerializable("selectedItems",chatList as Serializable)
                }
                val fragment = ForwardFragment()
                fragment.arguments = bundle
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.cl, fragment)
                transaction.addToBackStack(null)
                    .commit()


            }
            binding.backBtn.setOnClickListener {
               chatsDayAdapter.clearSelection(chatList)

                showSeletedView()
            }


        }else{
           showSeletedView()
        }


   }

private fun showSeletedView(){
    binding.tvNumberOfMsgs.visibility = View.GONE

    binding.deleteSelect.visibility = View.GONE
    binding.forwardSelect.visibility = View.GONE
    binding.videoCallBtn.visibility = View.VISIBLE
    binding.audioCallBtn.visibility = View.VISIBLE
    binding.chatSettingsBtn.visibility = View.VISIBLE
    binding.tvToolbarTitle.visibility = View.VISIBLE
    binding.tvToolbarContacts.visibility = View.VISIBLE
    binding.videoCallBtn.visibility = View.VISIBLE
    binding.ivRound.visibility =View.VISIBLE
}

     @RequiresApi(Build.VERSION_CODES.O)
     fun toggleSelection(item:Chat,holder: ViewHolder){


         if (selectedList.contains(holder) ) {

             selectedList.remove(holder)
             chatList.remove(item)
             holder.itemView.setBackgroundColor(Color.parseColor("#00FFFFFF"))

         } else {

             selectedList.add(holder)
             chatList.add(item)

             holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.transparentBlue))

         }

     }

    private fun changeBackground(list:List<Chat>){
      for(chat in list){
          chat.isSelected = false
      }
       /* if(selectedList.isNotEmpty()){
            for(item in selectedList){
                item.itemView.setBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }

  selectedList.clear()
        chatList.clear()*/
     }






    @SuppressLint("SuspiciousIndentation")
    private fun stopRecording() {


        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
          //  handler.removeCallbacks(runnable)
            isRecording = false
            timer.stop()
            val uniqueId = UUID.randomUUID().toString()
            val fileName = "recordingAudio_$uniqueId.mp3"
          //  files = File(Environment.getExternalStorageDirectory().absolutePath + "/" + fileName)
         //   audioFilePath = files!!.absolutePath
              val date = dateConversion(System.currentTimeMillis())
               viewmodel.uploadAudioFileToFirebaseStorage(currentUser,userIdVisit,audioFilePath!!,date)


         //   viewmodel.getAllChats(currentUser,userIdVisit,
               // FirebaseAuth.getInstance().currentUser!!,userIdVisit)
            //  observeViewModelForChats()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startRecording() {


        if (CheckPermission()) {
            mediaRecorder = MediaRecorder()

            dirPath = "${externalCacheDir!!.absolutePath}/"
            val sdf = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
            val date = sdf.format(Date())
            fileName = "audio_record_$date"
            audioFilePath = "$dirPath$fileName.mp3"
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(audioFilePath)


            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                isRecording = true
                isPaused = false
                timer.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording(){
        mediaRecorder!!.pause()
        isPaused = true
        timer.pause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecordingBottom(){
        mediaRecorder!!.resume()
        isPaused = false
        timer.start()
    }


    /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         inflater.inflate(R.menu.menu_chat,menu)
         super.onCreateOptionsMenu(menu, inflater)
     }

     override fun onOptionsItemSelected(item: MenuItem): Boolean {

         when(item.itemId){
                R.id.voice_call ->{
                    startService(currentUser)
                }
         }
         return super.onOptionsItemSelected(item)
     }
 */

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
     fun observeViewmodel(){
        lifecycle.coroutineScope.launchWhenCreated {
            viewmodel.singleUser.collectLatest {

                if(it.error.isNotBlank()){

                }
                userIdVisit = it.userVisit.uid
                binding.tvToolbarTitle.text = it.userVisit.username

                Glide.with(this@MessageChatActivity)
                    .load(it.userVisit.profile)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivRound)


                userIdVisitName = it.userVisit.username
                viewmodel.getAllChats(currentUser,userIdVisit)
                CoroutineScope(Dispatchers.IO).launch {
                    viewmodel.chats.collectLatest {

                        withContext(Dispatchers.Main) {
                            chatDayList.clear()
                            chatDayList.addAll(it.data!!)
                            setUpRecyclerView(chatDayList)
                          //  chatsDayAdapter.differ.submitList(chatDayList)
                           // chatsDayAdapter.notifyDataSetChanged()
                        }
                    }
                }

         //    seenMessage(userIdVisit)



            }
        }
    }
    fun copyFileToInternalStorage(uri: Uri, contentResolver: ContentResolver): Uri? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            // Create a file in internal storage
            val file = File(this.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            // Write the data from InputStream to the OutputStream
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            // Close the streams
            outputStream.close()
            inputStream.close()

            // Return the Uri of the saved file
            return Uri.fromFile(file)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }



    private fun setUpRecyclerViewTest() {
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
     val  msgAdapter = DateMessageAdapter(dateMsgList,FirebaseAuth.getInstance().currentUser!!,this,this,this@MessageChatActivity)
        binding.recyclerView.adapter = msgAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)

        binding.recyclerView.setHasFixedSize(true)
    }


    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("MMM dd,yyyy")
        val fs = sdf.format(date)
        return fs
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    private fun getUserId(){
        //   parentFragmentManager.setFragmentResultListener("userId",this, FragmentResultListener { requestKey, result ->
        //      val id = result.getString("id")
      //  val args = this.arguments
        val intent = intent.extras
       val visitUserUId  = intent!!.get("id")
        viewmodel.retrieveUser(visitUserUId.toString())

        observeViewmodel()
       // setVoiceCall(visitUserUId.toString())
      //  setVideoCall(visitUserUId.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode == Activity.RESULT_OK ){
            if(requestCode == GALLERY || requestCode == CAMERA ){
                val fileUri = data!!.data
                fileUri?.let {
                    val date = dateConversion(System.currentTimeMillis())
                    if(it.toString().contains("image")){

                        viewmodel.uploadImageToFirebaseStorage(currentUser,userIdVisit,it,date)
                    //    viewmodel.getAllChats(currentUser,userIdVisit)
                     //   observeViewModelForChats()

                    }else if(it.toString().contains("video")){
                        viewmodel.uploadVideoFileToFirebaseStorage(currentUser,userIdVisit,it,date)

                    }


                }

            }

        }


    }
    private fun CheckPermission(): Boolean {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }

        return false

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {



        requestPermissions(
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            101
        )
    }



    @RequiresApi(Build.VERSION_CODES.N)
    private fun bottomLayout(){

      //  playSeekbar(audioFilePath!!)
        binding.clBottom.visibility = View.VISIBLE
        startRecording()
        binding.recordBtn.setOnClickListener {
            if(!isPaused){
                pauseRecording()
                record_btn.setImageResource(R.drawable.baseline_mic_24)
              //  tv_timer.visibility = View.INVISIBLE
              //  waveform.visibility = View.INVISIBLE
              //  play_btn.visibility = View.VISIBLE
             //   seek_bar.visibility =View.VISIBLE



                binding.playBtn.setOnClickListener {
                    playSeekbar(audioFilePath!!)
                    playAndPausePlayer()


                }
            }else{
                resumeRecordingBottom()
                tv_timer.visibility = View.VISIBLE
                waveform.visibility = View.VISIBLE
                play_btn.visibility = View.INVISIBLE
                seek_bar.visibility =View.INVISIBLE
                record_btn.setImageResource(R.drawable.baseline_pause_24)
                record_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
            }

        }
        binding.deleteBtn.setOnClickListener {
            cl_bottom.visibility = View.INVISIBLE
            timer.stop()
            tv_timer.visibility = View.VISIBLE
            waveform.visibility = View.VISIBLE
            play_btn.visibility = View.INVISIBLE
            seek_bar.visibility =View.INVISIBLE
            record_btn.setImageResource(R.drawable.baseline_pause_24)
            record_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
            binding.clMsg.visibility = View.VISIBLE
            binding.audioRecorder.visibility = View.VISIBLE
            isRecording = false
        }
        binding.sendBtn.setOnClickListener {
            stopRecording()
            cl_bottom.visibility = View.INVISIBLE
            timer.stop()
            timer.format()
            tv_timer.visibility = View.VISIBLE
            waveform.visibility = View.VISIBLE
            play_btn.visibility = View.INVISIBLE
            seek_bar.visibility =View.INVISIBLE
            record_btn.setImageResource(R.drawable.baseline_pause_24)
            record_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
            binding.clMsg.visibility = View.VISIBLE
            binding.audioRecorder.visibility = View.VISIBLE
            isRecording = false
        }
    }

    override fun onTimerTick(duration: String) {
        val tvTimer = bottomDialogLayout.findViewById<TextView>(R.id.tv_timer)
        val wave_form = bottomDialogLayout.findViewById<WaveformView>(R.id.waveform)
        binding.tvTimer.text = duration
        waveform.addAmplitudes(mediaRecorder!!.maxAmplitude.toFloat())
    }

    private fun setUpRecyclerView(list:List<ChatDay>){
        chatsDayAdapter  = DateMessageAdapter(list, FirebaseAuth.getInstance().currentUser!!,this,this,this@MessageChatActivity)
        binding.recyclerView.adapter = chatsDayAdapter
        val lll = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        lll.stackFromEnd = true
        binding.recyclerView.layoutManager = lll
       binding.recyclerView.setHasFixedSize(true)


    }

    private fun observeViewModelForChats(){

        /*    lifecycleScope.launchWhenStarted {
                viewmodel.chats.collectLatest {
                    if(it.isLoading){

                    }
                    if(it.error.isNotBlank()){

                    }
                    it.data.let {
                        setUpRecyclerView()

                        chatsAdapter.differ.submitList(it)


                    }
                }
            }*/
    }

    override fun onResume() {
        super.onResume()
        //  viewmodel.getAllChats(currentUser,userIdVisit,FirebaseAuth.getInstance().currentUser!!,userIdVisit)
        //  observeViewModelForChats()
       // changeBackground(emptyList())
      //  selectedList.clear()
      //  chatList.clear()
    }

    fun navigateToImageVideoFragment(chat: Chat){
        val bundle = Bundle().apply {
            putSerializable("chat",chat)
        }
        // parentFragmentManager.setFragmentResult("userId",bundle)
        val imviFragment = ImageVideoFragment()
        imviFragment.arguments = bundle
      //  fragmentManager!!.beginTransaction().replace(R.id.fragment_container,imviFragment).commit()

    }

  /*  private fun sendNotification(receiverId: String, username: String?, message: String) {


        //  val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        //   val query = ref.orderByKey().equalTo(receiverId)
        val ref = FirebaseFirestore.getInstance().collection("Tokens").document(receiverId)
   //     val query = ref.document(receiverId)

        ref.get().addOnCompleteListener {
            if(it.isSuccessful){
                val document = it.result
                if(document.exists()){
                    val token = document.toObject(Token::class.java)
                   // val token = document.toObject(Token::class.java)
                    val data = DataCall(user = currentUser,
                        icon =  R.mipmap.ic_launcher,
                        body =   "$username: $message",
                        title = "New Message",target = userIdVisit,
                        type = "text")
                    val sender = Sender(data!!, token!!.getToken()!!.toString())
                    Log.i("token",token!!.getToken().toString())



                    apiService!!.sendNotification(sender)?.enqueue(object : Callback<MyResponse?> {
                        override fun onResponse(
                            call: Call<MyResponse?>,
                            response: Response<MyResponse?>
                        ) {

                            if(response.code() == 200){
                                if(response.body()!!.success !=1){
                                    Log.i("respo",response.body().toString())
                                    Toast.makeText(this@MessageChatActivity,"Failed,Nothing happened", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<MyResponse?>, t: Throwable) {

                        }


                    })
                }
            }
        }






        }*/





   /*  fun sendVideoCallNotification(receiverId:String,senderName:String,callType:String){

         serviceRepository.startService(currentUser)

        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val query = ref.document(receiverId)

        query.addSnapshotListener{snapshot, exception ->
            if (exception != null) {

                return@addSnapshotListener
            }
            snapshot?.let { document ->

                val token = document.toObject(Token::class.java)
                val data = DataCall(user = currentUser,
                   icon =  R.mipmap.ic_launcher,
                  body =   senderName,
                     title = when (callType) {
                "start_video_call" -> "Incoming video call.."
                "start_audio_call" -> "Incoming voice call.."
                else -> "Incoming call.."
            }
                    ,target = userIdVisit,
                    type = callType)


                val sender = Sender(data, token!!.getToken()!!.toString())
                Log.i("token",token!!.getToken().toString())

                apiService!!.sendNotification(sender)?.enqueue(object : Callback<MyResponse?> {
                    override fun onResponse(
                        call: Call<MyResponse?>,
                        response: Response<MyResponse?>
                    ) {

                        if(response.code() == 200){
                            if(response.isSuccessful){

                            val intent = Intent(this@MessageChatActivity,CallsActivity::class.java)
                            intent.putExtra("data_model",data)
                            intent.action = ACTION_MSG_CHAT_ACTIVITY
                            startActivity(intent)

                            }
                            if(response.body()!!.success !=1){
                                Log.i("respo",response.body().toString())
                                Toast.makeText(this@MessageChatActivity,"Failed,Nothing happened", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {

                    }


                })
            }


        }
    }*/

    fun sendVideoCallNotification(receiverId:String,senderName:String,callType:String){

        serviceRepository.startService(currentUser)

        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val query = ref.document(receiverId)


        query.addSnapshotListener { snapshot, exception ->
            if (exception != null) return@addSnapshotListener

            snapshot?.let { document ->
                // This is the device's FCM registration token
                val token = document.toObject(Token::class.java)

                if (token != null) {
                    val data = DataCall(
                        user = currentUser,
                        icon = R.mipmap.ic_launcher,
                        body = senderName,
                        title = when (callType) {
                            "start_video_call" -> "Incoming video call.."
                            "start_audio_call" -> "Incoming voice call.."
                            else -> "Incoming call.."
                        },
                        target = userIdVisit,
                        type = callType
                    )

                    // Sender object with registration token and data

                    val sender = Sender(data, token.getToken()!!.toString())
                    Log.i("token", token.getToken().toString())

                    val tokenAuth = AccessToken.getAccessToken()
                    val authToken = "Bearer $tokenAuth"
                    // Get OAuth2 token
                 //   accessToken = OAuth2Util.getAccessToken(this).toString()
                    // Make the API call with OAuth token in the header
                    apiService!!.sendNotification(sender,authToken)
                        .enqueue(object : Callback<MyResponse> {
                            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                if (response.isSuccessful && response.code() == 200) {
                                    val intent = Intent(this@MessageChatActivity, CallsActivity::class.java)
                                    intent.putExtra("data_model", data)
                                    intent.action = ACTION_MSG_CHAT_ACTIVITY
                                    startActivity(intent)
                                } else {
                                    Log.i("FCM Error", "Error: ${response.code()}")
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                Log.e("FCM Failure", "Error: ${t.message}")
                            }
                        })
                }
            }
        }

    }

    var seenListener: ListenerRegistration? = null

    private fun seenMessage(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val chatRef = db.collection("ChatsDates")

        seenListener = chatRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle errors
                return@addSnapshotListener
            }

            snapshot?.let { documents ->
                for (document in documents) {

                    val chatDay = document.toObject(ChatDay::class.java)
                    for(chat in chatDay.chats){


                            if(chat.receiver == currentUser && chat.sender == userId ){
                                chat.isSeen = true
                                // Update the chat in Firestore



                            }

                        db.collection("ChatsDates").document(document.id)
                            .update("chats", chatDay.chats)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                            }


                    }



                }


            }
        }
    }


    override fun onPause() {
        super.onPause()
     //   updateStatus("offline")
        //!!.removeEventListener(seenListener!!)

    }





    companion object{
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }

    override fun onClick(chat: Chat, holder: View, position: Int) {
        if (chatList.contains(chat) ) {
            chatList.remove(chat)
            holder.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        } else {
            chatList.add(chat)
            holder.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.transparentBlue))
        }

    }

    override fun onSelectionChanged(selectedItemCount: Int) {


        navigateToForwardfragment()
    }


}