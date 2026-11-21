package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class DallasMusicRepository(
    private val context: Context,
    private val dao: DallasMusicDao,
    private val downloadManager: TrackDownloadManager = TrackDownloadManager(context),
    private val remoteSyncService: RemoteSyncService = RemoteSyncService(),
) {
    val allTracks: Flow<List<TrackEntity>> = dao.observeAllTracks()
    val newTracks: Flow<List<TrackEntity>> = dao.observeNewTracks()
    val artists: Flow<List<ArtistSummary>> = dao.observeArtists()
    val favoriteTracks: Flow<List<TrackEntity>> = dao.observeFavoriteTracks()
    val downloadedTracks: Flow<List<TrackEntity>> = dao.observeDownloadedTracks()
    val playlistsWithTracks: Flow<List<PlaylistWithTracks>> = dao.observePlaylistsWithTracks()

    fun observeTracksByArtist(artistId: String): Flow<List<TrackEntity>> =
        dao.observeTracksByArtist(artistId)

    fun observePlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?> =
        dao.observePlaylistWithTracks(playlistId)

    suspend fun ensureCatalogSeeded(): Int = CatalogSeeder.seedIfEmpty(context, dao)

    suspend fun syncCatalogFromUrl(catalogUrl: String): CatalogSyncResult {
        val fetched = remoteSyncService.fetchRemoteCatalog(catalogUrl)
        return mergeFetchedTracks(fetched)
    }

    suspend fun mergeFetchedTracks(fetchedTracks: List<TrackEntity>): CatalogSyncResult {
        if (fetchedTracks.isEmpty()) return CatalogSyncResult(0, 0, 0)
        val existingById = dao.getAllTracksSnapshot().associateBy { it.id }
        var added = 0
        var updated = 0

        val merged = fetchedTracks.map { incoming ->
            val current = existingById[incoming.id]
            if (current == null) {
                added++
                incoming
            } else {
                updated++
                incoming.copy(
                    isFavorite = current.isFavorite,
                    isDownloaded = current.isDownloaded,
                    localFilePath = current.localFilePath,
                    remoteUrl = incoming.remoteUrl.ifBlank { current.remoteUrl },
                    source = if (incoming.remoteUrl.isNotBlank()) incoming.source else current.source,
                )
            }
        }
        dao.upsertTracks(merged)
        return CatalogSyncResult(
            totalFetched = fetchedTracks.size,
            newlyAdded = added,
            updatedExisting = updated,
        )
    }

    suspend fun checkForAppUpdate(versionUrl: String): AppUpdateInfo =
        remoteSyncService.checkForUpdate(versionUrl)

    suspend fun toggleFavorite(trackId: String) {
        dao.toggleFavorite(trackId)
    }

    suspend fun toggleDownload(track: TrackEntity): Boolean {
        return if (track.isDownloaded) {
            downloadManager.deleteLocalTrack(track.localFilePath)
            dao.updateDownloadState(track.id, isDownloaded = false, localFilePath = null)
            false
        } else {
            val file = downloadManager.downloadTrack(track)
            dao.updateDownloadState(track.id, isDownloaded = true, localFilePath = file.absolutePath)
            true
        }
    }

    suspend fun updateRemoteAudioUrl(trackId: String, remoteUrl: String) {
        val cleaned = remoteUrl.trim()
        val source = if (cleaned.startsWith("http://", true) || cleaned.startsWith("https://", true)) {
            "custom-stream"
        } else {
            "offline-bundled"
        }
        dao.updateRemoteAudioSource(trackId, cleaned, source)
    }

    suspend fun attachLocalAudioToTrack(trackId: String, uri: Uri): File {
        val (file, _) = downloadManager.copyDocumentToLocalFile(trackId, uri)
        dao.updateDownloadState(trackId, isDownloaded = true, localFilePath = file.absolutePath)
        return file
    }

    suspend fun importStandaloneLocalTrack(uri: Uri): TrackEntity {
        val id = "local_${UUID.randomUUID().toString().take(8)}"
        val (file, displayName) = downloadManager.copyDocumentToLocalFile(id, uri)
        val cleanTitle = displayName
            ?.substringBeforeLast('.')
            ?.let { PersianCatalogNormalizer.normalizePersian(it) }
            ?.ifBlank { "آهنگ محلی" }
            ?: "آهنگ محلی"

        val track = TrackEntity(
            id = id,
            artist = "آرشیو شخصی",
            artistId = "local_device",
            title = cleanTitle,
            album = "وارد شده از گوشی",
            source = "local-device",
            remoteUrl = "",
            previewAsset = "audio/beat_1.wav",
            durationMs = 180_000L,
            lyrics = "فایل صوتی واردشده از حافظه دستگاه.",
            cover = "drawable://img_cover_trap",
            isNew = false,
            isFavorite = false,
            isDownloaded = true,
            localFilePath = file.absolutePath,
            sortOrder = 9999,
        )
        dao.upsertTrack(track)
        return track
    }

    suspend fun createPlaylist(name: String): Long {
        val normalized = PersianCatalogNormalizer.normalizePersian(name)
        return dao.insertPlaylist(PlaylistEntity(name = normalized))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId = playlistId, trackId = trackId)
    }
}
