package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class TrackDownloadManager(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    private val downloadsDir: File
        get() = File(context.filesDir, "downloads").apply {
            if (!exists()) mkdirs()
        }

    suspend fun downloadTrack(track: TrackEntity): File = withContext(Dispatchers.IO) {
        val remoteUrl = track.remoteUrl.trim()
        val hasRemoteHttp = remoteUrl.startsWith("https://", ignoreCase = true) ||
            remoteUrl.startsWith("http://", ignoreCase = true)

        val extension = if (hasRemoteHttp) "mp3" else "wav"
        val targetFile = File(downloadsDir, "${track.id}.$extension")

        if (hasRemoteHttp) {
            val request = Request.Builder().url(remoteUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}")
                }
                val body = response.body ?: throw IOException("Empty response body")
                targetFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
        } else {
            val assetPath = track.previewAsset.ifBlank { "audio/beat_1.wav" }
            context.assets.open(assetPath).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        targetFile
    }

    suspend fun deleteLocalTrack(localFilePath: String?) = withContext(Dispatchers.IO) {
        if (localFilePath.isNullOrBlank()) return@withContext
        val file = File(localFilePath)
        if (file.exists()) {
            file.delete()
        }
    }

    suspend fun copyDocumentToLocalFile(trackId: String, uri: Uri): Pair<File, String?> =
        withContext(Dispatchers.IO) {
            var displayName: String? = null
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) {
                            displayName = cursor.getString(idx)
                        }
                    }
                }

            val ext = displayName?.substringAfterLast('.', "")?.lowercase()
                ?.takeIf { it in setOf("mp3", "m4a", "wav", "ogg", "flac", "aac") }
                ?: "mp3"

            val targetFile = File(downloadsDir, "local_${trackId}.$ext")
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open selected audio file")
            input.use { stream ->
                targetFile.outputStream().use { output ->
                    stream.copyTo(output)
                }
            }
            targetFile to displayName
        }
}
