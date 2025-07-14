package com.example.bookboard.controller

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.example.bookboard.data.AppDatabase
import com.example.bookboard.model.User
import com.example.bookboard.repository.UserRepository
import com.example.bookboard.ui.screens.LoginFragment
import com.example.bookboard.ui.screens.ProfileFragment
import com.example.bookboard.ui.screens.SignupFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthController {

    private val auth = FirebaseAuth.getInstance()

    // Login user
    fun loginUser(email: String, password: String, fragment: LoginFragment) {
        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        fragment.showLoading(false)
                        fragment.navigateToHome()
                    }
                    .addOnFailureListener { exception ->
                        fragment.showLoading(false)
                        fragment.showError(exception.message ?: "Login failed")
                    }
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Login failed")
            }
        }
    }

    // Signup user
    fun signupUser(email: String, password: String, name: String, profileImagePath: String, fragment: SignupFragment) {
        fragment.lifecycleScope.launch {
            try {
                fragment.showLoading(true)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        // Create user profile in database
                        val user = User(
                            id = result.user?.uid ?: "",
                            name = name,
                            email = email,
                            profileImagePath = profileImagePath
                        )

                        // Launch a new coroutine to insert user
                        fragment.lifecycleScope.launch {
                            try {
                                val database = AppDatabase.getDatabase(fragment.requireContext())
                                val userRepository = UserRepository(database.userDao())
                                userRepository.insertUser(user)

                                // Verify the user was saved correctly
                                val savedUser = userRepository.getUserById(user.id)
                                if (savedUser != null) {
                                    fragment.showLoading(false)
                                    fragment.navigateToHome()
                                } else {
                                    fragment.showLoading(false)
                                    fragment.showError("Failed to verify user creation")
                                }
                            } catch (e: Exception) {
                                fragment.showLoading(false)
                                fragment.showError(e.message ?: "Failed to create user profile")
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        fragment.showLoading(false)
                        fragment.showError(exception.message ?: "Signup failed")
                    }
            } catch (e: Exception) {
                fragment.showLoading(false)
                fragment.showError(e.message ?: "Signup failed")
            }
        }
    }

    // Logout user
    fun logoutUser(fragment: ProfileFragment) {
        auth.signOut()
        fragment.navigateToLogin()
    }

    // Get current user profile
    fun loadUserProfile(fragment: ProfileFragment) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val userRepository = UserRepository(database.userDao())

        fragment.lifecycleScope.launch {
            try {
                // First try to sync from Firestore
                userRepository.syncUserFromFirestore(currentUser.uid)

                val user = userRepository.getUserById(currentUser.uid)
                if (user != null) {
                    fragment.updateUserProfile(user)
                } else {
                    // Create default user profile if not exists
                    val newUser = User(
                        id = currentUser.uid,
                        name = currentUser.displayName ?: "User",
                        email = currentUser.email ?: "",
                        profileImagePath = ""
                    )
                    userRepository.insertUser(newUser)
                    fragment.updateUserProfile(newUser)
                }
            } catch (e: Exception) {
                fragment.showError(e.message ?: "Failed to load user profile")
            }
        }
    }

    // Update user profile
    fun updateUserProfile(name: String, fragment: ProfileFragment) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val userRepository = UserRepository(database.userDao())

        fragment.lifecycleScope.launch {
            try {
                val existingUser = userRepository.getUserById(currentUser.uid)
                if (existingUser != null) {
                    val updatedUser = existingUser.copy(name = name)
                    userRepository.updateUser(updatedUser)
                    fragment.updateUserProfile(updatedUser)
                }
            } catch (e: Exception) {
                fragment.showError(e.message ?: "Failed to update profile")
            }
        }
    }

    // Update profile picture
    fun updateProfilePicture(imagePath: String, fragment: ProfileFragment) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val userRepository = UserRepository(database.userDao())

        fragment.lifecycleScope.launch {
            try {
                val existingUser = userRepository.getUserById(currentUser.uid)
                if (existingUser != null) {
                    val updatedUser = existingUser.copy(profileImagePath = imagePath)
                    userRepository.updateUser(updatedUser)
                    fragment.updateUserProfile(updatedUser)
                }
            } catch (e: Exception) {
                fragment.showError(e.message ?: "Failed to update profile picture")
            }
        }
    }

    // Update both name and profile picture together
    fun updateUserProfileAndPicture(name: String, imagePath: String?, fragment: ProfileFragment) {
        val currentUser = auth.currentUser ?: return
        val database = AppDatabase.getDatabase(fragment.requireContext())
        val userRepository = UserRepository(database.userDao())

        fragment.lifecycleScope.launch {
            try {
                val existingUser = userRepository.getUserById(currentUser.uid)
                if (existingUser != null) {
                    val updatedUser = if (imagePath != null) {
                        existingUser.copy(name = name, profileImagePath = imagePath)
                    } else {
                        existingUser.copy(name = name)
                    }
                    userRepository.updateUser(updatedUser)
                    fragment.updateUserProfile(updatedUser)
                    android.util.Log.d("AuthController", "Profile updated: name=$name, imagePath=$imagePath")
                } else {
                    android.util.Log.e("AuthController", "User not found for ID: ${currentUser.uid}")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthController", "Error updating profile", e)
                fragment.showError(e.message ?: "Failed to update profile")
            }
        }
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}