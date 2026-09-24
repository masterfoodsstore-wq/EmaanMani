package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameRelease
import com.example.model.UserAccount

private val GoldLight = Color(0xFFFFE082)
private val GoldCore = Color(0xFFFFD54F)
private val GoldDark = Color(0xFFC49000)
private val EmeraldDark = Color(0xFF04140C)
private val EmeraldCard = Color(0xEE092015)

@Composable
fun AdminUpdateControlScreen(
    user: UserAccount?,
    currentVersion: String,
    releases: List<GameRelease>,
    onTogglePublish: (String, Boolean) -> Unit,
    onSetInstalledVersion: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // SECURITY GUARD: Only authorized administrators can access this page
    if (user == null || !user.isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(EmeraldDark)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(48.dp))
                Text(
                    text = "ACCESS DENIED",
                    color = Color(0xFFFF5252),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "This administration route is restricted to authorized game administrators.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldCore)
                        .clickable(onClick = onNavigateBack)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Return to Game Home", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val latestRelease = releases.filter { it.isPublished }.maxByOrNull { it.versionCode }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF0B2D1D), Color(0xFF05170F), EmeraldDark),
                    radius = 1800f
                )
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B261A))
                .border(1.dp, GoldCore, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp).testTag("admin_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldLight)
                }
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldCore, modifier = Modifier.size(20.dp))
                Text(
                    text = "ADMIN UPDATE CONTROL & GITHUB RELEASES",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF4A148C))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = "ADMIN: ${user.username}", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary Bar: Production Version | Latest GitHub Release | Total Versions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Current Installed Version
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldCard)
                    .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("CURRENT INSTALLED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(currentVersion, color = GoldCore, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "[Reset to v1.0.0]",
                            color = Color(0xFF81D4FA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSetInstalledVersion("v1.0.0") }
                        )
                        Text(
                            text = "[Set to v1.1.0]",
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSetInstalledVersion("v1.1.0") }
                        )
                    }
                }
            }

            // Card 2: Latest GitHub Release
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldCard)
                    .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("LATEST GITHUB RELEASE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(latestRelease?.versionName ?: "None", color = Color(0xFF00E676), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = "Published: ${latestRelease?.releaseDate ?: "-"}",
                        color = Color.LightGray,
                        fontSize = 9.sp
                    )
                }
            }

            // Card 3: Total Versions Available
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldCard)
                    .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("TOTAL RELEASES IN CATALOG", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${releases.size} Versions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("${releases.count { it.isPublished }} Published • ${releases.count { !it.isPublished }} Draft", color = Color.LightGray, fontSize = 9.sp)
                }
            }
        }

        // Release Items List
        Text(
            text = "RELEASES MANAGEMENT & PUBLISH TOGGLES",
            color = GoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(releases) { rel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldCard)
                        .border(
                            1.dp,
                            if (rel.versionName == currentVersion) GoldCore else Color(0xFF1B4D36),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = rel.versionName,
                                color = GoldCore,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (rel.versionName == currentVersion) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF00E676))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text("CURRENT", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Text(
                                text = "• ${rel.releaseDate} • ${rel.assetSizeMb} MB",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = rel.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Checksum: ${rel.sha256Checksum.take(16)}...",
                            color = Color.Gray,
                            fontSize = 9.sp
                        )

                        Text(
                            text = "What's New: " + rel.whatsNew.joinToString(" • "),
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }

                    // Publish Toggle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (rel.isPublished) "PUBLISHED" else "DRAFT",
                            color = if (rel.isPublished) Color(0xFF00E676) else Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = rel.isPublished,
                            onCheckedChange = { isPub -> onTogglePublish(rel.versionName, isPub) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldCore,
                                checkedTrackColor = Color(0xFF0F472A),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("switch_publish_${rel.versionName}")
                        )
                    }
                }
            }
        }
    }
}
