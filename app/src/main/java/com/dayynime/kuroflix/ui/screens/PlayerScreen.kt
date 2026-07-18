@file:OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
package com.dayynime.kuroflix.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.media3.common.util.UnstableApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dayynime.kuroflix.data.model.AnimeDetail
import com.dayynime.kuroflix.data.model.EpisodeItem
import com.dayynime.kuroflix.data.model.VideoServer
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.DarkSurface
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.Typography
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.PlayerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@kotlin.OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    episode: EpisodeItem,
    animeDetail: AnimeDetail,
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val playerState by viewModel.playerUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showServersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = episode.id) {
        viewModel.loadVideoSource(episode.id, animeDetail)
    }

    // Intercept hardware back to clean resources
    BackHandler {
        onBackClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (val state = playerState) {
            is PlayerUiState.Idle, is PlayerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is PlayerUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color.White,
                        style = Typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                        ) {
                            Text("Kembali")
                        }
                        Button(
                            onClick = { viewModel.loadVideoSource(episode.id, animeDetail) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            is PlayerUiState.Success -> {
                val source = state.videoSource

                if (source.isEmbed) {
                    // Render in WebView
                    EmbedPlayerView(
                        embedUrl = source.url,
                        onBack = onBackClick,
                        onShowServers = { showServersSheet = true }
                    )
                } else {
                    // Render in Native ExoPlayer
                    NativePlayerView(
                        sourceUrl = source.url,
                        headers = source.headers,
                        episodeTitle = "${animeDetail.title} - ${episode.title}",
                        onBack = onBackClick,
                        onShowServers = { showServersSheet = true },
                        onProgressUpdate = { progress, duration ->
                            // Save watch progress reactively to local Room DB
                            viewModel.saveProgress(
                                animeId = animeDetail.id,
                                episodeId = episode.id,
                                animeTitle = animeDetail.title,
                                episodeTitle = episode.title,
                                thumbnail = animeDetail.thumbnail,
                                progressMillis = progress,
                                durationMillis = duration
                            )
                        }
                    )
                }

                // Servers list bottom sheet
                if (showServersSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showServersSheet = false },
                        containerColor = DarkSurface,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .padding(bottom = 32.dp)
                        ) {
                            Text(
                                text = "Pilih Server / Mirror Nonton",
                                color = Color.White,
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            state.servers.forEach { server ->
                                val isSelected = state.selectedServer?.name == server.name
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            viewModel.selectServer(server, state.servers, animeDetail)
                                            showServersSheet = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) OrangeAccent.copy(alpha = 0.15f) else DarkBg
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = server.name,
                                            color = if (isSelected) OrangeAccent else Color.White,
                                            style = Typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = OrangeAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmbedPlayerView(
    embedUrl: String,
    onBack: () -> Unit,
    onShowServers: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    loadUrl(embedUrl)
                }
            }
        )

        // Overlay Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Button(
                onClick = onShowServers,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Servers",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Pilih Server", color = Color.White, style = Typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun NativePlayerView(
    sourceUrl: String,
    headers: Map<String, String>,
    episodeTitle: String,
    onBack: () -> Unit,
    onShowServers: () -> Unit,
    onProgressUpdate: (progress: Long, duration: Long) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }

    // Setup ExoPlayer — pakai OkHttpDataSource (bukan DefaultHttpDataSource bawaan)
    // supaya request streaming manifest/segment ExoPlayer ikut kirim header
    // Referer/Origin/User-Agent yang sama kayak dipakai VideoExtractor pas resolve,
    // dan ikut lewat OkHttpClient yang punya fallback DNS-over-HTTPS. Tanpa ini,
    // kebanyakan host bakal nolak request ExoPlayer (403) walau direct URL-nya
    // udah bener, karena dianggap request tanpa Referer yang valid.
    val player = remember(sourceUrl) {
        val httpDataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(
            com.dayynime.kuroflix.data.network.VideoExtractor.streamingHttpClient
        ).setDefaultRequestProperties(headers)
        val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(context, httpDataSourceFactory)
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(15_000, 50_000, 2_500, 5_000)
            .build()
        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                if (sourceUrl.contains(".m3u8"))
                    androidx.media3.exoplayer.hls.HlsMediaSource.Factory(dataSourceFactory)
                else
                    androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
            )
            .build().apply {
                playWhenReady = true
            }
    }

    LaunchedEffect(key1 = sourceUrl) {
        val mediaItem = MediaItem.Builder()
            .setUri(sourceUrl)
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    // Progress updates tracking
    LaunchedEffect(key1 = isPlaying) {
        while (isPlaying) {
            currentPosition = player.currentPosition
            totalDuration = player.duration
            if (currentPosition > 0 && totalDuration > 0) {
                onProgressUpdate(currentPosition, totalDuration)
            }
            delay(1000)
        }
    }

    // Auto hide controls in 3 seconds
    LaunchedEffect(key1 = isControlsVisible) {
        if (isControlsVisible) {
            delay(3000)
            isControlsVisible = false
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
    ) {
        // Native Player Canvas View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )

        // Custom Overlay controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = episodeTitle,
                            color = Color.White,
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onShowServers,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Server",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Pilih Server", color = Color.White, style = Typography.labelSmall)
                        }
                    }
                }

                // Middle Media Actions (Play, Rewind, Fast Forward)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0)) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "-10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    IconButton(
                        onClick = {
                            if (player.isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.play()
                                isPlaying = true
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(OrangeAccent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    IconButton(
                        onClick = { player.seekTo((player.currentPosition + 10000).coerceAtMost(player.duration)) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "+10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom Seek controllers
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val progressValue = if (totalDuration > 0L) currentPosition.toFloat() / totalDuration.toFloat() else 0f
                    Slider(
                        value = progressValue,
                        onValueChange = { percent ->
                            val seekTo = (percent * totalDuration).toLong()
                            player.seekTo(seekTo)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = OrangeAccent,
                            activeTrackColor = OrangeAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            style = Typography.labelSmall
                        )
                        Text(
                            text = formatTime(totalDuration),
                            color = Color.White,
                            style = Typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val hrs = (millis / 3600000).toInt()
    val mins = ((millis % 3600000) / 60000).toInt()
    val secs = ((millis % 60000) / 1000).toInt()

    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
