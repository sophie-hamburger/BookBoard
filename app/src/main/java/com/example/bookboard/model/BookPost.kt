package com.example.bookboard.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookPost(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val review: String = "",
    val base64Image: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "review" to review,
            "base64Image" to base64Image,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any>): BookPost {
            return BookPost(
                id = json["id"] as? String ?: "",
                title = json["title"] as? String ?: "",
                review = json["review"] as? String ?: "",
                base64Image = json["base64Image"] as? String ?: "",
                timestamp = json["timestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}