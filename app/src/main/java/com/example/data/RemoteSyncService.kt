package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class VersionManifestJson(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val releaseNotes: String = "",
    val androidDownloadUrl: String = "",
    val catalogUrl: String = "",
)

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean,
)

data class CatalogSyncResult(
    val totalFetched: Int,
    val newlyAdded: Int,
    val updatedExisting: Int,
)

object RemoteSyncConfig {
    // Replace YOUR_GITHUB_USERNAME with your GitHub username before building a production release
    const val DEFAULT_CATALOG_URL = ""
    const val DEFAULT_VERSION_URL = ""

    private const val PREFS_NAME = "dallas_sync_prefs"
    private const val KEY_CATALOG_URL = "remote_catalog_url"
    private const val KEY_VERSION_URL = "remote_version_url"

    fun getCatalogUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CATALOG_URL, DEFAULT_CATALOG_URL).orEmpty().trim()
    }

    fun getVersionUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_VERSION_URL, DEFAULT_VERSION_URL).orEmpty().trim()
    }

    fun saveUrls(context: Context, catalogUrl: String, versionUrl: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CATALOG_URL, catalogUrl.trim())
            .putString(KEY_VERSION_URL, versionUrl.trim())
            .apply()
    }
}

class RemoteSyncService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun fetchRemoteCatalog(url: String): List<TrackEntity> = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        require(trimmed.startsWith("https://", ignoreCase = true)) {
            "لینک کاتالوگ باید با https:// شروع شود"
        }
        val bodyText = executeGet(trimmed)
        CatalogSeeder.parseCatalogJson(bodyText)
    }

    suspend fun checkForUpdate(
        versionUrl: String,
        currentVersionCode: Int = BuildConfig.VERSION_CODE,
    ): AppUpdateInfo = withContext(Dispatchers.IO) {
        val trimmed = versionUrl.trim()
        require(trimmed.startsWith("https://", ignoreCase = true)) {
            "لینک version.json باید با https:// شروع شود"
        }
        val bodyText = executeGet(trimmed)
        val adapter = moshi.adapter(VersionManifestJson::class.java)
        val manifest = adapter.fromJson(bodyText)
            ?: throw IOException("ساختار فایل version.json معتبر نیست")

        AppUpdateInfo(
            latestVersionCode = manifest.latestVersionCode,
            latestVersionName = manifest.latestVersionName,
            releaseNotes = manifest.releaseNotes,
            downloadUrl = manifest.androidDownloadUrl.trim(),
            isUpdateAvailable = manifest.latestVersionCode > currentVersionCode,
        )
    }

    private fun executeGet(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-cache")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            return response.body?.string() ?: throw IOException("پاسخ سرور خالی بود")
        }
    }
}
