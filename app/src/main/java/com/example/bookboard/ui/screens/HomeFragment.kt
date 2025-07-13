package com.example.bookboard.ui.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookboard.R
import com.example.bookboard.adapter.BookAdapter
import com.example.bookboard.controller.AuthController
import com.example.bookboard.controller.BookPostController
import com.example.bookboard.databinding.FragmentHomeBinding
import com.example.bookboard.model.BookPost

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val bookPostController = BookPostController()
    private val authController = AuthController()
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

        // Check if user is logged in
        if (!authController.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        setupSearchListener()
        loadPosts()
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

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            bookPostController.refreshPosts(this)
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
                bookPostController.searchPosts(query, this@HomeFragment)
            }
        })
    }

    private fun loadPosts() {
        bookPostController.loadAllPosts(this)
    }

    // UI Update Methods (called by controller)
    fun updatePosts(posts: List<BookPost>) {
        binding.swipeRefreshLayout.isRefreshing = false

        if (posts.isEmpty()) {
            binding.tvNoPosts.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.tvNoPosts.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            bookAdapter.submitList(posts)
        }
    }

    fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}