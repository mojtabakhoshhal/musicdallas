package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["artist_id"]),
        Index(value = ["is_favorite"]),
        Index(value = ["is_downloaded"]),
    ],
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val artist: String,
    @ColumnInfo(name = "artist_id") val artistId: String,
    val title: String,
    val album: String = "",
    val source: String = "offline-bundled",
    @ColumnInfo(name = "remote_url") val remoteUrl: String = "",
    @ColumnInfo(name = "preview_asset") val previewAsset: String = "audio/beat_1.wav",
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 18_000L,
    val lyrics: String = "",
    val cover: String = "drawable://img_cover_underground",
    @ColumnInfo(name = "is_new") val isNew: Boolean = false,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_downloaded") val isDownloaded: Boolean = false,
    @ColumnInfo(name = "local_file_path") val localFilePath: String? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlist_id", "track_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["track_id"])],
)
data class PlaylistTrackCrossRef(
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "track_id") val trackId: String,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
)

data class PlaylistWithTracks(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = "playlist_id",
            entityColumn = "track_id",
        ),
    )
    val tracks: List<TrackEntity>,
)

data class ArtistSummary(
    @ColumnInfo(name = "artist_id") val artistId: String,
    @ColumnInfo(name = "artist") val artistName: String,
    @ColumnInfo(name = "track_count") val trackCount: Int,
    @ColumnInfo(name = "cover") val cover: String,
    @ColumnInfo(name = "first_sort") val firstSort: Int,
)
