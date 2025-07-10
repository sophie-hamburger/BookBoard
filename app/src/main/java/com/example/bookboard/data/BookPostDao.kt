package com.example.bookboard.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookboard.model.BookPost

@Dao
interface BookPostDao {
    @Query("SELECT * FROM book_posts ORDER BY timestamp DESC")
    fun getAllPosts(): LiveData<List<BookPost>>

    @Query("SELECT * FROM book_posts ORDER BY timestamp DESC")
    suspend fun getAllPostsDirect(): List<BookPost>

    @Query("SELECT * FROM book_posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUser(userId: String): LiveData<List<BookPost>>

    @Query("SELECT * FROM book_posts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getPostsByUserDirect(userId: String): List<BookPost>

    @Query("SELECT * FROM book_posts WHERE id = :postId")
    suspend fun getPostById(postId: String): BookPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BookPost)

    @Update
    suspend fun updatePost(post: BookPost)

    @Delete
    suspend fun deletePost(post: BookPost)

    @Query("DELETE FROM book_posts WHERE userId = :userId")
    suspend fun deleteAllPostsByUser(userId: String)

    @Query("DELETE FROM book_posts")
    suspend fun deleteAllPosts()
}