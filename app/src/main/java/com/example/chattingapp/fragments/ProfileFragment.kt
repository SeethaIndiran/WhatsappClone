package com.example.chattingapp.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentProfileBinding
import com.example.chattingapp.utilities.Constants
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Base64
import java.util.UUID
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ProfileFragment : Fragment() {
   private lateinit var binding:FragmentProfileBinding
   private val viewModel:UserViewmodel by viewModels()
   private lateinit var auth:FirebaseAuth
    private  var verificationId:String =""
    private lateinit var dialog: AlertDialog
    private lateinit var selectedImageUri: Uri
    private var encodedeImage = ""
    private var uriString = ""
    var number=""
    var otp = ""
    var name = ""
    var imageUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding  = FragmentProfileBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Please wait")
        builder.setTitle("Loading")
        builder.setCancelable(true)

        dialog = builder.create()
        if(arguments!=null){
            if(arguments!!.containsKey("number")){
                number = arguments!!.getString("number").toString()
                otp = arguments!!.getString("otp").toString()
            }
        }
        val phoneNumber = "+1$number"

        val optins = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity!!)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(),"Please try again", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    dialog.dismiss()
                    verificationId = p0
                }

            }
            ).build()
        PhoneAuthProvider.verifyPhoneNumber(optins)

        binding.roundedImageView.setOnClickListener {
                 customImageSelectionDialog()
        }

        binding.btnProfile.setOnClickListener {
            if(binding.etNum.text.toString().isEmpty()){
                Toast.makeText(requireContext(),"Please enter otp",Toast.LENGTH_SHORT).show()
            }else{

                viewModel.signUpWithPhonenumber(otp,binding.etNum.text.toString(),uriString,verificationId)
                observeViewModel()
            }

        }

    }

    private fun observeViewModel(){

        lifecycle.coroutineScope.launchWhenCreated {
            viewModel.userData.collectLatest {
                if(it.isLoading){
                   dialog.show()
                }
                if(it.error.isNotBlank()){

                }
                it.data?.let {
                    dialog.dismiss()
                    val mainFragment = MainFragment()



                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.root_view, mainFragment)
                    transaction.addToBackStack("profileFragment")
                    transaction.commit()
                }
            }
        }

    }

    private fun observeViewModelForProfileImage(){

        lifecycle.coroutineScope.launchWhenCreated {
            viewModel.url.collectLatest {
                if(it.isLoading){
                    dialog.show()
                }
                if(it.error.isNotBlank()){

                }
               it.imageUrl?.let { imageString ->
                   uriString = imageString
               }
            }
        }

    }

    private fun customImageSelectionDialog(){
        Dexter.withContext(requireContext()).withPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    val galleryIntent= Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, Constants.GALLERY)
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
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {

            if (requestCode == Constants.GALLERY) {
                data?.let {
                     selectedImageUri = data.data!!
                     viewModel.selectProfilePhoto(selectedImageUri)
                      observeViewModelForProfileImage()

                    Glide.with(this)
                        .load(selectedImageUri)
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
            }
        }
    }
    private fun saveImageToInternalStorage(bitmap:Bitmap):String{
        val wrapper = ContextWrapper(requireContext())

        var file = wrapper.getDir(Constants.GALLERY_IMAGE, Context.MODE_PRIVATE)
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

    fun copyFileToInternalStorage(uri: Uri, contentResolver: ContentResolver): Uri? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            // Create a file in internal storage
            val file = File(requireContext().filesDir, fileName)
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



}