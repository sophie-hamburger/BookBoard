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
import androidx.navigation.fragment.navArgs
import com.example.bookboard.databinding.FragmentEditPostBinding
import com.example.bookboard.model.BookPost
import com.example.bookboard.utils.ImageUtils
import com.example.bookboard.viewmodel.BookPostViewModel
import com.squareup.picasso.Picasso
import java.io.File

class EditPostFragment : Fragment() {
    
    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!
    
    private val bookPostViewModel: BookPostViewModel by activityViewModels()
    private val args: EditPostFragmentArgs by navArgs()
    
    private var currentPost: BookPost? = null
    private var selectedImagePath: String = ""
    
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
        
        setupObservers()
        setupClickListeners()
        loadPost()
    }
    
    private fun loadPost() {
        // Find the post by ID from the user posts
        bookPostViewModel.userPosts.value?.find { it.id == args.postId }?.let { post ->
            currentPost = post
            selectedImagePath = post.imagePath
            populateFields(post)
        }
    }
    
    private fun populateFields(post: BookPost) {
        binding.etTitle.setText(post.title)
        binding.etAuthor.setText(post.author)
        binding.etReview.setText(post.review)
        binding.ratingBar.rating = post.rating
        
        // Display image if exists
        if (post.imagePath.isNotEmpty()) {
            val imageFile = File(post.imagePath)
            if (imageFile.exists()) {
                Picasso.get()
                    .load(imageFile)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivSelectedImage)
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.btnSelectImage.text = "Change Image"
            }
        }
    }
    
    private fun setupObservers() {
        bookPostViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnUpdatePost.isEnabled = !isLoading
            binding.btnDeletePost.isEnabled = !isLoading
        }
    }
    
    private fun setupClickListeners() {
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
            val imagePath = ImageUtils.saveImageToInternalStorage(context, uri)
            if (imagePath != null) {
                selectedImagePath = imagePath
                // Display the selected image
                Picasso.get()
                    .load(File(imagePath))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivSelectedImage)
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.btnSelectImage.text = "Change Image"
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updatePost() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val review = binding.etReview.text.toString().trim()
        val rating = binding.ratingBar.rating
        
        if (validateInput(title, author, review)) {
            currentPost?.let { post ->
                val updatedPost = post.copy(
                    title = title,
                    author = author,
                    review = review,
                    rating = rating,
                    imagePath = selectedImagePath
                )
                bookPostViewModel.updatePost(updatedPost)
                Toast.makeText(context, "Post updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }
    
    private fun deletePost() {
        currentPost?.let { post ->
            // Delete the image file if it exists
            if (post.imagePath.isNotEmpty()) {
                ImageUtils.deleteImage(post.imagePath)
            }
            bookPostViewModel.deletePost(post)
            Toast.makeText(context, "Post deleted successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 