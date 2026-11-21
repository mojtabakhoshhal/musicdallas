package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.playback.RepeatModeUi
import com.example.ui.components.TrackCoverArt
import com.example.ui.theme.DallasGold
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.OfflineEmerald
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextSecondary

@Composable
fun FullPlayerScreen(
    playbackState: PlaybackUiState,
    isDownloading: Boolean,
    onClose: () -> Unit,
    onArtistClick: (String) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleDownload: (TrackEntity) -> Unit,
    onAddToPlaylist: (TrackEntity) -> Unit,
    onOpenAudioSourceDialog: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playbackState.currentTrack
    var showLyrics by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1F1908),
                        ObsidianBlack,
                        ObsidianBlack,
                    ),
                ),
            )
            .testTag("full_player_screen"),
    ) {
        if (track == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .testTag("player_empty_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = DallasGold.copy(alpha = 0.6f),
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    text = "آهنگی در حال پخش نیست",
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = DallasGold,
                    )
                }
            }
            return@Box
        }

        val sourceLabel = when {
            track.source == "local-device" -> stringResource(R.string.player_source_local)
            track.isDownloaded -> stringResource(R.string.player_source_downloaded)
            track.remoteUrl.startsWith("http", ignoreCase = true) -> stringResource(R.string.player_source_stream)
            else -> stringResource(R.string.player_source_offline_preview)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_close_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.player_now_playing),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Text(
                        text = sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = DallasGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenAudioSourceDialog(track) }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }

                IconButton(
                    onClick = { onAddToPlaylist(track) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_add_playlist_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistAdd,
                        contentDescription = stringResource(R.string.cd_add_playlist),
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Cover Artwork
            TrackCoverArt(
                cover = track.cover,
                artistId = track.artistId,
                title = track.title,
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .aspectRatio(1f)
                    .border(1.dp, DallasGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Track Info
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.titleMedium,
                color = DallasGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onClose()
                        onArtistClick(track.artistId)
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
            if (track.album.isNotBlank()) {
                Text(
                    text = track.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seek bar + Elapsed / Total Time (LTR numeric progression for media scrubber)
            val durationMs = playbackState.durationMs.coerceAtLeast(1_000L)
            var isScrubbing by remember { mutableStateOf(false) }
            var scrubFraction by remember { mutableStateOf(0f) }
            val displayedFraction = if (isScrubbing) scrubFraction else playbackState.progress
            val displayedPositionMs = if (isScrubbing) (scrubFraction * durationMs).toLong() else playbackState.positionMs

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Slider(
                    value = displayedFraction,
                    onValueChange = { fraction ->
                        isScrubbing = true
                        scrubFraction = fraction
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        onSeekTo((scrubFraction * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = DallasGold,
                        activeTrackColor = DallasGold,
                        inactiveTrackColor = ObsidianOutline,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seek_bar"),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatTimeMs(displayedPositionMs),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = OutfitFontFamily),
                        color = TextSecondary,
                    )
                    Text(
                        text = formatTimeMs(durationMs),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = OutfitFontFamily),
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transport Controls (LTR media control order: Shuffle, Prev, Play/Pause, Next, Repeat)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_shuffle_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = stringResource(R.string.cd_shuffle),
                            tint = if (playbackState.shuffleEnabled) DallasGold else TextSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("player_previous_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = stringResource(R.string.cd_previous),
                            tint = Color.White,
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DallasGold)
                            .testTag("player_play_pause_btn"),
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(
                                if (playbackState.isPlaying) R.string.cd_pause else R.string.cd_play,
                            ),
                            tint = ObsidianBlack,
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("player_next_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = stringResource(R.string.cd_next),
                            tint = Color.White,
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    IconButton(
                        onClick = onCycleRepeat,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_repeat_btn"),
                    ) {
                        Icon(
                            imageVector = if (playbackState.repeatMode == RepeatModeUi.ONE) {
                                Icons.Filled.RepeatOne
                            } else {
                                Icons.Filled.Repeat
                            },
                            contentDescription = stringResource(R.string.cd_repeat),
                            tint = if (playbackState.repeatMode != RepeatModeUi.OFF) DallasGold else TextSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Utility Strip: Favorite, Download, Audio Source, Lyrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianOutline, RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onToggleFavorite(track.id) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_favorite_btn"),
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_favorite),
                        tint = if (track.isFavorite) HeartCrimson else TextSecondary,
                    )
                }

                IconButton(
                    onClick = { onToggleDownload(track) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_download_btn"),
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            color = DallasGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = if (track.isDownloaded) Icons.Filled.CheckCircle else Icons.Filled.Download,
                            contentDescription = stringResource(R.string.cd_download),
                            tint = if (track.isDownloaded) OfflineEmerald else TextSecondary,
                        )
                    }
                }

                IconButton(
                    onClick = { onOpenAudioSourceDialog(track) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_source_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = stringResource(R.string.action_attach_url),
                        tint = TextSecondary,
                    )
                }

                IconButton(
                    onClick = { showLyrics = !showLyrics },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_lyrics_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lyrics,
                        contentDescription = stringResource(R.string.cd_lyrics),
                        tint = if (showLyrics) DallasGold else TextSecondary,
                    )
                }
            }

            AnimatedVisibility(visible = showLyrics) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ObsidianElevated)
                        .border(1.dp, DallasGold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .padding(18.dp)
                        .testTag("player_lyrics_panel"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.player_lyrics),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DallasGold,
                    )
                    Text(
                        text = track.lyrics.ifBlank { stringResource(R.string.player_no_lyrics) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
