package com.example.chattingapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentVerifyNumberBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyNumberFragment : Fragment() {
  private lateinit var binding:FragmentVerifyNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       binding = FragmentVerifyNumberBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnContinue.setOnClickListener {


            if (binding.etNum.text!!.isEmpty()) {
                Toast.makeText(activity, "Please enter your number", Toast.LENGTH_SHORT).show()
            } else {

                val bundle = Bundle().apply {
                    putString("number", binding.etNum.text.toString())
                    Log.i("number",binding.tvNumber.text.toString())
                }


                // parentFragmentManager.setFragmentResult("userId",bundle)
                val otpFragment = OTPFragment()


                otpFragment.arguments = bundle
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.root_view, otpFragment)
                transaction.addToBackStack("verifyNumberFragment")
                transaction.commit()

                //   findNavController().navigate(R.id.action_verifyNumberFragment_to_OTPFragment,bundle)}
            }
        }
    }



}