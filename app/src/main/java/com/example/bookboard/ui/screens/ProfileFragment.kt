package com.example.bookboard.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookboard.R
import com.example.bookboard.adapter.BookAdapter
import com.example.bookboard.controller.AuthController
import com.example.bookboard.controller.BookPostController
import com.example.bookboard.databinding.FragmentProfileBinding
import com.example.bookboard.model.BookPost
import com.example.bookboard.model.User
import com.example.bookboard.utils.ImageUtils
import com.squareup.picasso.Picasso
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authController = AuthController()
    private val bookPostController = BookPostController()
    private lateinit var userPostsAdapter: BookAdapter

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Show immediate feedback
                Picasso.get()
                    .load(uri)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_camera)
                    .into(binding.ivProfilePicture)
                Toast.makeText(context, "Profile picture selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        setupSwipeRefresh()
        setupClickListeners()
        loadData()
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

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            bookPostController.refreshUserPosts(this)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateProfilePicture.setOnClickListener {
            openImagePicker()
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            if (name.isNotEmpty()) {
                // Upload profile picture to Cloudinary if selected
                lifecycleScope.launch {
                    try {
                        val imageUri = selectedImageUri
                        if (imageUri != null) {
                            val uploadedUrl = ImageUtils.uploadProfileImageToCloudinary(requireContext(), imageUri)
                            if (uploadedUrl != null) {
                                authController.updateProfilePicture(uploadedUrl, this@ProfileFragment)
                            } else {
                                showError("Failed to upload profile picture")
                                return@launch
                            }
                        }

                        authController.updateUserProfile(name, this@ProfileFragment)
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        showError(e.message ?: "Failed to update profile")
                    }
                }
            } else {
                binding.etProfileName.error = "Name is required"
            }
        }

        binding.btnLogout.setOnClickListener {
            authController.logoutUser(this)
        }
    }

    private fun loadData() {
        authController.loadUserProfile(this)
        bookPostController.loadUserPosts(this)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // UI Update Methods (called by controller)
    fun updateUserProfile(user: User) {
        binding.etProfileName.setText(user.name)
        binding.tvEmail.text = user.email

        // Load profile picture
        if (user.profileImagePath.isNotEmpty()) {
            Picasso.get()
                .load(user.profileImagePath)
                .placeholder(android.R.drawable.ic_menu_camera)
                .error(android.R.drawable.ic_menu_camera)
                .into(binding.ivProfilePicture)
        } else {
            // No profile image path, show placeholder
            binding.ivProfilePicture.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    fun updateUserPosts(posts: List<BookPost>) {
        binding.swipeRefreshLayout.isRefreshing = false

        if (posts.isEmpty()) {
            binding.tvNoUserPosts.visibility = View.VISIBLE
            binding.recyclerViewUserPosts.visibility = View.GONE
        } else {
            binding.tvNoUserPosts.visibility = View.GONE
            binding.recyclerViewUserPosts.visibility = View.VISIBLE
            userPostsAdapter.submitList(posts)
        }
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateToLogin() {
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}