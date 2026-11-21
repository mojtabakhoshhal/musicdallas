package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppUpdateInfo
import com.example.data.ArtistSummary
import com.example.data.DallasMusicDatabase
import com.example.data.DallasMusicRepository
import com.example.data.PersianCatalogNormalizer
import com.example.data.PlaylistWithTracks
import com.example.data.RemoteSyncConfig
import com.example.data.TrackEntity
import com.example.playback.PlaybackController
import com.example.playback.PlaybackUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val matchingArtists: List<ArtistSummary> = emptyList(),
    val matchingTracks: List<TrackEntity> = emptyList(),
)

data class SyncUiState(
    val catalogUrl: String = "",
    val versionUrl: String = "",
    val isSyncingCatalog: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val availableUpdate: AppUpdateInfo? = null,
)

class DallasMusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DallasMusicDatabase.getInstance(application)
    private val repository = DallasMusicRepository(application, database.musicDao())
    private val playbackController = PlaybackController(application)

    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val newTracks: StateFlow<List<TrackEntity>> = repository.newTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<ArtistSummary>> = repository.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteTracks: StateFlow<List<TrackEntity>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val downloadedTracks: StateFlow<List<TrackEntity>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<PlaylistWithTracks>> = repository.playlistsWithTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playbackState: StateFlow<PlaybackUiState> = playbackController.state

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        artists,
        allTracks,
    ) { query, allArtists, tracks ->
        val normalizedQuery = PersianCatalogNormalizer.normalizeForSearch(query)
        if (normalizedQuery.isBlank()) {
            SearchUiState(query = query)
        } else {
            val matchedArtists = allArtists.filter {
                PersianCatalogNormalizer.normalizeForSearch(it.artistName).contains(normalizedQuery) ||
                    it.artistId.lowercase().contains(normalizedQuery)
            }
            val matchedTracks = tracks.filter {
                PersianCatalogNormalizer.normalizeForSearch(it.title).contains(normalizedQuery) ||
                    PersianCatalogNormalizer.normalizeForSearch(it.artist).contains(normalizedQuery) ||
                    PersianCatalogNormalizer.normalizeForSearch(it.album).contains(normalizedQuery)
            }
            SearchUiState(
                query = query,
                matchingArtists = matchedArtists,
                matchingTracks = matchedTracks,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private val _downloadingTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTrackIds: StateFlow<Set<String>> = _downloadingTrackIds.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSeedingComplete = MutableStateFlow(false)
    val isSeedingComplete: StateFlow<Boolean> = _isSeedingComplete.asStateFlow()

    private val _syncState = MutableStateFlow(
        SyncUiState(
            catalogUrl = RemoteSyncConfig.getCatalogUrl(application),
            versionUrl = RemoteSyncConfig.getVersionUrl(application),
        ),
    )
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    init {
        playbackController.connect()
        viewModelScope.launch {
            runCatching { repository.ensureCatalogSeeded() }
            _isSeedingComplete.value = true
            val configuredCatalogUrl = _syncState.value.catalogUrl
            if (configuredCatalogUrl.startsWith("https://", ignoreCase = true)) {
                runCatching { repository.syncCatalogFromUrl(configuredCatalogUrl) }
            }
            val configuredVersionUrl = _syncState.value.versionUrl
            if (configuredVersionUrl.startsWith("https://", ignoreCase = true)) {
                runCatching { repository.checkForAppUpdate(configuredVersionUrl) }
                    .onSuccess { info ->
                        if (info.isUpdateAvailable) {
                            _syncState.update { it.copy(availableUpdate = info) }
                        }
                    }
            }
        }
        viewModelScope.launch {
            allTracks.collect { tracks ->
                val currentId = playbackState.value.currentTrack?.id ?: return@collect
                val latest = tracks.firstOrNull { it.id == currentId } ?: return@collect
                playbackController.updateTrackInQueue(latest)
            }
        }
    }

    fun observeArtistTracks(artistId: String): StateFlow<List<TrackEntity>> =
        repository.observeTracksByArtist(artistId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observePlaylist(playlistId: Long): StateFlow<PlaylistWithTracks?> =
        repository.observePlaylistWithTracks(playlistId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: TrackEntity, contextQueue: List<TrackEntity> = allTracks.value) {
        val queue = contextQueue.ifEmpty { listOf(track) }
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackController.playQueue(queue, startIndex = index, shuffle = false)
    }

    fun playQueueShuffled(tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) return
        playbackController.playQueue(tracks, startIndex = 0, shuffle = true)
    }

    fun playQueueSequential(tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) return
        playbackController.playQueue(tracks, startIndex = 0, shuffle = false)
    }

    fun togglePlayPause() = playbackController.togglePlayPause()
    fun skipNext() = playbackController.skipToNext()
    fun skipPrevious() = playbackController.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)
    fun toggleShuffle() = playbackController.toggleShuffle()
    fun cycleRepeatMode() = playbackController.cycleRepeatMode()
    fun clearPlaybackError() = playbackController.clearError()

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
        }
    }

    fun toggleDownload(track: TrackEntity) {
        if (_downloadingTrackIds.value.contains(track.id)) return
        viewModelScope.launch {
            _downloadingTrackIds.update { it + track.id }
            runCatching {
                repository.toggleDownload(track)
            }.onSuccess { downloaded ->
                _userMessage.value = if (downloaded) {
                    "«${track.title}» برای پخش آفلاین ذخیره شد"
                } else {
                    "فایل آفلاین «${track.title}» حذف شد"
                }
            }.onFailure { err ->
                _userMessage.value = "خطا در دانلود: ${err.localizedMessage ?: "بررسی اتصال اینترنت"}"
            }
            _downloadingTrackIds.update { it - track.id }
        }
    }

    fun createPlaylist(name: String, initialTrackId: String? = null) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val playlistId = repository.createPlaylist(trimmed)
            if (initialTrackId != null) {
                repository.addTrackToPlaylist(playlistId, initialTrackId)
            }
            _userMessage.value = "پلی‌لیست «$trimmed» ساخته شد"
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _userMessage.value = "پلی‌لیست حذف شد"
        }
    }

    fun addTrackToPlaylist(playlistId: Long, playlistName: String, track: TrackEntity) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track.id)
            _userMessage.value = "«${track.title}» به «$playlistName» اضافه شد"
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun attachRemoteAudioUrl(track: TrackEntity, url: String) {
        viewModelScope.launch {
            repository.updateRemoteAudioUrl(track.id, url)
            val updated = track.copy(
                remoteUrl = url.trim(),
                source = if (url.trim().startsWith("http", true)) "custom-stream" else "offline-bundled",
            )
            playbackController.updateTrackInQueue(updated)
            _userMessage.value = "منبع صوتی «${track.title}» بروزرسانی شد"
        }
    }

    fun attachLocalAudioFile(track: TrackEntity, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                repository.attachLocalAudioToTrack(track.id, uri)
            }.onSuccess { file ->
                val updated = track.copy(isDownloaded = true, localFilePath = file.absolutePath)
                playbackController.updateTrackInQueue(updated)
                _userMessage.value = "فایل صوتی دستگاه به «${track.title}» متصل شد"
            }.onFailure {
                _userMessage.value = "خواندن فایل صوتی ناموفق بود"
            }
        }
    }

    fun importStandaloneLocalAudio(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                repository.importStandaloneLocalTrack(uri)
            }.onSuccess { imported ->
                _userMessage.value = "«${imported.title}» به دانلودها اضافه شد"
            }.onFailure {
                _userMessage.value = "وارد کردن فایل صوتی ناموفق بود"
            }
        }
    }

    fun saveSyncUrls(catalogUrl: String, versionUrl: String) {
        RemoteSyncConfig.saveUrls(getApplication(), catalogUrl, versionUrl)
        _syncState.update {
            it.copy(
                catalogUrl = catalogUrl.trim(),
                versionUrl = versionUrl.trim(),
            )
        }
        _userMessage.value = "لینک‌های همگام‌سازی گیت‌هاب ذخیره شد"
    }

    fun syncRemoteCatalogNow() {
        val url = _syncState.value.catalogUrl
        if (url.isBlank()) {
            _userMessage.value = "ابتدا لینک آنلاین catalog.json را در تنظیمات وارد کنید"
            return
        }
        if (_syncState.value.isSyncingCatalog) return
        viewModelScope.launch {
            _syncState.update { it.copy(isSyncingCatalog = true) }
            runCatching {
                repository.syncCatalogFromUrl(url)
            }.onSuccess { result ->
                _userMessage.value = "کاتالوگ بروزرسانی شد: ${result.newlyAdded} آهنگ جدید، ${result.updatedExisting} مورد همگام‌سازی شد"
            }.onFailure { err ->
                _userMessage.value = "خطا در همگام‌سازی کاتالوگ: ${err.localizedMessage ?: "بررسی لینک و اینترنت"}"
            }
            _syncState.update { it.copy(isSyncingCatalog = false) }
        }
    }

    fun checkForUpdateNow() {
        val url = _syncState.value.versionUrl
        if (url.isBlank()) {
            _userMessage.value = "ابتدا لینک آنلاین version.json را در تنظیمات وارد کنید"
            return
        }
        if (_syncState.value.isCheckingUpdate) return
        viewModelScope.launch {
            _syncState.update { it.copy(isCheckingUpdate = true) }
            runCatching {
                repository.checkForAppUpdate(url)
            }.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _syncState.update { it.copy(availableUpdate = info) }
                } else {
                    _userMessage.value = "شما از آخرین نسخه دالاس موزیک (${info.latestVersionName}) استفاده می‌کنید"
                }
            }.onFailure { err ->
                _userMessage.value = "خطا در بررسی نسخه جدید: ${err.localizedMessage ?: "بررسی لینک و اینترنت"}"
            }
            _syncState.update { it.copy(isCheckingUpdate = false) }
        }
    }

    fun dismissUpdateDialog() {
        _syncState.update { it.copy(availableUpdate = null) }
    }

    fun consumeUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }
}
