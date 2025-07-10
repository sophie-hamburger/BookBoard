package com.example.bookboard.repository

import com.example.bookboard.data.UserDao
import com.example.bookboard.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            // Save to local database
            userDao.insertUser(user)

            // Save to Firestore
            try {
                firestore.collection("users").document(user.id).set(user).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            // Update local database
            userDao.updateUser(user)

            // Update Firestore
            try {
                firestore.collection("users").document(user.id).set(user).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun deleteUser(user: User) {
        withContext(Dispatchers.IO) {
            // Delete from local database
            userDao.deleteUser(user)

            // Delete from Firestore
            try {
                firestore.collection("users").document(user.id).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun syncUserFromFirestore(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                val user = document.toObject(User::class.java)?.copy(id = document.id)
                user?.let {
                    userDao.insertUser(it)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}