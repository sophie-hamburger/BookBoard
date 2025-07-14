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
import androidx.navigation.fragment.navArgs
import com.example.bookboard.controller.BookPostController
import com.example.bookboard.databinding.FragmentEditPostBinding
import com.example.bookboard.model.BookPost
import com.example.bookboard.utils.ImageUtils
import com.squareup.picasso.Picasso

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private val bookPostController = BookPostController()
    private val args: EditPostFragmentArgs by navArgs()

    private var currentPost: BookPost? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageUrl: String = ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadPost()
    }

    private fun loadPost() {
        // In MVC, we need to get the post from the controller
        // For now, we'll load it directly from the database
        val database = com.example.bookboard.data.AppDatabase.getDatabase(requireContext())
        val repository = com.example.bookboard.repository.BookPostRepository(database.bookPostDao())

        lifecycleScope.launch {
            try {
                val posts = repository.getPostsByUser(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "")
                val post = posts.find { it.id == args.postId }
                if (post != null) {
                    currentPost = post
                    selectedImageUrl = post.imagePath
                    populateFields(post)
                } else {
                    showError("Post not found")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Failed to load post")
            }
        }
    }

    private fun populateFields(post: BookPost) {
        binding.etTitle.setText(post.title)
        binding.etAuthor.setText(post.author)
        binding.etReview.setText(post.review)
        binding.ratingBar.rating = post.rating

        // Display image if exists
        if (post.imagePath.isNotEmpty()) {
            Picasso.get()
                .load(post.imagePath)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivSelectedImage)
            binding.ivSelectedImage.visibility = View.VISIBLE
            binding.btnSelectImage.text = "Change Image"
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdatePost.setOnClickListener {
            updatePost()
        }

        binding.btnDeletePost.setOnClickListener {
            deletePost()
        }

        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        context?.let { context ->
            selectedImageUri = uri
            // Display the selected image immediately using the URI
            Picasso.get()
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivSelectedImage)
            binding.ivSelectedImage.visibility = View.VISIBLE
            binding.btnSelectImage.text = "Change Image"
        }
    }

    private fun updatePost() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val review = binding.etReview.text.toString().trim()
        val rating = binding.ratingBar.rating

        if (validateInput(title, author, review)) {
            currentPost?.let { post ->
                // Upload new image to Cloudinary if selected
                lifecycleScope.launch {
                    try {
                        showLoading(true)

                        var finalImageUrl = selectedImageUrl
                        val imageUri = selectedImageUri
                        if (imageUri != null) {
                            val uploadedUrl = ImageUtils.uploadImageToCloudinary(requireContext(), imageUri)
                            if (uploadedUrl != null) {
                                finalImageUrl = uploadedUrl
                            } else {
                                showLoading(false)
                                showError("Failed to upload image")
                                return@launch
                            }
                        }

                        val updatedPost = post.copy(
                            title = title,
                            author = author,
                            review = review,
                            rating = rating,
                            imagePath = finalImageUrl
                        )
                        bookPostController.updatePost(updatedPost, this@EditPostFragment)
                    } catch (e: Exception) {
                        showLoading(false)
                        showError(e.message ?: "Failed to update post")
                    }
                }
            }
        }
    }

    private fun deletePost() {
        currentPost?.let { post ->
            // Delete the image from Cloudinary if it exists
            if (post.imagePath.isNotEmpty()) {
                lifecycleScope.launch {
                    ImageUtils.deleteImageFromCloudinary(post.imagePath)
                }
            }
            bookPostController.deletePost(post, this)
        }
    }

    private fun validateInput(title: String, author: String, review: String): Boolean {
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return false
        }

        if (author.isEmpty()) {
            binding.etAuthor.error = "Author is required"
            return false
        }

        if (review.isEmpty()) {
            binding.etReview.error = "Review is required"
            return false
        }

        return true
    }

    // UI Update Methods (called by controller)
    fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnUpdatePost.isEnabled = !isLoading
        binding.btnDeletePost.isEnabled = !isLoading
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}