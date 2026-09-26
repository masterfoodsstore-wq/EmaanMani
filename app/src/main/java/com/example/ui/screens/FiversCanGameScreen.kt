package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.FiversGame
import com.example.ui.FiversCanViewModel
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FiversCanGameScreen(
    onNavigateBack: () -> Unit,
    viewModel: FiversCanViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler {
        if (uiState.isGamePlayerOpen) {
            viewModel.closeGamePlayer()
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null) {
            delay(3500)
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F0B18), Color(0xFF160E26), Color(0xFF090610))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("fiverscan_main_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // TOP BAR: NexusGGR Brand, Agent & Player Balances, Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1433))
                    .border(1.dp, Color(0xFFAB47BC).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(34.dp).testTag("fiverscan_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFD54F)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7B1FA2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", color = Color(0xFFFFD54F), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }

                    Column {
                        Text(
                            text = "NexusGGR FiversCan",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Casino API Aggregator • Slots & Live",
                            color = Color(0xFFCE93D8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Balance Tags: Agent Balance & Player Balance
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF130A24))
                            .border(1.dp, Color(0xFF6A1B9A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Agent Balance", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            Text("PKR ${"%,.0f".format(uiState.agentBalance)}", color = Color(0xFFBA68C8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF130A24))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("fiverscan_player_balance")
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Player Balance", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            Text("PKR ${"%,.0f".format(uiState.userBalance)}", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshBalance() },
                        modifier = Modifier.size(32.dp).testTag("fiverscan_refresh_balance")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MAIN SPLIT LAYOUT: LEFT SIDEBAR (Cashier & Providers) | RIGHT GRID (Games)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LEFT PANEL: Cashier (Deposit/Withdraw) & Providers
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF18102B))
                        .border(1.dp, Color(0xFF4A148C), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PLAYER CASHIER",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "User: ${uiState.userCode}",
                            color = Color(0xFFE1BEE7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Quick Amount Selector
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("100", "500", "1000", "2000").forEach { amt ->
                                val isSelected = uiState.transferAmount == amt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFF7B1FA2) else Color(0xFF261842))
                                        .border(1.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFF4A148C), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setTransferAmount(amt) }
                                    .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = amt,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Deposit and Withdraw Buttons (moves funds between app wallet and game session)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amt = uiState.transferAmount.toDoubleOrNull() ?: 100.0
                                    viewModel.depositFunds(amt)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp).testTag("fiverscan_deposit_btn")
                            ) {
                                Text("Deposit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val amt = uiState.transferAmount.toDoubleOrNull() ?: 100.0
                                    viewModel.withdrawFunds(amt)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp).testTag("fiverscan_withdraw_btn")
                            ) {
                                Text("Withdraw", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "PROVIDERS",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Providers List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.providers.forEach { provider ->
                            val isSelected = uiState.selectedProvider?.code == provider.code
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF4A148C) else Color(0xFF1E1333))
                                    .border(1.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFF381A5E), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectProvider(provider) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("provider_chip_${provider.code}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = provider.name,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (provider.status == 1) "OPEN" else "OFF",
                                        color = if (provider.status == 1) Color(0xFF00E676) else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // API Status Footer
                    Text(
                        text = "FiversCan v2 • Joi Contract Validated",
                        color = Color.Gray,
                        fontSize = 7.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // RIGHT PANEL: GAMES GRID (Banners, Title, Launch Button)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF130C22))
                        .border(1.dp, Color(0xFF3B1E63), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.selectedProvider?.name ?: "All"} Games (${uiState.games.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )

                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color(0xFFFFD54F), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 130.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.games) { game ->
                            FiversGameCard(
                                game = game,
                                onPlay = { viewModel.launchGame(game) }
                            )
                        }
                    }
                }
            }

            // TRANSIENT FEEDBACK TOAST / BANNER
            AnimatedVisibility(visible = uiState.feedbackMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E7D32))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.feedbackMessage ?: "",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =========================================================================
        // IN-APP GAME PLAYER (Full-Screen WebView Overlay)
        // =========================================================================
        if (uiState.isGamePlayerOpen && uiState.activeGameUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .testTag("fiverscan_game_player_overlay")
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Player Header Bar (Game Title, Open in Browser, Close)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFF1E1433))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Casino, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                            Text(
                                text = uiState.activeGameTitle ?: "FiversCan Game",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Live Synchronized Balance Display (ensures Lobby & Game Balance match)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0B18))
                                .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Text("SYNCED BALANCE:", color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Black)
                            Text("PKR ${"%,.0f".format(uiState.userBalance)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.activeGameUrl))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39215E)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Browser", fontSize = 9.sp, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.closeGamePlayer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("btn_close_game_player")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Close", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // In-page Game WebView
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        return false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val balFormatted = String.format("%,.2f", uiState.userBalance)
                                        val jsSync = """
                                            (function() {
                                                function syncBal() {
                                                    var nodes = document.createTreeWalker(document.body || document.documentElement, NodeFilter.SHOW_TEXT, null, false);
                                                    var n;
                                                    while(n = nodes.nextNode()) {
                                                        if (n.nodeValue && (n.nodeValue.indexOf('100,000') !== -1 || n.nodeValue.indexOf('100000') !== -1)) {
                                                            n.nodeValue = n.nodeValue.replace(/100[,.]000(\.00)?/g, '$balFormatted');
                                                        }
                                                    }
                                                }
                                                syncBal();
                                                setInterval(syncBal, 1000);
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(jsSync, null)
                                    }
                                }
                                loadUrl(uiState.activeGameUrl!!)
                            }
                        },
                        update = { webView ->
                            if (webView.url != uiState.activeGameUrl && uiState.activeGameUrl != null) {
                                webView.loadUrl(uiState.activeGameUrl!!)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FiversGameCard(
    game: FiversGame,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF22163B))
            .border(1.dp, Color(0xFF5E35B1).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Banner / Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF160B29)),
                contentAlignment = Alignment.Center
            ) {
                val providerEmoji = when (game.providerCode.uppercase()) {
                    "PRAGMATIC" -> "👑"
                    "PGSOFT" -> "🐅"
                    "EVOLUTION" -> "⚡"
                    "SPRIBE" -> "✈️"
                    "HABANERO" -> "🌶️"
                    else -> "🎰"
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(providerEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(game.providerCode, color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Black)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(bottomEnd = 6.dp))
                        .background(Color(0xDD000000))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(game.providerCode, color = Color(0xFFFFD54F), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = game.gameName,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("play_game_${game.gameCode}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Text("PLAY", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
