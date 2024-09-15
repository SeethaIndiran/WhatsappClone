package com.example.chattingapp.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import androidx.navigation.fragment.navArgs
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.example.chattingapp.R
import com.example.chattingapp.adapters.ChatsAdapter
import com.example.chattingapp.adapters.ChatsDayAdapte
import com.example.chattingapp.databinding.FragmentMessageChatBinding
import com.example.chattingapp.models.Chat
import com.example.chattingapp.models.Timer
import com.example.chattingapp.models.Users
import com.example.chattingapp.models.WaveformView
import com.example.chattingapp.notifications.ApiService
import com.example.chattingapp.notifications.Client
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import java.io.File
import java.io.IOException
import java.util.Date
import kotlin.math.abs


@AndroidEntryPoint
class MessageChatFragment : Fragment(), Timer.OnTimerTickListener{
    private lateinit var binding:FragmentMessageChatBinding
    private val viewmodel:UserViewmodel by viewModels()
    private var userIdVisit:String = ""
    private var currentUser:String = ""
    private  var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer:MediaPlayer? = null
    private  var audioFilePath: String = ""
    private var dirPath:String = ""
    private var filePath:String = ""
    private var isRecording = false
    private var isPaused = false
    private var files:File? = null
    val cacheDir:File? = null


    private var initialX = 0f
    private var initialY =0f
    private var offsetX:Float? = null
    private var offsetY:Float? = null
    private var isDragHor = false
    private var isDragVer = false
    private var isDeleteOrSent = false


    private var animator:ValueAnimator? = null
    private lateinit var timer:Timer
    private lateinit var dialogBottom:BottomSheetDialog
    private lateinit var bottomDialogLayout:View
    private lateinit var seekBar: SeekBar
  //  private lateinit var waveform:WaveformView

    private lateinit var handler:Handler
    private lateinit var runnable: Runnable
    private var delay = 1000L

    private lateinit var chatsAdapter: ChatsDayAdapte

    var fragment: Fragment = this

    private var contextWrapper:ContextWrapper? = null
    var apiService:ApiService? = null
    var currentUsername:String =""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentMessageChatBinding.inflate(inflater,container,false)
        setHasOptionsMenu(true)


        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = Timer(this)
        mediaPlayer = MediaPlayer()
      //  waveform  = WaveformView(requireContext())
        dialogBottom = BottomSheetDialog(requireContext())
         bottomDialogLayout = layoutInflater.inflate(R.layout.bottom_sheet_record,null)
           //  seekBar = bottomDialogLayout.findViewById(R.id.seek_bar)
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        getUserId()

        apiService = Client.Client.getCloient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)



        //   val params = binding.tvSlide.layoutParams as ConstraintLayout.LayoutParams
     //   initialMarginEnd = params.marginEnd

          binding.audioRecorder.setOnClickListener {
            val msg = binding.etSendMsg.text.toString()
            if(msg == ""){
                Toast.makeText(activity,"Please type a message..",Toast.LENGTH_SHORT).show()
            }else {
              //  viewmodel.sendMessage(currentUser, userIdVisit, msg,)

            }
            binding.etSendMsg.setText("")

              FirebaseFirestore.getInstance().collection("Users")
                  .document(currentUser).get().addOnSuccessListener {
                      if(it.exists()){
                          val user = it.toObject(Users::class.java)
                          val username = user!!.username
                        //  sendNotification(userIdVisit,username,msg)
                      }
                  }




        }

     /*  val contextWrapper = ContextWrapper(requireContext())
        val music: File? = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file:File = File(music,"testFile"+".mp3")
        audioFilePath = file.path*/

         val time = Date().time
      //  files!!.mkdirs()
       files  =File(Environment.getExternalStorageDirectory().absolutePath + "/" + "recordingAudio_$time.mp3")
      //  files = File(cacheDir,"my_audio_${System.currentTimeMillis()}.m4a")
        audioFilePath = files!!.absolutePath






        /*    binding.gallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }
        */
           */







        //  observeViewmodel()
        binding.gallery.setOnClickListener {

            Dexter.withContext(requireContext()).withPermission(
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
            Dexter.withContext(requireContext()).withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(object:
                MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let{
                        if(report.areAllPermissionsGranted()){
                        //    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                         //   startActivityForResult(intent,CAMERA)
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


       // seek_bar.max  =mediaPlayer!!.duration
      // setUpRecyclerView()
        viewmodel.getAllChats(
            currentUser,userIdVisit)



        seek_bar.max = mediaPlayer!!.duration

        handler = Handler(Looper.getMainLooper())
        runnable  = Runnable {
            seek_bar.progress = mediaPlayer!!.currentPosition
            handler.postDelayed(runnable,delay)
        }







      CoroutineScope(Dispatchers.IO).launch{
                viewmodel.chats.collectLatest {
                //    chatsAdapter.differ.submitList(it.data)

                }
      }







}


    private fun stopRecording() {

        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            isRecording = false
            timer.stop()
        //   viewmodel.uploadAudioFileToFirebaseStorage(currentUser,userIdVisit,audioFilePath)
            viewmodel.getAllChats(currentUser,userIdVisit)
          //  observeViewModelForChats()
        }
    }

    private fun startRecording() {

        if (CheckPermission()) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
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

    private fun observeViewmodel(){
        lifecycle.coroutineScope.launchWhenCreated {
            viewmodel.singleUser.collectLatest {

                if(it.error.isNotBlank()){

                }
                userIdVisit = it.userVisit.uid
                binding.tvToolbarTitle.text = it.userVisit.username

            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getUserId(){
      //  parentFragmentManager.setFragmentResultListener("userId",this, FragmentResultListener { requestKey, result ->
         //   val id = result.getString("id")
        val args = this.arguments
        val visitUserUId  = args!!.get("id")
            viewmodel.retrieveUser(visitUserUId.toString())
        observeViewmodel()

        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode == Activity.RESULT_OK ){
            if(requestCode == GALLERY ){
                val fileUri = data!!.data
            fileUri?.let {
                if(it.toString().contains("image")){
                   // viewmodel.uploadImageToFirebaseStorage(currentUser,userIdVisit,it)
                    viewmodel.getAllChats(currentUser,userIdVisit)
                    observeViewModelForChats()

                }else if(it.toString().contains("video")){
               //    viewmodel.uploadVideoFileToFirebaseStorage(currentUser,userIdVisit,it)

                }


            }

        }

        }


    }
    private fun CheckPermission(): Boolean {

        if (ActivityCompat.checkSelfPermission(requireActivity(),
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }

        return false

    }

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
        binding.clBottom.visibility = View.VISIBLE
        startRecording()
        binding.recordBtn.setOnClickListener {
            if(!isPaused){
                pauseRecording()
                record_btn.setImageResource(R.drawable.baseline_mic_24)
                tv_timer.visibility = View.INVISIBLE
                waveform.visibility = View.INVISIBLE
                play_btn.visibility = View.VISIBLE
                seek_bar.visibility =View.VISIBLE



                play_btn.setOnClickListener {


                    if(!mediaPlayer!!.isPlaying){

                        mediaPlayer!!.start()
                        play_btn.setImageResource(R.drawable.baseline_pause)

                        handler.postDelayed(runnable,delay)
                    }else{
                        mediaPlayer!!.pause()
                        play_btn.setImageResource(R.drawable.round_play_arrow_24)
                        handler.removeCallbacks(runnable)
                    }
                    mediaPlayer!!.setOnCompletionListener {

                        play_btn.setImageResource(R.drawable.round_play_arrow_24)
                    }
                    seek_bar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if(fromUser)mediaPlayer!!.seekTo(progress)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        }
                    })
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
        }
        binding.sendBtn.setOnClickListener {
            stopRecording()
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
        }
        }




    @RequiresApi(Build.VERSION_CODES.N)
    private fun bottomSheetDialog(){
        dialogBottom.setContentView(bottomDialogLayout)
        dialogBottom.show()
        dialogBottom.setCancelable(false)

        val rec_btn = bottomDialogLayout.findViewById<ImageButton>(R.id.record_btn)
        val deleteBtn = bottomDialogLayout.findViewById<ImageButton>(R.id.delete_btn)
        val send_btn = bottomDialogLayout.findViewById<ImageButton>(R.id.send_btn)
        val play_btn = bottomDialogLayout.findViewById<ImageButton>(R.id.play_btn)
      //  val seekBar = bottomDialogLayout.findViewById<SeekBar>(R.id.seek_bar)
        val tv_timer = bottomDialogLayout.findViewById<TextView>(R.id.tv_timer)
        val waveView = bottomDialogLayout.findViewById<WaveformView>(R.id.waveform)
        var isPausedMP = false
       startRecording()

       rec_btn.setOnClickListener {
           if(!isPaused){
               pauseRecording()
               rec_btn.setImageResource(R.drawable.baseline_mic_24)
               tv_timer.visibility = View.INVISIBLE
               waveView.visibility = View.INVISIBLE
               play_btn.visibility = View.VISIBLE
               seekBar.visibility =View.VISIBLE

               play_btn.setOnClickListener {
                   if(!mediaPlayer!!.isPlaying){
                       mediaPlayer!!.start()
                      play_btn.setImageResource(R.drawable.baseline_pause)

                       handler.postDelayed(runnable,0)
                   }else{
                       mediaPlayer!!.pause()
                       isPausedMP = true
                      play_btn.setImageResource(R.drawable.round_play_arrow_24)
                       handler.removeCallbacks(runnable)
                   }
                   mediaPlayer!!.setOnCompletionListener {

                       play_btn.setImageResource(R.drawable.round_play_arrow_24)
                   }
               }

           }else{
               resumeRecordingBottom()
               tv_timer.visibility = View.VISIBLE
               waveView.visibility = View.VISIBLE
               play_btn.visibility = View.INVISIBLE
               seekBar.visibility =View.INVISIBLE
               rec_btn.setImageResource(R.drawable.baseline_pause_24)
               rec_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
           }

        }
        deleteBtn.setOnClickListener {
             dialogBottom.cancel()
            timer.stop()
            tv_timer.visibility = View.VISIBLE
            waveView.visibility = View.VISIBLE
            play_btn.visibility = View.INVISIBLE
            seekBar.visibility =View.INVISIBLE
            rec_btn.setImageResource(R.drawable.baseline_pause_24)
            rec_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
            binding.clMsg.visibility = View.VISIBLE
            binding.audioRecorder.visibility = View.VISIBLE
        }
        send_btn.setOnClickListener {
            stopRecording()
            dialogBottom.cancel()
            timer.stop()
            tv_timer.visibility = View.VISIBLE
            waveView.visibility = View.VISIBLE
            play_btn.visibility = View.INVISIBLE
            seekBar.visibility =View.INVISIBLE
            rec_btn.setImageResource(R.drawable.baseline_pause_24)
            rec_btn.setBackgroundResource(R.drawable.pause_btn_bgnd)
            binding.clMsg.visibility = View.VISIBLE
            binding.audioRecorder.visibility = View.VISIBLE
        }

    }

    override fun onTimerTick(duration: String) {
           val tvTimer = bottomDialogLayout.findViewById<TextView>(R.id.tv_timer)
        val wave_form = bottomDialogLayout.findViewById<WaveformView>(R.id.waveform)
               binding.tvTimer.text = duration
        waveform.addAmplitudes(mediaRecorder!!.maxAmplitude.toFloat())
    }

/*   private fun setUpRecyclerView(){
        chatsAdapter  = ChatsAdapter(context!!,FirebaseAuth.getInstance().currentUser!!,this)
        binding.recyclerView.adapter = chatsAdapter
        val lll = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL,false)
        lll.stackFromEnd = true
        binding.recyclerView.layoutManager = lll
        binding.recyclerView.setHasFixedSize(true)


    }*/

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
    }

    fun navigateToImageVideoFragment(chat: Chat){
        val bundle = Bundle().apply {
            putSerializable("chat",chat)
        }
        // parentFragmentManager.setFragmentResult("userId",bundle)
        val imviFragment = ImageVideoFragment()
        imviFragment.arguments = bundle
        fragmentManager!!.beginTransaction().replace(R.id.root_view,imviFragment).commit()

    }

    private fun sendNotification(receiverId: String, username: String?, message: String) {


        //  val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        //   val query = ref.orderByKey().equalTo(receiverId)
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val query = ref.document(receiverId)

        query.addSnapshotListener{snapshot, exception ->
            if (exception != null) {

                return@addSnapshotListener
            }
            snapshot?.let { document ->

             /*   val token = document.toObject(Token::class.java)
                val data = DataCalls(currentUser,
                    R.mipmap.ic_launcher,
                    "$username: $message",
                    "New message",userIdVisit,"text")
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
                              //  Toast.makeText(this@MessageChatActivity,"Failed,Nothing happened", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {

                    }


                })*/
            }


        }
    }


    private fun sendVideoCallNotification(receiverId:String,senderName:String){
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val query = ref.document(receiverId)

        query.addSnapshotListener{snapshot, exception ->
            if (exception != null) {

                return@addSnapshotListener
            }
            snapshot?.let { document ->

            /*    val token = document.toObject(Token::class.java)
                val data = Data(currentUser,
                    R.mipmap.ic_launcher,
                    senderName,
                    "Incoming video call..",userIdVisit,"video_call")
                val sender = Sender(data, token!!.getToken()!!.toString())
                Log.i("token",token!!.getToken().toString())

                apiService!!.sendNotification(sender)?.enqueue(object : Callback<MyResponse?> {
                    override fun onResponse(
                        call: Call<MyResponse?>,
                        response: Response<MyResponse?>
                    ) {

                        if(response.code() == 200){
                            if(response.body()!!.success !=1){
                                Log.i("respo",response.body().toString())
                              //  Toast.makeText(this@MessageChatActivity,"Failed,Nothing happened", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<MyResponse?>, t: Throwable) {

                    }


                })*/
            }


        }
    }













    companion object{
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }


}


