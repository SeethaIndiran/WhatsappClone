package com.example.chattingapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapters.CallsAdapter
import com.example.chattingapp.databinding.FragmentCallsBinding
import com.example.chattingapp.firebasevideocall.service.MainServiceRepository
import com.example.chattingapp.notifications.ApiService
import com.example.chattingapp.notifications.Client
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class CallsFragment : Fragment() {
    private lateinit var leftView: ImageView
    private lateinit var rightView: ImageView
    private lateinit var binding:FragmentCallsBinding
    private lateinit var callAdapter: CallsAdapter
    var currentUser =""
    var apiService: ApiService? = null
    private val viewModel: UserViewmodel by viewModels()

    @Inject
    lateinit var serviceRepository: MainServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallsBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser=FirebaseAuth.getInstance().currentUser!!.uid

        apiService = Client.Client.getCloient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)

        viewModel.getAllCalls(currentUser)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.calls.collectLatest {
                withContext(Dispatchers.Main){
                    setUpRecyclerView()
                    callAdapter.differ.submitList(it.data)
                    callAdapter.notifyDataSetChanged()
                }
            }
        }

    }



    private fun setUpRecyclerView(){
        callAdapter  = CallsAdapter(this@CallsFragment)
        binding.rvCalls.adapter = callAdapter
        binding.rvCalls.layoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.VERTICAL,false)
        binding.rvCalls.setHasFixedSize(true)
    }}

   /* fun sendVideoCallNotification(receiverId:String,senderName:String,callType:String){

        serviceRepository.startService(senderName)

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
                    title = "Incoming video call..",target = receiverId,
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

                                val intent = Intent(requireContext(), CallsActivity::class.java)
                                intent.putExtra("data_model",data)
                                intent.action = Constants.ACTION_MSG_CHAT_ACTIVITY
                                startActivity(intent)

                            }
                            if(response.body()!!.success !=1){
                                Log.i("respo",response.body().toString())
                                Toast.makeText(requireContext(),"Failed,Nothing happened", Toast.LENGTH_SHORT).show()
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