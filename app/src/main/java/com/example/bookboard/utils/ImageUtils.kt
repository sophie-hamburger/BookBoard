package com.example.bookboard.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {

    private val cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", "ddh5wvv35",
        "api_key", "364245525954458",
        "api_secret", "pwmKmHrRl0Y2GXVGkFmJUVRv9aY"
    ))

    suspend fun uploadImageToCloudinary(context: Context, imageUri: Uri, type: String = "book_covers"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val publicId = "${type}_${System.currentTimeMillis()}"
                val options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image"
                )

                val inputStream = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    return@withContext null
                }

                val result = cloudinary.uploader().upload(inputStream, options)
                result["secure_url"] as String

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun uploadProfileImageToCloudinary(context: Context, imageUri: Uri): String? {
        return uploadImageToCloudinary(context, imageUri, "profile_pictures")
    }

    suspend fun deleteImageFromCloudinary(imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                if (imageUrl.isNotEmpty() && imageUrl.contains("cloudinary.com")) {
                    val publicId = extractPublicIdFromUrl(imageUrl)
                    if (publicId.isNotEmpty()) {
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun extractPublicIdFromUrl(url: String): String {
        return try {
            val parts = url.split("/")
            val filename = parts.last().split(".").first()
            filename
        } catch (e: Exception) {
            ""
        }
    }
}