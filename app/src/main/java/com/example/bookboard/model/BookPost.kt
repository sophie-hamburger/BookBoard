package com.example.bookboard.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_posts")
data class BookPost(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val author: String = "",
    val review: String = "",
    val rating: Float = 0f,
    val imagePath: String = "",
    val timestamp: Long = System.currentTimeMillis()
)