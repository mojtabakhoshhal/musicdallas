package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlaylistWithTracks
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.ui.components.TrackRow
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    playlists: List<PlaylistWithTracks>,
    favoriteTracks: List<TrackEntity>,
    downloadedTracks: List<TrackEntity>,
    playbackState: PlaybackUiState,
    downloadingTrackIds: Set<String>,
    onCreatePlaylistClick: () -> Unit,
    onImportAudioClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onPlayPlaylist: (List<TrackEntity>) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabLabels = listOf(
        stringResource(R.string.library_tab_playlists),
        stringResource(R.string.library_tab_favorites),
        stringResource(R.string.library_tab_downloads),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(top = 12.dp)
            .testTag("library_screen"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.library_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(
                onClick = onImportAudioClick,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("library_import_audio_btn"),
            ) {
                Icon(
                    imageVector = Icons.Filled.AudioFile,
                    contentDescription = null,
                    tint = DallasGold,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.library_import_audio),
                    style = MaterialTheme.typography.labelMedium,
                    color = DallasGold,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabLabels.forEachIndexed { index, title ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) DallasGold else ObsidianCard)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp)
                        .testTag("library_tab_$index"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) ObsidianBlack else TextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "create_playlist_cta") {
                        Button(
                            onClick = onCreatePlaylistClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DallasGold,
                                contentColor = ObsidianBlack,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("create_playlist_btn"),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.library_create_playlist),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    if (playlists.isEmpty()) {
                        item(key = "empty_playlists") {
                            LibraryEmptyState(
                                icon = Icons.Filled.QueueMusic,
                                title = stringResource(R.string.library_empty_playlists_title),
                                subtitle = stringResource(R.string.library_empty_playlists_subtitle),
                                tag = "empty_playlists_state",
                            )
                        }
                    } else {
                        items(playlists, key = { it.playlist.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ObsidianCard)
                                    .border(1.dp, ObsidianOutline, RoundedCornerShape(16.dp))
                                    .clickable { onPlaylistClick(item.playlist.id) }
                                    .padding(12.dp)
                                    .testTag("playlist_card_${item.playlist.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = DallasGold,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.playlist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = stringResource(R.string.track_count_format, item.tracks.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                                if (item.tracks.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onPlayPlaylist(item.tracks) },
                                        modifier = Modifier.size(48.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(DallasGold),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = stringResource(R.string.cd_play),
                                                tint = ObsidianBlack,
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { onDeletePlaylist(item.playlist.id) },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = stringResource(R.string.action_delete),
                                        tint = ErrorCoral,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (favoriteTracks.isEmpty()) {
                    LibraryEmptyState(
                        icon = Icons.Filled.FavoriteBorder,
                        title = stringResource(R.string.library_empty_favorites_title),
                        subtitle = stringResource(R.string.library_empty_favorites_subtitle),
                        tag = "empty_favorites_state",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(favoriteTracks, key = { it.id }) { track ->
                            TrackRow(
                                track = track,
                                isCurrentTrack = playbackState.currentTrack?.id == track.id,
                                isPlaying = playbackState.isPlaying,
                                isDownloading = downloadingTrackIds.contains(track.id),
                                onPlayClick = { onPlayTrack(track, favoriteTracks) },
                                onFavoriteClick = { onFavoriteClick(track.id) },
                                onDownloadClick = { onDownloadClick(track) },
                                onAddToPlaylistClick = { onAddToPlaylistClick(track) },
                            )
                        }
                    }
                }
            }

            else -> {
                if (downloadedTracks.isEmpty()) {
                    LibraryEmptyState(
                        icon = Icons.Filled.DownloadDone,
                        title = stringResource(R.string.library_empty_downloads_title),
                        subtitle = stringResource(R.string.library_empty_downloads_subtitle),
                        tag = "empty_downloads_state",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(downloadedTracks, key = { it.id }) { track ->
                            TrackRow(
                                track = track,
                                isCurrentTrack = playbackState.currentTrack?.id == track.id,
                                isPlaying = playbackState.isPlaying,
                                isDownloading = downloadingTrackIds.contains(track.id),
                                onPlayClick = { onPlayTrack(track, downloadedTracks) },
                                onFavoriteClick = { onFavoriteClick(track.id) },
                                onDownloadClick = { onDownloadClick(track) },
                                onAddToPlaylistClick = { onAddToPlaylistClick(track) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistWithTracks: PlaylistWithTracks?,
    playbackState: PlaybackUiState,
    downloadingTrackIds: Set<String>,
    onBack: () -> Unit,
    onPlayShuffled: (List<TrackEntity>) -> Unit,
    onPlaySequential: (List<TrackEntity>) -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onRemoveFromPlaylist: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("playlist_detail_screen"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("playlist_back_btn"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlistWithTracks?.playlist?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.track_count_format, playlistWithTracks?.tracks?.size ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        val tracks = playlistWithTracks?.tracks.orEmpty()
        if (tracks.isEmpty()) {
            LibraryEmptyState(
                icon = Icons.Filled.QueueMusic,
                title = "این پلی‌لیست هنوز خالی است",
                subtitle = "از صفحه خانه یا هنرمندان، روی آیکون «افزودن به پلی‌لیست» کنار آهنگ‌ها بزنید.",
                tag = "playlist_detail_empty",
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { onPlayShuffled(tracks) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DallasGold,
                        contentColor = ObsidianBlack,
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.artist_shuffle_play), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onPlaySequential(tracks) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = DallasGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.artist_play_all), color = DallasGold)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tracks, key = { it.id }) { track ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrackRow(
                            track = track,
                            isCurrentTrack = playbackState.currentTrack?.id == track.id,
                            isPlaying = playbackState.isPlaying,
                            isDownloading = downloadingTrackIds.contains(track.id),
                            onPlayClick = { onPlayTrack(track, tracks) },
                            onFavoriteClick = { onFavoriteClick(track.id) },
                            onDownloadClick = { onDownloadClick(track) },
                            onAddToPlaylistClick = {
                                playlistWithTracks?.let { onRemoveFromPlaylist(it.playlist.id, track.id) }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DallasGold.copy(alpha = 0.6f),
            modifier = Modifier.size(54.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
