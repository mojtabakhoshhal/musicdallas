package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogTrackJson(
    val id: String,
    val artist: String,
    @Json(name = "artist_id") val artistId: String,
    val title: String,
    val album: String = "",
    val source: String = "offline-bundled",
    val remoteUrl: String = "",
    val previewAsset: String = "audio/beat_1.wav",
    val durationMs: Long = 18_000L,
    val lyrics: String = "",
    val cover: String = "drawable://img_cover_underground",
    val isNew: Boolean = false,
)

object PersianCatalogNormalizer {
    private val bannedTerms = listOf(
        "ریمیکس",
        "remix",
        "میکس",
        "کالکشن",
        "تمام آهنگهای",
        "تمام آهنگ‌های",
        "playlist",
    )

    private val downloadPrefixRegex = Regex("""^دانلود\s+آهنگ\s+.*?\s+به\s+نام\s+""")

    fun normalizePersian(raw: String): String {
        return raw
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ۀ', 'ه')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun cleanTitle(raw: String): String {
        val normalized = normalizePersian(raw)
        return downloadPrefixRegex.replace(normalized, "").trim()
    }

    fun isOriginalTrack(title: String): Boolean {
        val cleaned = cleanTitle(title).lowercase()
        if (cleaned.isBlank()) return false
        return bannedTerms.none { cleaned.contains(it) }
    }

    fun normalizeForSearch(raw: String): String {
        return normalizePersian(raw)
            .replace('\u200C', ' ')
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }
}
