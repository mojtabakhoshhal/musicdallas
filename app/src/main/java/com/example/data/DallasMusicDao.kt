package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DallasMusicDao {

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksSnapshot(): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: TrackEntity)

    @Query("SELECT * FROM tracks ORDER BY sort_order ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE is_new = 1 ORDER BY sort_order ASC")
    fun observeNewTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist_id = :artistId ORDER BY sort_order ASC")
    fun observeTracksByArtist(artistId: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId LIMIT 1")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query(
        """
        SELECT artist_id, artist, COUNT(*) AS track_count, MIN(cover) AS cover, MIN(sort_order) AS first_sort
        FROM tracks
        GROUP BY artist_id, artist
        ORDER BY first_sort ASC
        """,
    )
    fun observeArtists(): Flow<List<ArtistSummary>>

    @Query("SELECT * FROM tracks WHERE is_favorite = 1 ORDER BY sort_order ASC")
    fun observeFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE is_downloaded = 1 ORDER BY sort_order ASC")
    fun observeDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("UPDATE tracks SET is_favorite = NOT is_favorite WHERE id = :trackId")
    suspend fun toggleFavorite(trackId: String)

    @Query(
        """
        UPDATE tracks
        SET is_downloaded = :isDownloaded, local_file_path = :localFilePath
        WHERE id = :trackId
        """,
    )
    suspend fun updateDownloadState(
        trackId: String,
        isDownloaded: Boolean,
        localFilePath: String?,
    )

    @Query(
        """
        UPDATE tracks
        SET remote_url = :remoteUrl, source = :source
        WHERE id = :trackId
        """,
    )
    suspend fun updateRemoteAudioSource(
        trackId: String,
        remoteUrl: String,
        source: String,
    )

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY created_at DESC")
    fun observePlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun observePlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
}
