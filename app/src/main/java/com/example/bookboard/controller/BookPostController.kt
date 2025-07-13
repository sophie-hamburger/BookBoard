package com.example.bookboard.controller

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.example.bookboard.data.AppDatabase
import com.example.bookboard.model.BookPost
import com.example.bookboard.repository.BookPostRepository
import com.example.bookboard.repository.UserRepository
import com.example.bookboard.ui.screens.AddBookFragment
import com.example.bookboard.ui.screens.EditPostFragment
import com.example.bookboard.ui.screens.HomeFragment
import com.example.bookboard.ui.screens.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class BookPostController {

    private val auth = FirebaseAuth.getInstance()

    // Load all posts for home page
    fun loadAllPosts(fragment: HomeFragment) {
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)
                repository.syncPostsFromFirestore()
                val posts = repository.getAllPosts()
                fragment.updatePosts(posts)
                fragment.showLoading(false)
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Failed to load posts")
            }
        }
    }

    // Load user posts for profile page
    fun loadUserPosts(fragment: ProfileFragment) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                val posts = repository.getPostsByUser(currentUser.uid)
                fragment.updateUserPosts(posts)
            } catch (e: Exception) {
                fragment.showError(e.message ?: "Failed to load user posts")
            }
        }
    }

    // Create new post
    fun createPost(
        title: String,
        author: String,
        review: String,
        rating: Float,
        imagePath: String,
        fragment: AddBookFragment
    ) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)

                val post = BookPost(
                    id = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userName = "User",
                    title = title,
                    author = author,
                    review = review,
                    rating = rating,
                    imagePath = imagePath
                )

                repository.insertPost(post)
                fragment.showLoading(false)
                fragment.navigateBack()
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Failed to create post")
            }
        }
    }

    // Update existing post
    fun updatePost(post: BookPost, fragment: EditPostFragment) {
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)
                repository.updatePost(post)
                fragment.showLoading(false)
                fragment.navigateBack()
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Failed to update post")
            }
        }
    }

    // Delete post
    fun deletePost(post: BookPost, fragment: EditPostFragment) {
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)
                repository.deletePost(post)
                fragment.showLoading(false)
                fragment.navigateBack()
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Failed to delete post")
            }
        }
    }

    // Search posts
    fun searchPosts(query: String, fragment: HomeFragment) {
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val repository = BookPostRepository(database.bookPostDao())

        fragment.lifecycleScope.launch {
            try {
                val allPosts = repository.getAllPosts()
                val filteredPosts = if (query.isNotEmpty()) {
                    allPosts.filter { post ->
                        post.title.contains(query, ignoreCase = true) ||
                                post.author.contains(query, ignoreCase = true)
                    }
                } else {
                    allPosts
                }

                val sortedPosts = filteredPosts.sortedByDescending { it.timestamp }
                fragment.updatePosts(sortedPosts)
            } catch (e: Exception) {
                fragment.showError(e.message ?: "Failed to search posts")
            }
        }
    }

    // Refresh posts (for pull-to-refresh)
    fun refreshPosts(fragment: HomeFragment) {
        loadAllPosts(fragment)
    }

    fun refreshUserPosts(fragment: ProfileFragment) {
        loadUserPosts(fragment)
    }
}