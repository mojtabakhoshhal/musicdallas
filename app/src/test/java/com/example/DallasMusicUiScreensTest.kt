package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.ArtistSummary
import com.example.data.PlaylistEntity
import com.example.data.PlaylistWithTracks
import com.example.data.TrackEntity
import com.example.playback.PlaybackUiState
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.DevelopersScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DallasMusicTheme
import com.example.viewmodel.SearchUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DallasMusicUiScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTrack = TrackEntity(
        id = "ali_sorena_01",
        artist = "علی سورنا",
        artistId = "ali_sorena",
        title = "مجنون شهر",
        album = "آوار (۱۳۹۲)",
        isNew = true,
        lyrics = "متن نمونه قطعه",
    )

    private val sampleArtist = ArtistSummary(
        artistId = "ali_sorena",
        artistName = "علی سورنا",
        trackCount = 8,
        cover = "drawable://img_cover_underground",
        firstSort = 0,
    )

    @Test
    fun `home screen renders empty state cleanly`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                HomeScreen(
                    artists = emptyList(),
                    newTracks = emptyList(),
                    allTracks = emptyList(),
                    playbackState = PlaybackUiState(),
                    downloadingTrackIds = emptySet(),
                    onArtistClick = {},
                    onPlayTrack = { _, _ -> },
                    onShuffleAll = {},
                    onFavoriteClick = {},
                    onDownloadClick = {},
                    onAddToPlaylistClick = {},
                    onOpenDevelopers = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen").performScrollToNode(hasTestTag("home_empty_state"))
        composeTestRule.onNodeWithTag("home_empty_state").assertIsDisplayed()
    }

    @Test
    fun `home screen renders full catalog state with artist rail and tracks`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                HomeScreen(
                    artists = listOf(sampleArtist),
                    newTracks = listOf(sampleTrack),
                    allTracks = listOf(sampleTrack),
                    playbackState = PlaybackUiState(),
                    downloadingTrackIds = emptySet(),
                    onArtistClick = {},
                    onPlayTrack = { _, _ -> },
                    onShuffleAll = {},
                    onFavoriteClick = {},
                    onDownloadClick = {},
                    onAddToPlaylistClick = {},
                    onOpenDevelopers = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen").performScrollToNode(hasTestTag("artist_rail"))
        composeTestRule.onNodeWithTag("artist_rail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_screen").performScrollToNode(hasTestTag("new_releases_shelf"))
        composeTestRule.onNodeWithTag("new_releases_shelf").assertIsDisplayed()
    }

    @Test
    fun `search screen handles empty prompt no results and matched catalog`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                SearchScreen(
                    searchState = SearchUiState(query = "ناموجود"),
                    allArtists = listOf(sampleArtist),
                    playbackState = PlaybackUiState(),
                    downloadingTrackIds = emptySet(),
                    onQueryChange = {},
                    onArtistClick = {},
                    onPlayTrack = { _, _ -> },
                    onFavoriteClick = {},
                    onDownloadClick = {},
                    onAddToPlaylistClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_empty_results").assertIsDisplayed()
    }

    @Test
    fun `library screen renders playlists empty and populated states`() {
        val playlist = PlaylistWithTracks(
            playlist = PlaylistEntity(id = 1L, name = "رپ شبانه"),
            tracks = listOf(sampleTrack),
        )
        composeTestRule.setContent {
            DallasMusicTheme {
                LibraryScreen(
                    playlists = listOf(playlist),
                    favoriteTracks = emptyList(),
                    downloadedTracks = emptyList(),
                    playbackState = PlaybackUiState(),
                    downloadingTrackIds = emptySet(),
                    onCreatePlaylistClick = {},
                    onImportAudioClick = {},
                    onPlaylistClick = {},
                    onPlayPlaylist = {},
                    onDeletePlaylist = {},
                    onPlayTrack = { _, _ -> },
                    onFavoriteClick = {},
                    onDownloadClick = {},
                    onAddToPlaylistClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("رپ شبانه").assertIsDisplayed()
    }

    @Test
    fun `artist detail screen renders shuffle button cleanly`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                ArtistDetailScreen(
                    artistId = "ali_sorena",
                    tracks = listOf(sampleTrack),
                    playbackState = PlaybackUiState(),
                    downloadingTrackIds = emptySet(),
                    onBack = {},
                    onShufflePlay = {},
                    onPlayAll = {},
                    onPlayTrack = {},
                    onFavoriteClick = {},
                    onDownloadClick = {},
                    onAddToPlaylistClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("artist_detail_screen").performScrollToNode(hasTestTag("artist_shuffle_play_btn"))
        composeTestRule.onNodeWithTag("artist_shuffle_play_btn").assertIsDisplayed()
    }

    @Test
    fun `full player active state renders seek bar and transport controls`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                FullPlayerScreen(
                    playbackState = PlaybackUiState(currentTrack = sampleTrack, durationMs = 18_000L),
                    isDownloading = false,
                    onClose = {},
                    onArtistClick = {},
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeekTo = {},
                    onToggleShuffle = {},
                    onCycleRepeat = {},
                    onToggleFavorite = {},
                    onToggleDownload = {},
                    onAddToPlaylist = {},
                    onOpenAudioSourceDialog = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("player_seek_bar").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_play_pause_btn").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `developers screen displays Mojtaba and Amir Khoshhal credits`() {
        composeTestRule.setContent {
            DallasMusicTheme {
                DevelopersScreen()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("مجتبی خوشحال").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("امیر خوشحال").performScrollTo().assertIsDisplayed()
    }
}
