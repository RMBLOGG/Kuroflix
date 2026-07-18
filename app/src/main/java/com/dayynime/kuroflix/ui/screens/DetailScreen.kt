package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dayynime.kuroflix.data.model.AnimeDetail
import com.dayynime.kuroflix.data.model.AnimeItem
import com.dayynime.kuroflix.data.model.EpisodeItem
import com.dayynime.kuroflix.ui.components.DarkOverlayGradient
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.components.GlowButton
import com.dayynime.kuroflix.ui.components.ShimmerBrush
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    animeId: String,
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onEpisodeClick: (EpisodeItem, AnimeDetail) -> Unit
) {
    val detailState by viewModel.detailUiState.collectAsState()
    val isBookmarked by viewModel.isBookmarked(animeId).collectAsState(initial = false)
    val historyList by viewModel.history.collectAsState()

    LaunchedEffect(key1 = animeId) {
        viewModel.loadDetail(animeId)
    }

    LaunchedEffect(key1 = detailState) {
        (detailState as? DetailUiState.Success)?.let { viewModel.loadAvailableServers(it.detail) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        when (val state = detailState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is DetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.White,
                        style = Typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadDetail(animeId) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }

            is DetailUiState.Success -> {
                val detail = state.detail
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 60.dp)
                ) {
                    // Parallax Big Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = detail.thumbnail,
                            contentDescription = detail.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkOverlayGradient)
                        )
                    }

                    // Content details
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset(y = (-40).dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Mini Poster Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(160.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                AsyncImage(
                                    model = detail.thumbnail,
                                    contentDescription = detail.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Details right side
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 45.dp)
                            ) {
                                Text(
                                    text = detail.title,
                                    color = Color.White,
                                    style = Typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${detail.status} • ${detail.type}",
                                        color = TextSecondary,
                                        style = Typography.labelSmall
                                    )
                                    if (detail.rating.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .background(FireGradient, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "★ ${detail.rating}",
                                                color = Color.White,
                                                style = Typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Play & Bookmark control actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val firstEp = detail.episodes.lastOrNull() // typically episodes list asc or desc
                            GlowButton(
                                text = "Mulai Menonton",
                                onClick = {
                                    if (firstEp != null) {
                                        onEpisodeClick(firstEp, detail)
                                    } else {
                                        // Trigger no episode dialog or toast
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Favorite Toggle Button
                            IconButton(
                                onClick = { viewModel.toggleBookmark(detail) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DarkSurface, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) RedAccent else Color.White
                                )
                            }
                        }

                        // Default server streaming — cuma nampilin server yang BENERAN ADA
                        // buat anime ini (hasil sampel episode pertama), bukan daftar
                        // gabungan semua host yang mungkin. Berlaku global lintas 4 sumber
                        // data karena disimpen berdasarkan nama server, bukan per-source.
                        val preferredKeyword by viewModel.preferredServerKeyword.collectAsState()
                        val availableServers by viewModel.availableServers.collectAsState()
                        var serverMenuExpanded by remember { mutableStateOf(false) }
                        val currentLabel = if (preferredKeyword.isBlank()) {
                            "Otomatis (server pertama)"
                        } else {
                            availableServers.firstOrNull { it.name.contains(preferredKeyword, ignoreCase = true) }?.name
                                ?: preferredKeyword
                        }

                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(DarkSurface)
                                    .clickable(enabled = availableServers.isNotEmpty()) { serverMenuExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (availableServers.isEmpty()) "Server Default: memuat..."
                                           else "Server Default: $currentLabel",
                                    color = TextSecondary,
                                    style = Typography.labelSmall
                                )
                            }
                            DropdownMenu(
                                expanded = serverMenuExpanded,
                                onDismissRequest = { serverMenuExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Otomatis (server pertama)",
                                            color = if (preferredKeyword.isBlank()) OrangeAccent else Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.setPreferredServer("")
                                        serverMenuExpanded = false
                                    }
                                )
                                availableServers.forEach { server ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = server.name,
                                                color = if (server.name.contains(preferredKeyword, ignoreCase = true) && preferredKeyword.isNotBlank())
                                                    OrangeAccent else Color.White
                                            )
                                        },
                                        onClick = {
                                            viewModel.setPreferredServer(server.name)
                                            serverMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Genres chips
                        if (detail.genres.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                detail.genres.forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = genre,
                                            color = TextSecondary,
                                            style = Typography.labelSmall,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Collapsible Synopsis
                        var isExpanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable { isExpanded = !isExpanded }
                                .animateContentSize(animationSpec = tween(300))
                        ) {
                            Text(
                                text = "Sinopsis",
                                color = Color.White,
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = detail.description,
                                color = TextSecondary,
                                style = Typography.bodyMedium,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp
                            )
                            Text(
                                text = if (isExpanded) "Sembunyikan" else "Selengkapnya...",
                                color = OrangeAccent,
                                style = Typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Episode Tabs Underline List
                        Text(
                            text = "Episode (${detail.episodes.size})",
                            color = Color.White,
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)
                        )

                        // Episodes vertical listings
                        if (detail.episodes.isEmpty()) {
                            Text(
                                text = "Tidak ada episode yang tersedia.",
                                color = TextMuted,
                                style = Typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            detail.episodes.forEachIndexed { index, ep ->
                                val compositeEpId = "${detail.source}:${ep.id}"
                                val isWatched = historyList.any { it.episodeId == compositeEpId }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable { onEpisodeClick(ep, detail) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isWatched) DarkSurface.copy(alpha = 0.5f) else DarkSurface
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ep.title,
                                                color = if (isWatched) TextSecondary else Color.White,
                                                style = Typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (ep.date.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = ep.date,
                                                    color = TextMuted,
                                                    style = Typography.labelSmall,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (isWatched) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Sudah Ditonton",
                                                    tint = OrangeAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Tonton",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(20.dp)
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

        // Floating Back Button on Top Left
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
