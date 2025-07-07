package com.example.bookboard.model

import com.google.firebase.firestore.FirebaseFirestore

object BookPostFirebase {
    private val db = FirebaseFirestore.getInstance()

    fun addPost(post: BookPost, onComplete: () -> Unit) {
        db.collection("posts").document(post.id).set(post.toJson())
            .addOnSuccessListener { onComplete() }
    }

    fun getAllPosts(onComplete: (List<BookPost>) -> Unit) {
        db.collection("posts").get().addOnSuccessListener { result ->
            val posts = result.map { BookPost.fromJson(it.data) }
            onComplete(posts)
        }
    }
}