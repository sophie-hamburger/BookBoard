package com.example.bookboard.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profileImagePath: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 