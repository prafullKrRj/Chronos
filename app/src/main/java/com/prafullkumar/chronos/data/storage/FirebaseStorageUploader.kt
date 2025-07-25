package com.prafullkumar.chronos.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale
import dagger.Provides

@Singleton
class FirebaseStorageUploader @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) {

    suspend fun uploadImage(imageUri: Uri, reminderId: String): String {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        // Compress image
        val compressedImageData = compressImage(imageUri)

        // Create storage reference
        val imageRef = firebaseStorage.reference
            .child("users")
            .child(userId)
            .child("${reminderId}_image.jpg")

        // Upload compressed image
        val uploadTask = imageRef.putBytes(compressedImageData)
        uploadTask.await()

        // Get download URL
        return imageRef.downloadUrl.await().toString()
    }

    private fun compressImage(imageUri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Resize bitmap to max 800px on the longer side
        val resizedBitmap = resizeBitmap(bitmap, 800)

        // Compress to JPEG with 80% quality
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Clean up
        if (bitmap != resizedBitmap) {
            resizedBitmap.recycle()
        }
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return bitmap.scale(newWidth, newHeight)
    }
}

