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
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authController = AuthController()
    private val bookPostController = BookPostController()
    private lateinit var userPostsAdapter: BookAdapter

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Save the image to internal storage
                val imagePath = ImageUtils.saveProfileImageToInternalStorage(requireContext(), uri)
                if (imagePath != null) {
                    // Update the profile picture in database
                    authController.updateProfilePicture(imagePath, this)
                    // Show immediate feedback
                    binding.ivProfilePicture.setImageURI(uri)
                    Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
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
                authController.updateUserProfile(name, this)
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
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
            val file = File(user.profileImagePath)
            if (file.exists()) {
                try {
                    binding.ivProfilePicture.setImageURI(Uri.fromFile(file))
                } catch (e: Exception) {
                    // If there's an error loading the image, show a placeholder
                    binding.ivProfilePicture.setImageResource(android.R.drawable.ic_menu_camera)
                }
            } else {
                // File doesn't exist, show placeholder
                binding.ivProfilePicture.setImageResource(android.R.drawable.ic_menu_camera)
            }
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