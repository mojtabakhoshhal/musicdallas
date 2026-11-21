package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.playback.PlaybackUiState
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.TextSecondary

@Composable
fun MiniPlayerBar(
    playbackState: PlaybackUiState,
    onOpenFullPlayer: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playbackState.currentTrack ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianElevated.copy(alpha = 0.96f))
            .border(1.dp, ObsidianOutline, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenFullPlayer)
            .testTag("mini_player_bar"),
    ) {
        LinearProgressIndicator(
            progress = { playbackState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp),
            color = DallasGold,
            trackColor = ObsidianOutline,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackCoverArt(
                cover = track.cover,
                artistId = track.artistId,
                title = track.title,
                cornerRadius = 10.dp,
                showCornerAccent = false,
                modifier = Modifier.size(44.dp),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
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

            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("mini_player_play_pause"),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DallasGold),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(
                            if (playbackState.isPlaying) R.string.cd_pause else R.string.cd_play,
                        ),
                        tint = ObsidianBlack,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("mini_player_next"),
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = stringResource(R.string.cd_next),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
