package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.TrackEntity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class RepeatModeUi {
    OFF,
    ALL,
    ONE,
}

data class PlaybackUiState(
    val currentTrack: TrackEntity? = null,
    val queue: List<TrackEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatModeUi = RepeatModeUi.OFF,
    val playbackError: String? = null,
) {
    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

class PlaybackController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null
    private var pendingQueue: Pair<List<TrackEntity>, Int>? = null
    private var pendingShuffle: Boolean = false

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncFromPlayer()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncFromPlayer()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val uiMode = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatModeUi.ONE
                Player.REPEAT_MODE_ALL -> RepeatModeUi.ALL
                else -> RepeatModeUi.OFF
            }
            _state.update { it.copy(repeatMode = uiMode) }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update {
                it.copy(
                    isPlaying = false,
                    playbackError = "خطا در پخش صدا: ${error.localizedMessage ?: "منبع در دسترس نیست"}",
                )
            }
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(
            context.applicationContext,
            ComponentName(context.applicationContext, DallasPlaybackService::class.java),
        )
        val future = MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }.onSuccess { controller ->
                    mediaController = controller
                    controller.addListener(playerListener)
                    startProgressTicker()
                    pendingQueue?.let { (tracks, startIndex) ->
                        pendingQueue = null
                        applyQueueToController(controller, tracks, startIndex, pendingShuffle)
                    } ?: syncFromPlayer()
                }
            },
            ContextCompat.getMainExecutor(context.applicationContext),
        )
    }

    fun playQueue(tracks: List<TrackEntity>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val orderedTracks = if (shuffle) tracks.shuffled() else tracks
        val targetIndex = if (shuffle) 0 else safeIndex

        _state.update {
            it.copy(
                queue = orderedTracks,
                currentIndex = targetIndex,
                currentTrack = orderedTracks[targetIndex],
                positionMs = 0L,
                durationMs = orderedTracks[targetIndex].durationMs,
                shuffleEnabled = shuffle,
                playbackError = null,
            )
        }

        val controller = mediaController
        if (controller != null) {
            applyQueueToController(controller, orderedTracks, targetIndex, shuffle)
        } else {
            pendingQueue = orderedTracks to targetIndex
            pendingShuffle = shuffle
            connect()
        }
    }

    private fun applyQueueToController(
        controller: MediaController,
        tracks: List<TrackEntity>,
        startIndex: Int,
        shuffle: Boolean,
    ) {
        val mediaItems = tracks.map { it.toMediaItem() }
        controller.shuffleModeEnabled = shuffle
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        syncFromPlayer()
    }

    fun updateTrackInQueue(updatedTrack: TrackEntity) {
        _state.update { current ->
            val updatedQueue = current.queue.map { if (it.id == updatedTrack.id) updatedTrack else it }
            val updatedCurrent = if (current.currentTrack?.id == updatedTrack.id) updatedTrack else current.currentTrack
            current.copy(queue = updatedQueue, currentTrack = updatedCurrent)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0L)
            }
            controller.play()
        }
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
        } else if (controller.mediaItemCount > 0) {
            controller.seekTo(0, 0L)
            controller.play()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3_000L) {
            controller.seekTo(0L)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            controller.play()
        } else {
            controller.seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController
        val bounded = positionMs.coerceAtLeast(0L)
        _state.update { it.copy(positionMs = bounded) }
        controller?.seekTo(bounded)
    }

    fun toggleShuffle() {
        val controller = mediaController
        val nextValue = !_state.value.shuffleEnabled
        _state.update { it.copy(shuffleEnabled = nextValue) }
        if (controller != null) {
            controller.shuffleModeEnabled = nextValue
        }
    }

    fun cycleRepeatMode() {
        val nextUi = when (_state.value.repeatMode) {
            RepeatModeUi.OFF -> RepeatModeUi.ALL
            RepeatModeUi.ALL -> RepeatModeUi.ONE
            RepeatModeUi.ONE -> RepeatModeUi.OFF
        }
        val playerMode = when (nextUi) {
            RepeatModeUi.OFF -> Player.REPEAT_MODE_OFF
            RepeatModeUi.ALL -> Player.REPEAT_MODE_ALL
            RepeatModeUi.ONE -> Player.REPEAT_MODE_ONE
        }
        _state.update { it.copy(repeatMode = nextUi) }
        mediaController?.repeatMode = playerMode
    }

    fun clearError() {
        _state.update { it.copy(playbackError = null) }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null) {
                    val dur = controller.duration.takeIf { it > 0L }
                        ?: _state.value.currentTrack?.durationMs
                        ?: 0L
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    _state.update {
                        it.copy(
                            positionMs = pos,
                            durationMs = dur,
                            isPlaying = controller.isPlaying,
                        )
                    }
                }
                delay(350L)
            }
        }
    }

    private fun syncFromPlayer() {
        val controller = mediaController ?: return
        val index = controller.currentMediaItemIndex
        val currentQueue = _state.value.queue
        val track = currentQueue.getOrNull(index) ?: _state.value.currentTrack
        val duration = controller.duration.takeIf { it > 0L } ?: track?.durationMs ?: 0L
        _state.update {
            it.copy(
                currentTrack = track,
                currentIndex = index,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
            )
        }
    }

    fun release() {
        progressJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }

    private fun TrackEntity.toMediaItem(): MediaItem {
        val resolvedUri = resolvePlaybackUri(this)
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album.ifBlank { "Dallas Music" })
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(resolvedUri)
            .setMediaMetadata(metadata)
            .build()
    }

    companion object {
        fun resolvePlaybackUri(track: TrackEntity): Uri {
            val localPath = track.localFilePath
            if (track.isDownloaded && !localPath.isNullOrBlank()) {
                val file = File(localPath)
                if (file.exists() && file.length() > 0L) {
                    return Uri.fromFile(file)
                }
            }
            val remote = track.remoteUrl.trim()
            if (remote.startsWith("https://", true) || remote.startsWith("http://", true)) {
                return Uri.parse(remote)
            }
            val asset = track.previewAsset.ifBlank { "audio/beat_1.wav" }
            return Uri.parse("asset:///$asset")
        }
    }
}
