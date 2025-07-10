package com.example.bookboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookboard.data.AppDatabase
import com.example.bookboard.model.BookPost
import com.example.bookboard.repository.BookPostRepository
import com.example.bookboard.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class BookPostViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BookPostRepository(database.bookPostDao())
    private val userRepository = UserRepository(database.userDao())
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<BookPost>>()
    val posts: LiveData<List<BookPost>> = _posts

    private val _userPosts = MutableLiveData<List<BookPost>>()
    val userPosts: LiveData<List<BookPost>> = _userPosts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Simple search state
    private var currentSearchQuery = ""

    init {
        loadAllPosts()
        loadUserPosts()
    }

    fun clearError() {
        _errorMessage.value = ""
    }

    fun refreshPosts() {
        // Clear current data first
        _posts.value = emptyList()
        _userPosts.value = emptyList()

        // Then reload
        loadAllPosts()
        loadUserPosts()
    }

    fun loadAllPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.syncPostsFromFirestore()
                val posts = repository.getAllPostsDirect()
                applySearch(posts)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to load posts"
            }
        }
    }

    fun loadUserPosts() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val posts = repository.getPostsByUserDirect(currentUser.uid)
                _userPosts.value = posts
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user posts"
            }
        }
    }

    fun searchPosts(query: String) {
        currentSearchQuery = query
        viewModelScope.launch {
            try {
                val allPosts = repository.getAllPostsDirect()
                applySearch(allPosts)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to search posts"
            }
        }
    }

    private fun applySearch(posts: List<BookPost>) {
        var filteredPosts = posts

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            filteredPosts = posts.filter { post ->
                post.title.contains(currentSearchQuery, ignoreCase = true) ||
                        post.author.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Always sort by newest first
        filteredPosts = filteredPosts.sortedByDescending { it.timestamp }

        _posts.value = filteredPosts
    }

    fun createPost(title: String, author: String, review: String, rating: Float, imagePath: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val post = BookPost(
                    id = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userName = "User", // Simple default
                    title = title,
                    author = author,
                    review = review,
                    rating = rating,
                    imagePath = imagePath
                )

                repository.insertPost(post)

                // Refresh posts after creating new post
                loadAllPosts()
                loadUserPosts()

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to create post"
            }
        }
    }

    fun updatePost(post: BookPost) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updatePost(post)

                // Refresh posts after updating
                loadAllPosts()
                loadUserPosts()

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to update post"
            }
        }
    }

    fun deletePost(post: BookPost) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deletePost(post)

                // Refresh posts after deleting
                loadAllPosts()
                loadUserPosts()

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to delete post"
            }
        }
    }
}