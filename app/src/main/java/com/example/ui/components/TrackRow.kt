package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TrackEntity
import com.example.ui.theme.DallasGold
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.OfflineEmerald
import com.example.ui.theme.TextSecondary

@Composable
fun TrackRow(
    track: TrackEntity,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitleOverride: String? = null,
) {
    val containerColor = if (isCurrentTrack) {
        ObsidianElevated
    } else {
        ObsidianCard.copy(alpha = 0.65f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            TrackCoverArt(
                cover = track.cover,
                artistId = track.artistId,
                title = track.title,
                cornerRadius = 10.dp,
                showCornerAccent = false,
                modifier = Modifier.size(52.dp),
            )
            if (isCurrentTrack) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = stringResource(R.string.player_now_playing),
                        tint = DallasGold,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                ),
                color = if (isCurrentTrack) DallasGold else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (track.isDownloaded) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.player_source_downloaded),
                        tint = OfflineEmerald,
                        modifier = Modifier.size(13.dp),
                    )
                }
                Text(
                    text = subtitleOverride ?: buildString {
                        append(track.artist)
                        if (track.album.isNotBlank()) {
                            append(" • ")
                            append(track.album)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .size(44.dp)
                .testTag("favorite_btn_${track.id}"),
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.cd_favorite),
                tint = if (track.isFavorite) HeartCrimson else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        IconButton(
            onClick = onDownloadClick,
            modifier = Modifier
                .size(44.dp)
                .testTag("download_btn_${track.id}"),
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    color = DallasGold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Icon(
                    imageVector = if (track.isDownloaded) Icons.Filled.CheckCircle else Icons.Filled.Download,
                    contentDescription = stringResource(R.string.cd_download),
                    tint = if (track.isDownloaded) OfflineEmerald else TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        IconButton(
            onClick = onAddToPlaylistClick,
            modifier = Modifier
                .size(44.dp)
                .testTag("add_playlist_btn_${track.id}"),
        ) {
            Icon(
                imageVector = Icons.Filled.PlaylistAdd,
                contentDescription = stringResource(R.string.cd_add_playlist),
                tint = TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
