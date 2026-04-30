package com.silas.omaster.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

data class ExifInfo(
    val dateTime: String?,
    val make: String?,
    val model: String?
)

object ExifReader {

    private const val TAG = "ExifReader"

    suspend fun read(context: Context, uri: Uri): ExifInfo = withContext(Dispatchers.IO) {
        try {
            val exif = try {
                val fd = context.contentResolver.openFileDescriptor(uri, "r")
                if (fd != null) {
                    ExifInterface(fd.fileDescriptor)
                } else {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        ExifInterface(stream)
                    } ?: return@withContext ExifInfo(null, null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open ExifInterface", e)
                return@withContext ExifInfo(null, null, null)
            }

            val dateTime = readDateTime(exif)
            val make = exif.getAttribute(ExifInterface.TAG_MAKE)
            val model = exif.getAttribute(ExifInterface.TAG_MODEL)

            ExifInfo(dateTime = dateTime, make = make, model = model)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read EXIF", e)
            ExifInfo(null, null, null)
        }
    }

    private fun readDateTime(exif: ExifInterface): String? {
        val raw = exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
            ?: return null

        return try {
            val inputFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(raw)
            val outputFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            raw.replace(":", ".").take(16)
        }
    }
}
