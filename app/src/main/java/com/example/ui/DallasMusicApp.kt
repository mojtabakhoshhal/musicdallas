package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.TrackEntity
import com.example.ui.components.AddToPlaylistSheet
import com.example.ui.components.AudioSourceDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SyncSettingsDialog
import com.example.ui.components.UpdateAvailableDialog
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.DevelopersScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DallasGold
import com.example.ui.theme.DallasGoldContainer
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DallasMusicViewModel

object DallasRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_SEARCH = "search"
    const val ROUTE_LIBRARY = "library"
    const val ROUTE_DEVELOPERS = "developers"
    const val ROUTE_PLAYER = "player"
    const val ROUTE_ARTIST = "artist/{artistId}"
    const val ROUTE_PLAYLIST = "playlist/{playlistId}"

    fun artistRoute(artistId: String): String = "artist/$artistId"
    fun playlistRoute(playlistId: Long): String = "playlist/$playlistId"
}

private data class BottomTabItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

@Composable
fun DallasMusicApp(
    viewModel: DallasMusicViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: DallasRoutes.ROUTE_HOME

    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val newTracks by viewModel.newTracks.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val downloadedTracks by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val downloadingTrackIds by viewModel.downloadingTrackIds.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSyncSettingsDialog by remember { mutableStateOf(false) }
    var trackForPlaylistSheet by remember { mutableStateOf<TrackEntity?>(null) }
    var trackForAudioSourceDialog by remember { mutableStateOf<TrackEntity?>(null) }
    var trackAwaitingLocalFile by remember { mutableStateOf<TrackEntity?>(null) }
    var notificationPermissionRequested by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ -> /* Media playback works even if notifications are denied */ }

    val localAudioAttachLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val target = trackAwaitingLocalFile
        trackAwaitingLocalFile = null
        if (uri != null && target != null) {
            viewModel.attachLocalAudioFile(target, uri)
        }
    }

    val standaloneImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importStandaloneLocalAudio(uri)
        }
    }

    LaunchedEffect(userMessage) {
        val message = userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeUserMessage()
    }

    LaunchedEffect(playbackState.playbackError) {
        val err = playbackState.playbackError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearPlaybackError()
    }

    LaunchedEffect(playbackState.isPlaying) {
        if (playbackState.isPlaying && !notificationPermissionRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionRequested = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val tabs = remember {
        listOf(
            BottomTabItem(
                route = DallasRoutes.ROUTE_HOME,
                labelRes = R.string.nav_home,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_tab_home",
            ),
            BottomTabItem(
                route = DallasRoutes.ROUTE_SEARCH,
                labelRes = R.string.nav_search,
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                testTag = "nav_tab_search",
            ),
            BottomTabItem(
                route = DallasRoutes.ROUTE_LIBRARY,
                labelRes = R.string.nav_library,
                selectedIcon = Icons.Filled.LibraryMusic,
                unselectedIcon = Icons.Outlined.LibraryMusic,
                testTag = "nav_tab_library",
            ),
            BottomTabItem(
                route = DallasRoutes.ROUTE_DEVELOPERS,
                labelRes = R.string.nav_developers,
                selectedIcon = Icons.Filled.Code,
                unselectedIcon = Icons.Outlined.Code,
                testTag = "nav_tab_developers",
            ),
        )
    }

    val isFullPlayerRoute = currentRoute == DallasRoutes.ROUTE_PLAYER

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
    ) {
        val useRail = maxWidth >= 720.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && !isFullPlayerRoute) {
                NavigationRail(
                    containerColor = ObsidianSurface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding(),
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes),
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DallasGold,
                                selectedTextColor = DallasGold,
                                indicatorColor = DallasGoldContainer,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                            ),
                            modifier = Modifier.testTag(tab.testTag),
                        )
                    }
                }
            }

            Scaffold(
                containerColor = ObsidianBlack,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!isFullPlayerRoute) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AnimatedVisibility(
                                visible = playbackState.currentTrack != null,
                                enter = slideInVertically { it },
                                exit = slideOutVertically { it },
                            ) {
                                MiniPlayerBar(
                                    playbackState = playbackState,
                                    onOpenFullPlayer = {
                                        navController.navigate(DallasRoutes.ROUTE_PLAYER) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onPlayPauseClick = viewModel::togglePlayPause,
                                    onNextClick = viewModel::skipNext,
                                )
                            }

                            if (!useRail) {
                                NavigationBar(
                                    containerColor = ObsidianSurface,
                                    windowInsets = WindowInsets.navigationBars,
                                ) {
                                    tabs.forEach { tab ->
                                        val selected = currentRoute == tab.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateToTab(tab.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = stringResource(tab.labelRes),
                                                )
                                            },
                                            label = { Text(stringResource(tab.labelRes)) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = DallasGold,
                                                selectedTextColor = DallasGold,
                                                indicatorColor = DallasGoldContainer,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary,
                                            ),
                                            modifier = Modifier.testTag(tab.testTag),
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = DallasRoutes.ROUTE_HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 900.dp),
                    ) {
                        composable(DallasRoutes.ROUTE_HOME) {
                            HomeScreen(
                                artists = artists,
                                newTracks = newTracks,
                                allTracks = allTracks,
                                playbackState = playbackState,
                                downloadingTrackIds = downloadingTrackIds,
                                onArtistClick = { artistId ->
                                    navController.navigate(DallasRoutes.artistRoute(artistId))
                                },
                                onPlayTrack = viewModel::playTrack,
                                onShuffleAll = { viewModel.playQueueShuffled(allTracks) },
                                onFavoriteClick = viewModel::toggleFavorite,
                                onDownloadClick = viewModel::toggleDownload,
                                onAddToPlaylistClick = { trackForPlaylistSheet = it },
                                onOpenDevelopers = { navigateToTab(DallasRoutes.ROUTE_DEVELOPERS) },
                            )
                        }

                        composable(DallasRoutes.ROUTE_SEARCH) {
                            SearchScreen(
                                searchState = searchState,
                                allArtists = artists,
                                playbackState = playbackState,
                                downloadingTrackIds = downloadingTrackIds,
                                onQueryChange = viewModel::updateSearchQuery,
                                onArtistClick = { artistId ->
                                    navController.navigate(DallasRoutes.artistRoute(artistId))
                                },
                                onPlayTrack = viewModel::playTrack,
                                onFavoriteClick = viewModel::toggleFavorite,
                                onDownloadClick = viewModel::toggleDownload,
                                onAddToPlaylistClick = { trackForPlaylistSheet = it },
                            )
                        }

                        composable(DallasRoutes.ROUTE_LIBRARY) {
                            LibraryScreen(
                                playlists = playlists,
                                favoriteTracks = favoriteTracks,
                                downloadedTracks = downloadedTracks,
                                playbackState = playbackState,
                                downloadingTrackIds = downloadingTrackIds,
                                onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                                onImportAudioClick = {
                                    standaloneImportLauncher.launch(arrayOf("audio/*"))
                                },
                                onPlaylistClick = { playlistId ->
                                    navController.navigate(DallasRoutes.playlistRoute(playlistId))
                                },
                                onPlayPlaylist = viewModel::playQueueSequential,
                                onDeletePlaylist = viewModel::deletePlaylist,
                                onPlayTrack = viewModel::playTrack,
                                onFavoriteClick = viewModel::toggleFavorite,
                                onDownloadClick = viewModel::toggleDownload,
                                onAddToPlaylistClick = { trackForPlaylistSheet = it },
                            )
                        }

                        composable(DallasRoutes.ROUTE_DEVELOPERS) {
                            DevelopersScreen(
                                syncState = syncState,
                                onConfigureSyncUrls = { showSyncSettingsDialog = true },
                                onSyncCatalogNow = {
                                    if (syncState.catalogUrl.isBlank()) {
                                        showSyncSettingsDialog = true
                                    } else {
                                        viewModel.syncRemoteCatalogNow()
                                    }
                                },
                                onCheckUpdateNow = {
                                    if (syncState.versionUrl.isBlank()) {
                                        showSyncSettingsDialog = true
                                    } else {
                                        viewModel.checkForUpdateNow()
                                    }
                                },
                            )
                        }

                        composable(
                            route = DallasRoutes.ROUTE_ARTIST,
                            arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
                        ) { entry ->
                            val artistId = entry.arguments?.getString("artistId").orEmpty()
                            val artistTracksFlow = remember(artistId) {
                                viewModel.observeArtistTracks(artistId)
                            }
                            val artistTracks by artistTracksFlow.collectAsStateWithLifecycle()

                            ArtistDetailScreen(
                                artistId = artistId,
                                tracks = artistTracks,
                                playbackState = playbackState,
                                downloadingTrackIds = downloadingTrackIds,
                                onBack = { navController.popBackStack() },
                                onShufflePlay = { viewModel.playQueueShuffled(artistTracks) },
                                onPlayAll = { viewModel.playQueueSequential(artistTracks) },
                                onPlayTrack = { track -> viewModel.playTrack(track, artistTracks) },
                                onFavoriteClick = viewModel::toggleFavorite,
                                onDownloadClick = viewModel::toggleDownload,
                                onAddToPlaylistClick = { trackForPlaylistSheet = it },
                            )
                        }

                        composable(
                            route = DallasRoutes.ROUTE_PLAYLIST,
                            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
                        ) { entry ->
                            val playlistId = entry.arguments?.getLong("playlistId") ?: -1L
                            val playlistFlow = remember(playlistId) {
                                viewModel.observePlaylist(playlistId)
                            }
                            val playlistWithTracks by playlistFlow.collectAsStateWithLifecycle()

                            PlaylistDetailScreen(
                                playlistWithTracks = playlistWithTracks,
                                playbackState = playbackState,
                                downloadingTrackIds = downloadingTrackIds,
                                onBack = { navController.popBackStack() },
                                onPlayShuffled = viewModel::playQueueShuffled,
                                onPlaySequential = viewModel::playQueueSequential,
                                onPlayTrack = viewModel::playTrack,
                                onFavoriteClick = viewModel::toggleFavorite,
                                onDownloadClick = viewModel::toggleDownload,
                                onRemoveFromPlaylist = viewModel::removeTrackFromPlaylist,
                            )
                        }

                        composable(DallasRoutes.ROUTE_PLAYER) {
                            val activeTrack = playbackState.currentTrack
                            FullPlayerScreen(
                                playbackState = playbackState,
                                isDownloading = activeTrack != null && downloadingTrackIds.contains(activeTrack.id),
                                onClose = { navController.popBackStack() },
                                onArtistClick = { artistId ->
                                    navController.navigate(DallasRoutes.artistRoute(artistId))
                                },
                                onPlayPause = viewModel::togglePlayPause,
                                onNext = viewModel::skipNext,
                                onPrevious = viewModel::skipPrevious,
                                onSeekTo = viewModel::seekTo,
                                onToggleShuffle = viewModel::toggleShuffle,
                                onCycleRepeat = viewModel::cycleRepeatMode,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onToggleDownload = viewModel::toggleDownload,
                                onAddToPlaylist = { trackForPlaylistSheet = it },
                                onOpenAudioSourceDialog = { trackForAudioSourceDialog = it },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name -> viewModel.createPlaylist(name) },
        )
    }

    trackForPlaylistSheet?.let { track ->
        AddToPlaylistSheet(
            track = track,
            playlists = playlists,
            onDismiss = { trackForPlaylistSheet = null },
            onSelectPlaylist = { playlistWithTracks ->
                viewModel.addTrackToPlaylist(
                    playlistId = playlistWithTracks.playlist.id,
                    playlistName = playlistWithTracks.playlist.name,
                    track = track,
                )
            },
            onCreatePlaylistWithTrack = { newName ->
                viewModel.createPlaylist(newName, initialTrackId = track.id)
            },
        )
    }

    trackForAudioSourceDialog?.let { track ->
        AudioSourceDialog(
            track = track,
            onDismiss = { trackForAudioSourceDialog = null },
            onSaveUrl = { url -> viewModel.attachRemoteAudioUrl(track, url) },
            onPickLocalFile = {
                trackAwaitingLocalFile = track
                localAudioAttachLauncher.launch(arrayOf("audio/*"))
            },
        )
    }

    if (showSyncSettingsDialog) {
        SyncSettingsDialog(
            initialCatalogUrl = syncState.catalogUrl,
            initialVersionUrl = syncState.versionUrl,
            onDismiss = { showSyncSettingsDialog = false },
            onSave = viewModel::saveSyncUrls,
        )
    }

    syncState.availableUpdate?.let { updateInfo ->
        UpdateAvailableDialog(
            versionName = updateInfo.latestVersionName,
            releaseNotes = updateInfo.releaseNotes,
            onDismiss = viewModel::dismissUpdateDialog,
            onOpenDownloadPage = {
                if (updateInfo.downloadUrl.startsWith("https://", ignoreCase = true)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                    runCatching { context.startActivity(intent) }
                }
            },
        )
    }
}
