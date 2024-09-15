package com.example.chattingapp.fragments


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentSignInBinding
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class  SignInFragment : Fragment() {

    private lateinit var binding:FragmentSignInBinding
    private val viewModel:UserViewmodel by viewModels()
    var currentUser :FirebaseUser? = null

    @Inject
    lateinit var sharedPref: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = FragmentSignInBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // currentUser = FirebaseAuth.getInstance().currentUser

     /*   if(sharedPref.getBoolean(Constants.KEY_SIGNED_IN,false)){
           findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
        }*/






        binding.signInBtn.setOnClickListener {
            if(isValidDetails()){
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
              signInUser(email, password)
            }
        }
   binding.createNewAccount.setOnClickListener {
       val fragment = SignUpFragment()
      val transaction =  activity!!.supportFragmentManager.beginTransaction()
       transaction.replace(R.id.root_view,fragment)
       transaction.addToBackStack(null)
       transaction.commit()
   }

    }

    private fun signInUser( email: String, password: String) {

        viewModel.signIn( email, password)
        observeViewModel()

    }

  /*  private fun signIn(){
        loading(true)
        val database:FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_NAME)
            .whereEqualTo(Constants.KEY_EMAIL,binding.etEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.etPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if(task.isSuccessful && task.result!= null && task.result.documents.size>0){
                    val documentSnapshot:DocumentSnapshot = task.result.documents[0]
                    sharedPref.edit().putBoolean(Constants.KEY_SIGNED_IN,true).apply()
                    sharedPref.edit().putString(Constants.KEY_USER_ID,documentSnapshot.id).apply()
                    sharedPref.edit().putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME)).apply()
                    sharedPref.edit().putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE)).apply()
                    findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
                }else{
                    loading(false)
                    showText("Unable to Signin")
                }
            }
    }*/

    private fun observeViewModel(){

       lifecycle.coroutineScope.launchWhenCreated {
            viewModel.userData.collectLatest {
                if(it.isLoading){
                   loading(true)
                }
                if(it.error.isNotBlank()){

                }
                it.data?.let {
                    loading(false)
                  if(findNavController().currentDestination!!.id == R.id.signInFragment){
                      findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
                  }
                }
            }
        }

    }

    private fun loading(isLoading:Boolean){
        if(isLoading){
            binding.signInBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.signInBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
private fun showText(message:String){
    Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
}
    private fun isValidDetails():Boolean{
        if(binding.etEmail.text.trim().toString().isEmpty()){
            showText("Enter your email")
            return false
        }else if(binding.etPassword.text.trim().toString().isEmpty()){
            showText("Enter your password")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.trim().toString()).matches()){
            showText("Enter your valid email")
            return false
        }else{
            return true
        }
    }

    override fun onStart() {
        super.onStart()
        currentUser  = FirebaseAuth.getInstance().currentUser
//        val currentUserId = currentUser!!.uid

        if(currentUser!=null){
             if(findNavController().currentDestination?.id == R.id.signInFragment){
                  findNavController().navigate(R.id.action_signInFragment_to_mainFragment)}
         //   val intent = Intent(activity, MainActivity::class.java)
           // startActivity(intent)


        }
    }

}