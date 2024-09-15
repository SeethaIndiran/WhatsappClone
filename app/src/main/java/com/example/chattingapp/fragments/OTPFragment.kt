package com.example.chattingapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentOTPBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPFragment : Fragment() {

      private lateinit var binding:FragmentOTPBinding
      private lateinit var auth:FirebaseAuth
      private lateinit var verificationId:String
      private lateinit var dialog:AlertDialog
      var number=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(layoutInflater,container,false)
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
            }
        }


        val phoneNumber = "+1$number"

   /*     val optins = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(activity!!)
            .setCallbacks(object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(),"Please try again",Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    dialog.dismiss()
                    verificationId = p0
                }

            }
            ).build()
        PhoneAuthProvider.verifyPhoneNumber(optins)
 binding.btnContinue.setOnClickListener {
     if(binding.otpNumber.text.toString().isEmpty()){
         Toast.makeText(requireContext(),"Please enter otp",Toast.LENGTH_SHORT).show()
     }else{
         dialog.show()
         val credential = PhoneAuthProvider.getCredential(verificationId,binding.etOtp.text.toString())
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                dialog.dismiss()

                val mainFragment = MainFragment()

                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.root_view, mainFragment)
                transaction.addToBackStack("otpFragment")
                transaction.commit()
            }else{
                dialog.dismiss()
                Toast.makeText(requireContext(),"Error ${it.exception}",Toast.LENGTH_SHORT).show()
            }
        }


     }
 }*/
        binding.btnContinue.setOnClickListener {


            if (binding.etOtp.text!!.isEmpty()) {
                Toast.makeText(activity, "Please enter your  otp number", Toast.LENGTH_SHORT).show()
            } else {

                val bundle = Bundle().apply {
                    putString("number", number)
                    putString("otp",binding.etOtp.text.toString())

                }


                // parentFragmentManager.setFragmentResult("userId",bundle)
                val profileFragment = ProfileFragment()


                profileFragment.arguments = bundle
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.root_view, profileFragment)
                transaction.addToBackStack("otpFragment")
                transaction.commit()

                //   findNavController().navigate(R.id.action_verifyNumberFragment_to_OTPFragment,bundle)}
            }
        }

    }


}