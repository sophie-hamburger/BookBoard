package com.example.bookboard.repository

import com.example.bookboard.data.BookPostDao
import com.example.bookboard.model.BookPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookPostRepository(
    private val bookPostDao: BookPostDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getAllPosts(): List<BookPost> {
        return withContext(Dispatchers.IO) {
            bookPostDao.getAllPosts()
        }
    }

    suspend fun getPostsByUser(userId: String): List<BookPost> {
        return withContext(Dispatchers.IO) {
            bookPostDao.getPostsByUser(userId)
        }
    }

    suspend fun getPostById(postId: String): BookPost? {
        return bookPostDao.getPostById(postId)
    }

    suspend fun insertPost(post: BookPost) {
        withContext(Dispatchers.IO) {
            // Save to local database
            bookPostDao.insertPost(post)

            // Save to Firestore
            try {
                firestore.collection("posts").document(post.id).set(post).await()
            } catch (e: Exception) {
                // Handle error - could implement retry mechanism
            }
        }
    }

    suspend fun updatePost(post: BookPost) {
        withContext(Dispatchers.IO) {
            // Update local database
            bookPostDao.updatePost(post)

            // Update Firestore
            try {
                firestore.collection("posts").document(post.id).set(post).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun deletePost(post: BookPost) {
        withContext(Dispatchers.IO) {
            // Delete from local database
            bookPostDao.deletePost(post)

            // Delete from Firestore
            try {
                firestore.collection("posts").document(post.id).delete().await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun syncPostsFromFirestore() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("posts").get().await()
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BookPost::class.java)?.copy(id = doc.id)
                }

                // Clear local database and insert fresh data
                bookPostDao.deleteAllPosts()
                posts.forEach { post ->
                    bookPostDao.insertPost(post)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
