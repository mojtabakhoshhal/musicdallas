package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.ArtistAccentPalette
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import kotlin.math.abs

@DrawableRes
fun resolveBundledCoverRes(cover: String): Int {
    return when {
        cover.contains("img_cover_poetic") -> R.drawable.img_cover_poetic
        cover.contains("img_cover_trap") -> R.drawable.img_cover_trap
        cover.contains("img_hero_persian_rap") -> R.drawable.img_hero_persian_rap
        else -> R.drawable.img_cover_underground
    }
}

fun artistAccentColor(artistId: String): Color {
    val index = abs(artistId.hashCode()) % ArtistAccentPalette.size
    return ArtistAccentPalette[index]
}

@Composable
fun TrackCoverArt(
    cover: String,
    artistId: String,
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    showCornerAccent: Boolean = true,
) {
    val accent = artistAccentColor(artistId)
    val isRemote = cover.startsWith("http://", true) || cover.startsWith("https://", true)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(ObsidianCard),
    ) {
        if (isRemote) {
            AsyncImage(
                model = cover,
                contentDescription = stringResource(R.string.cd_track_cover),
                placeholder = painterResource(resolveBundledCoverRes(cover)),
                error = painterResource(resolveBundledCoverRes(cover)),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(id = resolveBundledCoverRes(cover)),
                contentDescription = stringResource(R.string.cd_track_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.22f),
                                Color.Transparent,
                                ObsidianBlack.copy(alpha = 0.52f),
                            ),
                        ),
                    ),
            )
        }

        if (showCornerAccent) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .size(width = 18.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.copy(alpha = 0.9f)),
            )
        }
    }
}

@Composable
fun ArtistCircularAvatar(
    artistId: String,
    artistName: String,
    cover: String,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier,
) {
    val accent = artistAccentColor(artistId)
    val monogram = artistName.trim().take(2)
    val isRemote = cover.startsWith("http://", true) || cover.startsWith("https://", true)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        DallasGold,
                        accent,
                        DallasGold.copy(alpha = 0.45f),
                        DallasGold,
                    ),
                ),
                shape = CircleShape,
            )
            .padding(3.dp)
            .clip(CircleShape)
            .background(ObsidianCard),
        contentAlignment = Alignment.Center,
    ) {
        if (isRemote) {
            AsyncImage(
                model = cover,
                contentDescription = stringResource(R.string.cd_artist_avatar),
                placeholder = painterResource(resolveBundledCoverRes(cover)),
                error = painterResource(resolveBundledCoverRes(cover)),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(id = resolveBundledCoverRes(cover)),
                contentDescription = stringResource(R.string.cd_artist_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ObsidianBlack.copy(alpha = 0.35f),
                                ObsidianBlack.copy(alpha = 0.72f),
                            ),
                        ),
                    ),
            )
            Text(
                text = monogram,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.25f).sp,
                ),
                color = DallasGold,
            )
        }
    }
}
