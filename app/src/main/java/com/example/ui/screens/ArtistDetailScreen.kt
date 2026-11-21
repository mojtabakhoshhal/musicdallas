package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.ui.components.ArtistCircularAvatar
import com.example.ui.components.TrackRow
import com.example.ui.components.resolveBundledCoverRes
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextSecondary

@Composable
fun ArtistDetailScreen(
    artistId: String,
    tracks: List<TrackEntity>,
    playbackState: PlaybackUiState,
    downloadingTrackIds: Set<String>,
    onBack: () -> Unit,
    onShufflePlay: () -> Unit,
    onPlayAll: () -> Unit,
    onPlayTrack: (TrackEntity) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstTrack = tracks.firstOrNull()
    val artistName = firstTrack?.artist ?: stringResource(R.string.artist_not_found)
    val cover = firstTrack?.cover ?: "drawable://img_cover_underground"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("artist_detail_screen"),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "artist_hero") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(276.dp),
            ) {
                Image(
                    painter = painterResource(id = resolveBundledCoverRes(cover)),
                    contentDescription = artistName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBlack.copy(alpha = 0.35f),
                                    ObsidianBlack.copy(alpha = 0.72f),
                                    ObsidianBlack,
                                ),
                            ),
                        ),
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ObsidianBlack.copy(alpha = 0.6f))
                        .testTag("artist_back_btn"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White,
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ArtistCircularAvatar(
                        artistId = artistId,
                        artistName = artistName,
                        cover = cover,
                        size = 96.dp,
                    )
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.track_count_format, tracks.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = DallasGold,
                    )
                }
            }
        }

        item(key = "artist_actions") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onShufflePlay,
                    enabled = tracks.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DallasGold,
                        contentColor = ObsidianBlack,
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("artist_shuffle_play_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.artist_shuffle_play),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                OutlinedButton(
                    onClick = onPlayAll,
                    enabled = tracks.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("artist_play_all_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = DallasGold,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.artist_play_all),
                        color = DallasGold,
                    )
                }
            }
        }

        if (tracks.isEmpty()) {
            item(key = "artist_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .testTag("artist_empty_state"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.artist_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
            }
        } else {
            item(key = "discography_title") {
                Text(
                    text = stringResource(R.string.artist_discography_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            items(tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isCurrentTrack = playbackState.currentTrack?.id == track.id,
                    isPlaying = playbackState.isPlaying,
                    isDownloading = downloadingTrackIds.contains(track.id),
                    onPlayClick = { onPlayTrack(track) },
                    onFavoriteClick = { onFavoriteClick(track.id) },
                    onDownloadClick = { onDownloadClick(track) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(track) },
                    subtitleOverride = track.album.ifBlank { track.artist },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
