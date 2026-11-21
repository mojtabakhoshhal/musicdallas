package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DallasMusicDatabase : RoomDatabase() {
    abstract fun musicDao(): DallasMusicDao

    companion object {
        @Volatile
        private var instance: DallasMusicDatabase? = null

        fun getInstance(context: Context): DallasMusicDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DallasMusicDatabase::class.java,
                    "dallas_music.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

object CatalogSeeder {
    private const val CATALOG_ASSET_NAME = "catalog.json"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun parseCatalogJson(json: String): List<TrackEntity> {
        val listType = Types.newParameterizedType(List::class.java, CatalogTrackJson::class.java)
        val adapter = moshi.adapter<List<CatalogTrackJson>>(listType)
        val parsed = adapter.fromJson(json).orEmpty()
        return parsed
            .filter { PersianCatalogNormalizer.isOriginalTrack(it.title) }
            .mapIndexed { index, item ->
                TrackEntity(
                    id = item.id.trim(),
                    artist = PersianCatalogNormalizer.normalizePersian(item.artist),
                    artistId = item.artistId.trim(),
                    title = PersianCatalogNormalizer.cleanTitle(item.title),
                    album = PersianCatalogNormalizer.normalizePersian(item.album),
                    source = item.source.ifBlank { "offline-bundled" },
                    remoteUrl = item.remoteUrl.trim(),
                    previewAsset = item.previewAsset.ifBlank { "audio/beat_1.wav" },
                    durationMs = item.durationMs.coerceAtLeast(1_000L),
                    lyrics = item.lyrics.trim(),
                    cover = item.cover.ifBlank { "drawable://img_cover_underground" },
                    isNew = item.isNew,
                    sortOrder = index,
                )
            }
    }

    suspend fun seedIfEmpty(context: Context, dao: DallasMusicDao): Int = withContext(Dispatchers.IO) {
        if (dao.getTrackCount() > 0) return@withContext 0
        val json = context.assets.open(CATALOG_ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val entities = parseCatalogJson(json)
        if (entities.isNotEmpty()) {
            dao.insertTracks(entities)
        }
        entities.size
    }
}
