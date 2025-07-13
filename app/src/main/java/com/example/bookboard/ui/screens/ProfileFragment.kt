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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bookboard.R
import com.example.bookboard.adapter.BookAdapter
import com.example.bookboard.databinding.FragmentProfileBinding
import com.example.bookboard.utils.ImageUtils
import com.example.bookboard.viewmodel.AuthViewModel
import com.example.bookboard.viewmodel.BookPostViewModel
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val bookPostViewModel: BookPostViewModel by activityViewModels()
    private lateinit var userPostsAdapter: BookAdapter

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Save the image to internal storage
                val imagePath = ImageUtils.saveProfileImageToInternalStorage(requireContext(), uri)
                if (imagePath != null) {
                    // Delete old profile picture if exists
                    authViewModel.userProfile.value?.profileImagePath?.let { oldPath ->
                        if (oldPath.isNotEmpty()) {
                            ImageUtils.deleteImage(oldPath)
                        }
                    }

                    // Update profile picture
                    authViewModel.updateProfilePicture(imagePath)
                    binding.ivProfilePicture.setImageURI(uri)
                    Toast.makeText(context, getString(R.string.msg_profile_picture_updated), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.msg_failed_to_save_image), Toast.LENGTH_SHORT).show()
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

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            bookPostViewModel.refreshPosts()
        }
    }

    private fun setupObservers() {
        authViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etProfileName.setText(it.name)
                binding.tvEmail.text = it.email

                // Load profile picture
                if (it.profileImagePath.isNotEmpty()) {
                    val file = File(it.profileImagePath)
                    if (file.exists()) {
                        binding.ivProfilePicture.setImageURI(Uri.fromFile(file))
                    }
                }
            }
        }

        bookPostViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
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
                authViewModel.updateUserProfile(name)
                Toast.makeText(context, getString(R.string.msg_profile_updated), Toast.LENGTH_SHORT).show()
            } else {
                binding.etProfileName.error = "Name is required"
            }
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}