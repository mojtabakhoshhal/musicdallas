package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ArtistSummary
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.ui.components.ArtistCircularAvatar
import com.example.ui.components.TrackRow
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SearchUiState

@Composable
fun SearchScreen(
    searchState: SearchUiState,
    allArtists: List<ArtistSummary>,
    playbackState: PlaybackUiState,
    downloadingTrackIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(top = 12.dp)
            .testTag("search_screen"),
    ) {
        Text(
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchState.query,
            onValueChange = onQueryChange,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    color = TextSecondary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = DallasGold,
                )
            },
            trailingIcon = {
                if (searchState.query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("search_clear_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.search_clear),
                            tint = TextSecondary,
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard,
                focusedBorderColor = DallasGold,
                unfocusedBorderColor = ObsidianOutline,
                cursorColor = DallasGold,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_input"),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchState.query.isBlank()) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "prompt") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .testTag("search_prompt_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = DallasGold.copy(alpha = 0.7f),
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = stringResource(R.string.search_prompt_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.search_prompt_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (allArtists.isNotEmpty()) {
                    item(key = "quick_artists") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.search_artists_header),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(allArtists, key = { it.artistId }) { artist ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(ObsidianCard)
                                            .clickable { onArtistClick(artist.artistId) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        ArtistCircularAvatar(
                                            artistId = artist.artistId,
                                            artistName = artist.artistName,
                                            cover = artist.cover,
                                            size = 34.dp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = artist.artistName,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (searchState.matchingArtists.isEmpty() && searchState.matchingTracks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp)
                    .testTag("search_empty_results"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SearchOff,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(52.dp),
                )
                Text(
                    text = stringResource(R.string.search_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.search_empty_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (searchState.matchingArtists.isNotEmpty()) {
                    item(key = "matched_artists_header") {
                        Text(
                            text = stringResource(R.string.search_artists_header),
                            style = MaterialTheme.typography.titleMedium,
                            color = DallasGold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    item(key = "matched_artists_rail") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(searchState.matchingArtists, key = { it.artistId }) { artist ->
                                Column(
                                    modifier = Modifier
                                        .width(86.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onArtistClick(artist.artistId) }
                                        .padding(vertical = 4.dp)
                                        .testTag("search_artist_result_${artist.artistId}"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    ArtistCircularAvatar(
                                        artistId = artist.artistId,
                                        artistName = artist.artistName,
                                        cover = artist.cover,
                                        size = 66.dp,
                                    )
                                    Text(
                                        text = artist.artistName,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                if (searchState.matchingTracks.isNotEmpty()) {
                    item(key = "matched_tracks_header") {
                        Text(
                            text = stringResource(R.string.search_tracks_header),
                            style = MaterialTheme.typography.titleMedium,
                            color = DallasGold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(searchState.matchingTracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            isCurrentTrack = playbackState.currentTrack?.id == track.id,
                            isPlaying = playbackState.isPlaying,
                            isDownloading = downloadingTrackIds.contains(track.id),
                            onPlayClick = { onPlayTrack(track, searchState.matchingTracks) },
                            onFavoriteClick = { onFavoriteClick(track.id) },
                            onDownloadClick = { onDownloadClick(track) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(track) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
