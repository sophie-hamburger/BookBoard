package com.example.bookboard.ui.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookboard.R
import com.example.bookboard.adapter.BookAdapter
import com.example.bookboard.databinding.FragmentHomeBinding
import com.example.bookboard.viewmodel.AuthViewModel
import com.example.bookboard.viewmodel.BookPostViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val bookPostViewModel: BookPostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSearchListener()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter { post ->
            // Simple click - just show a message for now
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookAdapter
        }
    }

    private fun setupObservers() {
        bookPostViewModel.posts.observe(viewLifecycleOwner) { posts ->
            if (posts.isEmpty()) {
                binding.tvNoPosts.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvNoPosts.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                bookAdapter.submitList(posts)
            }
        }

        bookPostViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        bookPostViewModel.errorMessage.observe(viewLifecycleOwner) { error: String ->
            if (error.isNotEmpty()) {
                // Show error message
                bookPostViewModel.clearError()
            }
        }

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBookFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                bookPostViewModel.searchPosts(query)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}