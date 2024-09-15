package com.example.chattingapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.MessageChatActivity
import com.example.chattingapp.R
import com.example.chattingapp.adapters.UsersAdapter
import com.example.chattingapp.databinding.FragmentSearchBinding
import com.example.chattingapp.viewmodels.UserViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding:FragmentSearchBinding
    private lateinit var usersAdapter: UsersAdapter
    private val viewModel:UserViewmodel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentSearchBinding.inflate(inflater,container,false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Select"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

              val frUser = FirebaseAuth.getInstance().currentUser!!.uid
        if( binding.etToolbar.text.toString() == ""){
            viewModel.getAllUsers(frUser)
            observeViewModel()

        }


binding.etToolbar.addTextChangedListener(object:TextWatcher{
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.getAllSearchUsers(s.toString().trim(),frUser)
        observeViewModelForSearchUsers()

    }

    override fun afterTextChanged(s: Editable?) {

    }

})

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{

        }catch(e: ClassCastException){
            throw ClassCastException("parent")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_fragment,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.search ->{
                showEditText()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun observeViewModel(){

      lifecycle.coroutineScope.launchWhenCreated {
            viewModel.users.collectLatest {
                if(it.isLoading){

                }
                if(it.error.isNotBlank()){

                }
                it.data.let {
                    setUpRecyclerView()
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    FirebaseMessaging.getInstance().subscribeToTopic("/topic/$userId")
                    usersAdapter.differ.submitList(it)
                }
            }
        }
    }

    private fun observeViewModelForSearchUsers(){

        lifecycle.coroutineScope.launchWhenCreated {
            viewModel.searchUsers.collectLatest {
                if(it.isLoading){

                }
                if(it.error.isNotBlank()){

                }
                it.data.let {
                    setUpRecyclerView()
                    usersAdapter.differ.submitList(it)


                }
            }
        }
    }
    private fun setUpRecyclerView(){
        usersAdapter  = UsersAdapter(this@SearchFragment)
        binding.rvSearch.adapter = usersAdapter
        binding.rvSearch.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        binding.rvSearch.setHasFixedSize(true)
    }

private fun showEditText(){
    binding.tvToolbarTitle.visibility = View.GONE
    binding.tvToolbarContacts.visibility = View.GONE
    binding.etToolbar.visibility = View.VISIBLE
    binding.etToolbar.requestFocus()
    binding.toolbar.setBackgroundColor(resources.getColor(R.color.white))
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    setHasOptionsMenu(false)
}
    private fun showToolbar(){
        binding.tvToolbarTitle.visibility = View.VISIBLE
        binding.tvContacts.visibility = View.VISIBLE
        binding.etToolbar.visibility = View.INVISIBLE
        binding.toolbar.setBackgroundColor(resources.getColor(R.color.purple_500))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }
    fun navigateToMessageChatFragment(userIdVisit:String){
             //   onclickListener!!.onClick(userIdVisit)
        val bundle = Bundle().apply {
            putString("id",userIdVisit)
        }
     //   parentFragmentManager.setFragmentResult("userId",bundle)
        val msgFragment = MessageChatFragment()
       val intent  =Intent(requireContext(),MessageChatActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
      //  findNavController().navigateUp()
      //  msgFragment.arguments = bundle
      //  fragmentManager!!.beginTransaction().replace(R.id.fragment_container,msgFragment).commit()

    }


}



