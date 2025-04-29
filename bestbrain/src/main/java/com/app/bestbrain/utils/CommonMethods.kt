package com.app.bestbrain.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommonMethods {

    fun getCurrentDateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }

    fun createImageFile(context: Context): Pair<File, Uri> {
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        return Pair(photoFile, photoUri)
    }

    fun uriToBytes(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: ByteArray(0)
    }

    fun fileToBytes(file: File): ByteArray {
        return file.inputStream().use {
            it.readBytes()
        }
    }

    fun isPrimitiveAndValid(value: Any?): Boolean {
        return when (value) {
            is String -> value.isNotBlank()
            is Int, is Double, is Float,
            is Long, is Short -> true

            else -> false
        }
    }

    fun isNonStringPrimitive(value: Any?): Boolean {
        return when (value) {
            is Int, is Double, is Float,
            is Long, is Short -> true

            else -> false
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result
    }

    /*fun copyUriToFile(context: Context, uri: Uri): File {
        val destinationFile = File(context.cacheDir, getFileName(context, uri)?:"temp_file.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return destinationFile
    }*/

    fun compressImageUriToByteArray(context: Context, uri: Uri, quality: Int = 75): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun resizeAndCompressImageUriToByteArray(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val resizedBitmap = resizeBitmapKeepingRatio(originalBitmap, maxWidth, maxHeight)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmapKeepingRatio(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = ((maxHeight.toFloat() * ratioBitmap).toInt())
        } else {
            finalHeight = ((maxWidth.toFloat() / ratioBitmap).toInt())
        }

        return bitmap.scale(finalWidth, finalHeight)
    }

    fun saveByteArrayToImageFile(byteArray: ByteArray, outputFile: File): Boolean {
        return try {
            outputFile.outputStream().use { outputStream ->
                outputStream.write(byteArray)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}