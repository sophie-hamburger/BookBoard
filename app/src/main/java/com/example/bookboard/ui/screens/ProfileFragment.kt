package com.example.bookboard.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookboard.R
import com.example.bookboard.adapter.BookAdapter
import com.example.bookboard.databinding.FragmentProfileBinding
import com.example.bookboard.viewmodel.AuthViewModel
import com.example.bookboard.viewmodel.BookPostViewModel

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    private val bookPostViewModel: BookPostViewModel by activityViewModels()
    private lateinit var userPostsAdapter: BookAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        userPostsAdapter = BookAdapter { post ->
            // Navigate to edit post fragment
            val action = ProfileFragmentDirections.actionProfileFragmentToEditPostFragment(post.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewUserPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userPostsAdapter
        }
    }
    
    private fun setupObservers() {
        authViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etProfileName.setText(it.name)
                binding.tvEmail.text = it.email
            }
        }
        
        bookPostViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            if (posts.isEmpty()) {
                binding.tvNoUserPosts.visibility = View.VISIBLE
                binding.recyclerViewUserPosts.visibility = View.GONE
            } else {
                binding.tvNoUserPosts.visibility = View.GONE
                binding.recyclerViewUserPosts.visibility = View.VISIBLE
                userPostsAdapter.submitList(posts)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            if (name.isNotEmpty()) {
                authViewModel.updateUserProfile(name)
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            } else {
                binding.etProfileName.error = "Name is required"
            }
        }
        
        binding.btnLogout.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 