package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {
    private const val TAG = "ImageStorageHelper"

    data class ImportedImage(
        val fullPath: String,
        val thumbPath: String,
        val width: Float,
        val height: Float
    )

    /**
     * Copies the image from the source Uri to internal filesDir, rotates it if necessary,
     * creates a downsampled compressed thumbnail, and returns paths and dimensions.
     */
    fun importImage(context: Context, sourceUri: Uri): ImportedImage? {
        return try {
            val contentResolver = context.contentResolver
            val id = UUID.randomUUID().toString()

            val isHttp = sourceUri.scheme?.startsWith("http", ignoreCase = true) == true
            val openStream = {
                if (isHttp) {
                    java.net.URL(sourceUri.toString()).openConnection().apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                    }.getInputStream()
                } else {
                    contentResolver.openInputStream(sourceUri)
                }
            }

            // Check if source image is a GIF
            val mimeType = if (isHttp) null else contentResolver.getType(sourceUri)
            val isGif = mimeType == "image/gif" || sourceUri.toString().endsWith(".gif", ignoreCase = true)

            // 1. Read bounds to get width/height
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var originalWidth = options.outWidth.toFloat()
            var originalHeight = options.outHeight.toFloat()
            if (originalWidth <= 0 || originalHeight <= 0) {
                // Fallback dimensions if decoding bounds failed
                originalWidth = 800f
                originalHeight = 600f
            }

            // Determine file extension based on MIME type or source URI
            val extension = when {
                mimeType == "image/png" || sourceUri.toString().endsWith(".png", ignoreCase = true) -> "png"
                mimeType == "image/webp" || sourceUri.toString().endsWith(".webp", ignoreCase = true) -> "webp"
                mimeType == "image/gif" || sourceUri.toString().endsWith(".gif", ignoreCase = true) -> "gif"
                else -> "jpg"
            }

            // Define destination files
            val fullFile = File(context.filesDir, "ref_full_$id.$extension")
            val thumbFile = File(context.filesDir, "ref_thumb_$id.jpg")

            if (isGif) {
                // Copy original GIF data to preserve anim frames
                copyStreamToFile(openStream(), fullFile)
                fullFile.copyTo(thumbFile, overwrite = true)

                return ImportedImage(
                    fullPath = fullFile.absolutePath,
                    thumbPath = thumbFile.absolutePath,
                    width = originalWidth,
                    height = originalHeight
                )
            }

            // 1. Get orientation and size information (for non-GIFs)
            var rotationDegrees = 0
            try {
                openStream()?.use { stream ->
                    val exifInterface = ExifInterface(stream)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading EXIF", e)
            }

            // 2. Direct Raw Copy of original file to preserve 100% of its original quality and details
            copyStreamToFile(openStream(), fullFile)
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                val temp = originalWidth
                originalWidth = originalHeight
                originalHeight = temp
            }

            // 3. Create downsampled thumbnail (target max dimension 400px)
            val thumbOptions = BitmapFactory.Options().apply {
                val maxDim = maxOf(options.outWidth, options.outHeight)
                var sample = 1
                while (maxDim / sample > 400) {
                    sample *= 2
                }
                inSampleSize = sample
            }

            var thumbBitmap = openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, thumbOptions)
            }

            if (thumbBitmap != null) {
                // Rotate thumbnail if needed
                if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    val rotatedThumb = Bitmap.createBitmap(
                        thumbBitmap, 0, 0, thumbBitmap.width, thumbBitmap.height, matrix, true
                    )
                    if (rotatedThumb != thumbBitmap) {
                        thumbBitmap.recycle()
                    }
                    thumbBitmap = rotatedThumb
                }
                FileOutputStream(thumbFile).use { out ->
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                thumbBitmap.recycle()
            } else {
                // Fallback: Copy the full file to thumb if decoding failed
                fullFile.copyTo(thumbFile, overwrite = true)
            }

            ImportedImage(
                fullPath = fullFile.absolutePath,
                thumbPath = thumbFile.absolutePath,
                width = originalWidth,
                height = originalHeight
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import image", e)
            null
        }
    }

    private fun copyStreamToFile(inputStream: InputStream?, outputFile: File) {
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Safely deletes full resolution and thumbnail files associated with a CanvasItem.
     */
    fun deleteImageFiles(fullPath: String, thumbPath: String) {
        try {
            val fullFile = File(fullPath)
            if (fullFile.exists()) {
                fullFile.delete()
            }
            val thumbFile = File(thumbPath)
            if (thumbFile.exists()) {
                thumbFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image files", e)
        }
    }
}
