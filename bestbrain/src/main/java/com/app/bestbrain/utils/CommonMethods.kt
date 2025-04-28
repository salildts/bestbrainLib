package com.app.bestbrain.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
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

    fun isPrimitiveAndValid(value: Any?): Boolean {
        return when (value) {
            is String -> value.isNotBlank()
            is Int, is Double, is Float,
            is Long, is Short-> true
            else -> false
        }
    }

    fun isNonStringPrimitive(value: Any?): Boolean {
        return when (value) {
            is Int, is Double, is Float,
            is Long, is Short-> true
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

}