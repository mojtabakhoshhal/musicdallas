package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ArtistSummary
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.ui.components.ArtistCircularAvatar
import com.example.ui.components.TrackCoverArt
import com.example.ui.components.TrackRow
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    artists: List<ArtistSummary>,
    newTracks: List<TrackEntity>,
    allTracks: List<TrackEntity>,
    playbackState: PlaybackUiState,
    downloadingTrackIds: Set<String>,
    onArtistClick: (String) -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onShuffleAll: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    onOpenDevelopers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Brand Header
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dallas_music_logo),
                        contentDescription = stringResource(R.string.cd_app_logo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DallasGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.app_brand_persian),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "DALLAS MUSIC",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = OutfitFontFamily,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = DallasGold,
                        )
                    }
                }

                IconButton(
                    onClick = onOpenDevelopers,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_developers_btn"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(R.string.developers_title),
                        tint = DallasGold,
                    )
                }
            }
        }

        // Featured Editorial Hero Banner
        item(key = "hero") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(190.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DallasGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_persian_rap),
                    contentDescription = stringResource(R.string.home_featured_title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBlack.copy(alpha = 0.2f),
                                    ObsidianBlack.copy(alpha = 0.65f),
                                    ObsidianBlack.copy(alpha = 0.94f),
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_featured_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = DallasGold,
                    )
                    Text(
                        text = stringResource(R.string.home_featured_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = if (artists.isNotEmpty()) {
                                "${stringResource(R.string.artists_count_format, artists.size)} • ${stringResource(R.string.track_count_format, allTracks.size)}"
                            } else {
                                stringResource(R.string.home_featured_subtitle)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Button(
                            onClick = onShuffleAll,
                            enabled = allTracks.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DallasGold,
                                contentColor = ObsidianBlack,
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("home_shuffle_all_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_shuffle_all),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        if (allTracks.isEmpty()) {
            item(key = "empty_catalog") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .testTag("home_empty_state"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicOff,
                        contentDescription = null,
                        tint = DallasGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(52.dp),
                    )
                    Text(
                        text = "کاتالوگ در حال بارگذاری است",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "آهنگ‌های آرشیو دالاس موزیک به‌صورت خودکار وارد پایگاه داده می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            // Circular Artist Rail
            item(key = "artists_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.home_artists_section),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.artists_count_format, artists.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = DallasGold,
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.testTag("artist_rail"),
                    ) {
                        items(artists, key = { it.artistId }) { artist ->
                            Column(
                                modifier = Modifier
                                    .width(86.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onArtistClick(artist.artistId) }
                                    .padding(vertical = 4.dp)
                                    .testTag("artist_chip_${artist.artistId}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                ArtistCircularAvatar(
                                    artistId = artist.artistId,
                                    artistName = artist.artistName,
                                    cover = artist.cover,
                                    size = 74.dp,
                                )
                                Text(
                                    text = artist.artistName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = stringResource(R.string.track_count_format, artist.trackCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            // New Releases Horizontal Shelf ("تازه‌ها")
            if (newTracks.isNotEmpty()) {
                item(key = "new_releases_section") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.home_new_releases),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.testTag("new_releases_shelf"),
                        ) {
                            items(newTracks, key = { it.id }) { track ->
                                Column(
                                    modifier = Modifier
                                        .width(152.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ObsidianCard)
                                        .border(1.dp, ObsidianOutline.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                        .clickable { onPlayTrack(track, newTracks) }
                                        .padding(10.dp)
                                        .testTag("new_track_card_${track.id}"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(modifier = Modifier.size(132.dp)) {
                                        TrackCoverArt(
                                            cover = track.cover,
                                            artistId = track.artistId,
                                            title = track.title,
                                            cornerRadius = 12.dp,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(DallasGold),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = stringResource(R.string.cd_play),
                                                tint = ObsidianBlack,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // All Tracks Header ("همه آهنگ‌ها")
            item(key = "all_tracks_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_all_tracks),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.track_count_format, allTracks.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }

            items(allTracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    isCurrentTrack = playbackState.currentTrack?.id == track.id,
                    isPlaying = playbackState.isPlaying,
                    isDownloading = downloadingTrackIds.contains(track.id),
                    onPlayClick = { onPlayTrack(track, allTracks) },
                    onFavoriteClick = { onFavoriteClick(track.id) },
                    onDownloadClick = { onDownloadClick(track) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(track) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
