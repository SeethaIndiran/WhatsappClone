package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.adapters.RecentUpdatesAdapter
import com.example.chattingapp.databinding.FragmentStoriesViewBinding
import com.example.chattingapp.models.Status
import com.example.chattingapp.others.FirebaseLoadDone
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.bottom_sheet_status.view.delete_btn
import kotlinx.android.synthetic.main.bottom_sheet_status.view.no_views
import kotlinx.android.synthetic.main.bottom_sheet_status.view.rv_my_status
import kotlinx.android.synthetic.main.bottom_sheet_status.view.tv_viewed_by
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class StoriesViewFragment : Fragment() {

    private lateinit var binding:FragmentStoriesViewBinding
   internal var count = 0
    private var imagesList :ArrayList<String> = ArrayList()
     private val viewmodel:UserViewmodel by viewModels ()
    private lateinit var statusAdapter: RecentUpdatesAdapter
    private var currentUserId:String = ""
    private lateinit var dialogView:BottomSheetDialog
    private lateinit var bottomDialogLayout:View

   // val args:StoriesV by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoriesViewBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity?)?.hideToolbar()

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        if (arguments != null ) {
            if(arguments!!.containsKey("status")){
                binding.etSendMsg.visibility = View.VISIBLE
                binding.viewBtn.visibility =View.GONE
                val status = arguments!!.getSerializable("status") as Status
                for(image in status.imageUrls){
                    imagesList.add(image)
                }
                storyView()
                binding.etSendMsg.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                       binding.storiesView.pause()
                    } else if (binding.etSendMsg.text.isEmpty()) {
                       binding.storiesView.resume()
                    }
                }

                binding.etSendMsg.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        binding.storiesView.pause()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s != null && s.isNotEmpty()) {
                           binding.storiesView.pause()
                        } else {
                            binding.storiesView.resume()
                        }
                    }
                })
                binding.btnSnd.setOnClickListener {

                    if(binding.etSendMsg.text.isEmpty()){
                        Toast.makeText(requireContext(),"Please type a message",Toast.LENGTH_SHORT).show()
                    }else{
                        viewmodel.sendMessage(currentUserId,status.userId,binding.etSendMsg.text.toString(),dateConversion(System.currentTimeMillis()),"")
                        binding.etSendMsg.setText("")
                        binding.storiesView.resume()
                    }
                }
            }else if(arguments!!.containsKey("my_status") ){
                binding.etSendMsg.visibility = View.INVISIBLE
                binding.viewBtn.visibility =View.VISIBLE
                val status = arguments!!.getSerializable("my_status") as Status
                for(image in status.imageUrls){
                    imagesList.add(image)
                }

                storyView()
                binding.viewBtn.setOnClickListener {
                    binding.storiesView.pause()
                 //   isPaused = true
                    showBottomSheetDialog()
                }

            }


        }
        // val status = args.status



    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun showBottomSheetDialog(){
         bottomDialogLayout = layoutInflater.inflate(R.layout.bottom_sheet_status,null)
         dialogView = BottomSheetDialog(requireContext(),R.style.BottomSheetDialogTheme)
        dialogView.setContentView(bottomDialogLayout)
        dialogView.show()
        dialogView.setCancelable(true)
        dialogView.setOnDismissListener {
                // isPaused = false
            binding.storiesView.resume()



        }
        bottomDialogLayout.tv_viewed_by.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
        viewmodel.retrieveCurrentUserStatus(currentUserId)
        CoroutineScope(Dispatchers.IO).launch {

            viewmodel.singleStatus.collectLatest {
                withContext(Dispatchers.Main){
                    if(it.status!= null){

                      val  views = it.status.viewedUsers

                        if(views.isNotEmpty()){
                            bottomDialogLayout.apply {
                                tv_viewed_by.text = " viewed by ${views.size} "

                                rv_my_status.visibility = View.VISIBLE
                                no_views.visibility = View.INVISIBLE
                             //    tv_viewed.text = views.size.toString()
                                setUpRecyclerView()
                               // statusAdapter.differ.submitList(views)
                            }
                        }else{
                            bottomDialogLayout.apply {
                                tv_viewed_by.text = "viewed by 0"
                                rv_my_status.visibility = View.INVISIBLE
                                tv_viewed_by.visibility = View.VISIBLE
                          //     tv_viewed.text = "0"
                            }
                        }

                    }
                }
            }

        }

        bottomDialogLayout.delete_btn.setOnClickListener {
          //  viewmodel.deleteStatus()
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun dateConversion(time: Long): String {
        val date =  Date(time)
        val sdf = SimpleDateFormat("MMM dd,yyyy")
        val fs = sdf.format(date)
        return fs
    }


    private fun storyView(){

        binding.storiesView.apply {

            val crossFadeFactory =
                DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
            setStoriesCount(imagesList.size)
            setStoryDuration(2500L)
            if (imagesList.isNotEmpty()) {
                Glide.with(activity!!).load(imagesList[0])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.statusImages)

                startStories()

                setStoriesListener(object : StoriesProgressView.StoriesListener {
                    override fun onNext() {
                        if (count < imagesList.size) {
                            count++

                            Glide.with(activity!!).load(imagesList[count])
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transition(DrawableTransitionOptions.withCrossFade(crossFadeFactory))
                                .into(binding.statusImages)
                            binding.statusImages.setBackgroundColor(Color.TRANSPARENT)
                            //   fadeOut(stories_view)
                            //   Picasso.get().load(imagesList[count]).into(binding.statusImages)
                        }
                    }

                    override fun onPrev() {
                        if (count > 0) {

                            count--
                            Glide.with(activity!!).load(imagesList[count])
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.statusImages)
                            // Picasso.get().load(imagesList[count]).into(binding.statusImages)
                        }
                    }

                    override fun onComplete() {
                        count = 0
                        Log.d("StoriesProgressView", "Story completed. Popping back stack.")
                        if(arguments!=null){
                            if(arguments!!.containsKey("status")){

                             /*   val fragment = StatusFragment()
                                val transaction = activity!!.supportFragmentManager.beginTransaction()
                                transaction.replace(R.id.root_view,fragment)
                                transaction.addToBackStack(null)
                                transaction.commit()*/
                                activity!!.supportFragmentManager.popBackStack("statusFragment",FragmentManager.POP_BACK_STACK_INCLUSIVE)
                              //  (activity as AppCompatActivity).supportActionBar?.show()
                            }else if(arguments!!.containsKey("my_status")){
                                activity!!.supportFragmentManager.popBackStack("myStatusFragment",FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                (activity as AppCompatActivity).supportActionBar?.show()
                            }
                        }
                    }

                })
            }

        }
    }
    private fun setUpRecyclerView(){
        statusAdapter = RecentUpdatesAdapter(this@StoriesViewFragment)
        bottomDialogLayout.rv_my_status.apply {
            adapter = statusAdapter
            layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
            setHasFixedSize(true)
        }
    }
}
