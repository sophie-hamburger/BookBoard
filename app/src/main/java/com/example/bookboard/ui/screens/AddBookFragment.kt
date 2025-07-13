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
import com.example.bookboard.R
import com.example.bookboard.controller.BookPostController
import com.example.bookboard.databinding.FragmentAddBookBinding
import com.example.bookboard.utils.ImageUtils
import com.squareup.picasso.Picasso
import java.io.File

class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    private val bookPostController = BookPostController()
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
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnAddPost.setOnClickListener {
            createPost()
        }

        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val review = binding.etReview.text.toString().trim()
        val rating = binding.ratingBar.rating

        if (validateInput(title, author, review)) {
            bookPostController.createPost(title, author, review, rating, selectedImagePath, this)
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

    // UI Update Methods (called by controller)
    fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnAddPost.isEnabled = !isLoading
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun navigateBack() {
        findNavController().navigate(R.id.action_addBookFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}