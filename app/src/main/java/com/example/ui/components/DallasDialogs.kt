package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlaylistWithTracks
import com.example.data.TrackEntity
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextSecondary

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = stringResource(R.string.library_create_playlist),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.library_playlist_name_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playlist_name_input"),
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DallasGold,
                    contentColor = ObsidianBlack,
                ),
                modifier = Modifier.testTag("confirm_create_playlist_btn"),
            ) {
                Text(stringResource(R.string.action_create), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    track: TrackEntity,
    playlists: List<PlaylistWithTracks>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (PlaylistWithTracks) -> Unit,
    onCreatePlaylistWithTrack: (String) -> Unit,
) {
    var showInlineCreate by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.action_add_to_playlist),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${track.title} — ${track.artist}",
                style = MaterialTheme.typography.bodyMedium,
                color = DallasGold,
            )

            if (showInlineCreate) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.library_playlist_name_hint)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inline_playlist_name_input"),
                    )
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylistWithTrack(newPlaylistName.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DallasGold,
                            contentColor = ObsidianBlack,
                        ),
                        modifier = Modifier.testTag("inline_create_playlist_btn"),
                    ) {
                        Text(stringResource(R.string.action_create), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showInlineCreate = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sheet_new_playlist_btn"),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = DallasGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.library_create_playlist), color = DallasGold)
                }
            }

            if (playlists.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(playlists, key = { it.playlist.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianCard)
                                .clickable {
                                    onSelectPlaylist(item)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                .testTag("sheet_playlist_item_${item.playlist.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QueueMusic,
                                contentDescription = null,
                                tint = DallasGold,
                                modifier = Modifier.size(22.dp),
                            )
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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AudioSourceDialog(
    track: TrackEntity,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit,
    onPickLocalFile: () -> Unit,
) {
    var url by remember(track.id) { mutableStateOf(track.remoteUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = stringResource(R.string.player_audio_source_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.player_audio_source_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.player_audio_url_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_url_input"),
                )
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onPickLocalFile()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_local_audio_btn"),
                ) {
                    Icon(Icons.Filled.AudioFile, contentDescription = null, tint = DallasGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_attach_local), color = DallasGold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(url)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DallasGold,
                    contentColor = ObsidianBlack,
                ),
                modifier = Modifier.testTag("save_audio_url_btn"),
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        },
    )
}

@Composable
fun SyncSettingsDialog(
    initialCatalogUrl: String,
    initialVersionUrl: String,
    onDismiss: () -> Unit,
    onSave: (catalogUrl: String, versionUrl: String) -> Unit,
) {
    var catalogUrl by remember(initialCatalogUrl) { mutableStateOf(initialCatalogUrl) }
    var versionUrl by remember(initialVersionUrl) { mutableStateOf(initialVersionUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = stringResource(R.string.sync_configure_urls),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.sync_section_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                OutlinedTextField(
                    value = catalogUrl,
                    onValueChange = { catalogUrl = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.sync_catalog_url_label)) },
                    placeholder = { Text("https://raw.githubusercontent.com/.../catalog.json") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_catalog_url_input"),
                )
                OutlinedTextField(
                    value = versionUrl,
                    onValueChange = { versionUrl = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.sync_version_url_label)) },
                    placeholder = { Text("https://raw.githubusercontent.com/.../version.json") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_version_url_input"),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(catalogUrl, versionUrl)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DallasGold,
                    contentColor = ObsidianBlack,
                ),
                modifier = Modifier.testTag("save_sync_urls_btn"),
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        },
    )
}

@Composable
fun UpdateAvailableDialog(
    versionName: String,
    releaseNotes: String,
    onDismiss: () -> Unit,
    onOpenDownloadPage: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        title = {
            Text(
                text = "${stringResource(R.string.update_available_title)} ($versionName)",
                style = MaterialTheme.typography.titleLarge,
                color = DallasGold,
            )
        },
        text = {
            Text(
                text = releaseNotes.ifBlank { "نسخه جدیدتر دالاس موزیک آماده دریافت است." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onOpenDownloadPage()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DallasGold,
                    contentColor = ObsidianBlack,
                ),
                modifier = Modifier.testTag("open_update_download_btn"),
            ) {
                Text(stringResource(R.string.update_download_action), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        },
    )
}

