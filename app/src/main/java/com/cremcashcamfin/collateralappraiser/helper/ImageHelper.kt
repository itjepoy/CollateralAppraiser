package com.cremcashcamfin.collateralappraiser.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

/**
 * Utility object for handling common image-related tasks.
 */
object ImageHelper {

    /**
     * Creates a content URI for a new image in the MediaStore.
     * This is typically used when capturing a photo or saving a bitmap.
     *
     * @param context The context used to access the ContentResolver.
     * @return A URI pointing to the newly created image location, or null if failed.
     */
    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            // Generate a unique image name based on current timestamp
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        // Insert into external images content provider and return the Uri
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    /**
     * Converts a Bitmap into a JPEG-compressed byte array.
     *
     * @param bitmap The bitmap image to convert.
     * @return Byte array representing the JPEG-compressed image.
     */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        // Compress bitmap to JPEG with 90% quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    /**
     * Retrieves the file extension (e.g., "jpg", "png") from a given content URI.
     *
     * @param context The context used to resolve the content type.
     * @param uri The URI from which to determine the file extension.
     * @return The file extension string, or "jpg" as default if type is not resolvable.
     */
    fun getExtensionFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)

        return when {
            type != null -> MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "jpg"
            else -> "jpg" // Fallback default extension
        }
    }
}