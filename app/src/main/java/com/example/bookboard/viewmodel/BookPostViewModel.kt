package com.example.bookboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookboard.data.AppDatabase
import com.example.bookboard.model.BookPost
import com.example.bookboard.repository.BookPostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class BookPostViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BookPostRepository(database.bookPostDao())
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<BookPost>>()
    val posts: LiveData<List<BookPost>> = _posts

    private val _userPosts = MutableLiveData<List<BookPost>>()
    val userPosts: LiveData<List<BookPost>> = _userPosts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadAllPosts()
        loadUserPosts()
    }

    fun clearError() {
        _errorMessage.value = ""
    }

    fun loadAllPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.syncPostsFromFirestore()
                repository.getAllPosts().observeForever { posts ->
                    _posts.value = posts
                    _isLoading.value = false
                }
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
                repository.getPostsByUser(currentUser.uid).observeForever { posts ->
                    _userPosts.value = posts
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user posts"
            }
        }
    }

    fun createPost(title: String, author: String, review: String, rating: Float, imagePath: String) {
        val currentUser = auth.currentUser ?: return

        val post = BookPost(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            userName = currentUser.displayName ?: "Anonymous",
            title = title,
            author = author,
            review = review,
            rating = rating,
            imagePath = imagePath
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertPost(post)
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
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to delete post"
            }
        }
    }
}