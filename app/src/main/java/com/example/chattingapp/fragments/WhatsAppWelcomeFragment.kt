package com.example.chattingapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentWhatsAppWelcomeBinding
import com.google.firebase.auth.FirebaseAuth


class WhatsAppWelcomeFragment : Fragment() {
    private lateinit var binding:FragmentWhatsAppWelcomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWhatsAppWelcomeBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnCtn.setOnClickListener {
            if(findNavController().currentDestination?.id == R.id.whatsAppWelcomeFragment){
                findNavController().navigate(R.id.action_whatsAppWelcomeFragment_to_verifyNumberFragment)}
        }
    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            if(findNavController().currentDestination?.id == R.id.whatsAppWelcomeFragment){
                findNavController().navigate(R.id.action_whatsAppWelcomeFragment_to_mainFragment)
            }
        }
    }
}