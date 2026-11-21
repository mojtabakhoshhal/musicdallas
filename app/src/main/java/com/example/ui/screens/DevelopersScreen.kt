package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DallasGold
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SyncUiState

@Composable
fun DevelopersScreen(
    syncState: SyncUiState = SyncUiState(),
    onConfigureSyncUrls: () -> Unit = {},
    onSyncCatalogNow: () -> Unit = {},
    onCheckUpdateNow: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("developers_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Brand Badge
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(ObsidianElevated, ObsidianCard),
                    ),
                )
                .border(1.dp, DallasGold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dallas_music_logo),
                contentDescription = stringResource(R.string.cd_app_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, DallasGold, RoundedCornerShape(18.dp)),
            )
            Text(
                text = stringResource(R.string.app_brand_persian),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "DALLAS MUSIC",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = OutfitFontFamily,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = DallasGold,
            )
            Text(
                text = stringResource(R.string.developers_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = stringResource(R.string.developers_title),
            style = MaterialTheme.typography.titleLarge,
        )

        DeveloperCreditCard(
            name = stringResource(R.string.dev_mojtaba_name),
            role = stringResource(R.string.dev_mojtaba_role),
            icon = Icons.Filled.Code,
            tag = "credit_mojtaba",
        )

        DeveloperCreditCard(
            name = stringResource(R.string.dev_amir_name),
            role = stringResource(R.string.dev_amir_role),
            icon = Icons.Filled.Campaign,
            tag = "credit_amir",
        )

        // Online Catalog Sync & Update Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCard)
                .border(1.dp, DallasGold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(18.dp)
                .testTag("github_sync_card"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudSync,
                    contentDescription = null,
                    tint = DallasGold,
                )
                Text(
                    text = stringResource(R.string.sync_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = stringResource(R.string.sync_section_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Button(
                onClick = onSyncCatalogNow,
                enabled = !syncState.isSyncingCatalog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DallasGold,
                    contentColor = ObsidianBlack,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sync_catalog_now_btn"),
            ) {
                if (syncState.isSyncingCatalog) {
                    CircularProgressIndicator(
                        color = ObsidianBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sync_catalog_now),
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onConfigureSyncUrls,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("configure_sync_urls_btn"),
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = DallasGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.sync_configure_urls),
                        style = MaterialTheme.typography.labelMedium,
                        color = DallasGold,
                    )
                }

                OutlinedButton(
                    onClick = onCheckUpdateNow,
                    enabled = !syncState.isCheckingUpdate,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("check_update_now_btn"),
                ) {
                    if (syncState.isCheckingUpdate) {
                        CircularProgressIndicator(
                            color = DallasGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = DallasGold, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.sync_check_update),
                        style = MaterialTheme.typography.labelMedium,
                        color = DallasGold,
                    )
                }
            }
        }

        // Architecture Overview Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ObsidianCard)
                .border(1.dp, ObsidianOutline, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Memory,
                    contentDescription = null,
                    tint = DallasGold,
                )
                Text(
                    text = stringResource(R.string.developers_tech_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = stringResource(R.string.developers_tech_body),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Text(
            text = stringResource(R.string.developers_version),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun DeveloperCreditCard(
    name: String,
    role: String,
    icon: ImageVector,
    tag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianCard)
            .border(1.dp, DallasGold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(DallasGold.copy(alpha = 0.16f))
                .border(1.dp, DallasGold.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DallasGold,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodyMedium,
                color = DallasGold,
            )
        }
    }
}
