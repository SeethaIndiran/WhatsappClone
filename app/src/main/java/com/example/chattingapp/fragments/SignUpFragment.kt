package com.example.chattingapp.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.chattingapp.MainActivity
import com.example.chattingapp.databinding.FragmentSignUpBinding
import com.example.chattingapp.utilities.Constants.GALLERY
import com.example.chattingapp.utilities.Constants.GALLERY_IMAGE
import com.example.chattingapp.viewmodels.UserViewmodel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.*
import java.util.*

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private lateinit var binding:FragmentSignUpBinding
    private val viewModel:UserViewmodel by viewModels()
    private var encodedeImage:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSignIn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.signUpBtn.setOnClickListener {
            if(isValidDetails()){
                   val name = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
               // FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)

                registerUser(name,email, password)
            }
        }
        binding.roundedImageView.setOnClickListener {
            customImageSelectionDialog()
        }
    }

    private fun showText(message:String){
        Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
    }

    private fun isValidDetails():Boolean {
        if(binding.etName.text.trim().toString().isEmpty()){
            showText("Enter your name")
            return false
        }else if(binding.etEmail.text.trim().toString().isEmpty()){
            showText("Enter your email")
            return false
        }else if(binding.etPassword.text.trim().toString().isEmpty()){
            showText("Enter your password")
            return false
        }else if(binding.etConfirmPassword.text.trim().toString().isEmpty()){
            showText("Enter your confirm Password")
            return false
        }else if(binding.etEmail.text.trim().toString().isEmpty()){
            showText("Enter your email")
        }else if(!binding.etPassword.text.trim().toString().equals(binding.etConfirmPassword.text.trim().toString())){
            showText("Enter your email")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.trim().toString()).matches()){
            showText("Enter your email")
            return false
        }else if(encodedeImage == null){
            showText("Select an image")
            return false}

            return true

    }

    private  fun registerUser(username: String, email: String, password: String) {

        viewModel.signUp(username, email, password)
        observeViewModel()

    }

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
                   val intent  =Intent(requireContext(), MainActivity::class.java)
                   startActivity(intent)
               }
           }
       }
   }













  /*  private fun signUp(){

        loading(true)
        val database:FirebaseFirestore = FirebaseFirestore.getInstance()
        val user:HashMap<String,Any> = HashMap<String,Any>()
        user[Constants.KEY_NAME] = binding.etName.text.toString()
        user[Constants.KEY_EMAIL] = binding.etEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.etPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedeImage
        database.collection(Constants.KEY_COLLECTION_NAME)
            .add(user).addOnSuccessListener {
                loading(false)
                findNavController().navigate(R.id.action_signUpFragment_to_mainFragment)
            }.addOnFailureListener{
                loading(false)
                showText(it.message.toString())
            }

    }*/

    private fun loading(isLoading:Boolean){
   if(isLoading){
       binding.signUpBtn.visibility = View.INVISIBLE
       binding.progressBar.visibility = View.VISIBLE
   }else{
       binding.signUpBtn.visibility = View.VISIBLE
       binding.progressBar.visibility = View.INVISIBLE
   }
    }

    @Deprecated("Deprecated in Java")
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {

            if (requestCode == GALLERY) {
                data?.let {
                    val selectedPhotoUri = data.data
                    Glide.with(this)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error loading image", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    encodedeImage = saveImageToInternalStorage(bitmap)
                                }
                                return false
                            }

                        })
                        .into(binding.roundedImageView)

                }
            }}}

    private fun customImageSelectionDialog(){
        Dexter.withContext(requireContext()).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                        val galleryIntent= Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                      //  showRationalDialogForPermissions()
                    }

                }).onSameThread().check()




    }


    private fun saveImageToInternalStorage(bitmap:Bitmap):String{
        val wrapper = ContextWrapper(requireContext())

        var file = wrapper.getDir(GALLERY_IMAGE,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,stream)
            stream.flush()
            stream.close()
        }catch(e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath
    }


}