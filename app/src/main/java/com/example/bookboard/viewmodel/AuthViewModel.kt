package com.example.bookboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookboard.data.AppDatabase
import com.example.bookboard.model.User
import com.example.bookboard.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _currentUser.value = auth.currentUser
        auth.currentUser?.let { user ->
            loadUserProfile(user.uid)
        }
    }

    fun signUp(email: String, password: String, name: String, profileImagePath: String = "") {
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = email,
                            name = name,
                            profileImagePath = profileImagePath
                        )
                        viewModelScope.launch {
                            userRepository.insertUser(newUser)
                            _currentUser.value = firebaseUser
                            _userProfile.value = newUser
                            _isLoading.value = false
                        }
                    }
                } else {
                    _isLoading.value = false
                }
            }
    }

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        _currentUser.value = firebaseUser
                        loadUserProfile(firebaseUser.uid)
                    }
                }
                _isLoading.value = false
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.syncUserFromFirestore(userId)
                userRepository.getUserById(userId).observeForever { user ->
                    _userProfile.value = user
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun updateUserProfile(name: String) {
        val currentUser = _currentUser.value ?: return
        val updatedUser = _userProfile.value?.copy(name = name) ?: return

        viewModelScope.launch {
            try {
                userRepository.updateUser(updatedUser)
                _userProfile.value = updatedUser
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun updateProfilePicture(profileImagePath: String) {
        val currentUser = _currentUser.value ?: return
        val updatedUser = _userProfile.value?.copy(profileImagePath = profileImagePath) ?: return

        viewModelScope.launch {
            try {
                userRepository.updateUser(updatedUser)
                _userProfile.value = updatedUser
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}