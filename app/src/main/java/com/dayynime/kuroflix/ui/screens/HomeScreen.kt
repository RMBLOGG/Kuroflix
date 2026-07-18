package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dayynime.kuroflix.data.local.WatchHistoryEntity
import com.dayynime.kuroflix.data.model.AnimeItem
import com.dayynime.kuroflix.ui.components.*
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeItem) -> Unit,
    onContinueWatchClick: (WatchHistoryEntity) -> Unit,
    onScheduleClick: () -> Unit = {}
) {
    val currentSource by viewModel.selectedSource.collectAsState()
    val homeState by viewModel.homeUiState.collectAsState()
    val watchHistory by viewModel.history.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val sources = listOf(
        "animasu" to "Dayynime V1",
        "samehadaku" to "Dayynime V2",
        "animekompi" to "Dayynime V3",
        "donghua" to "Dayynime V4 (Donghua)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Logo & Notification Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kuroflix",
                style = Typography.displayLarge,
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.drawWithContent {
                    drawContent()
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.loadHome() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextPrimary
                    )
                }
                IconButton(onClick = onScheduleClick) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Jadwal Rilis",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Horizontal Source Selector Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sources) { (key, label) ->
                val isActive = currentSource == key
                val bgBrush = if (isActive) FireGradient else Brush.linearGradient(listOf(DarkSurface, DarkSurface))
                val border = if (!isActive) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(bgBrush)
                        .then(if (border != null) Modifier.border(border, RoundedCornerShape(50.dp)) else Modifier)
                        .clickable { viewModel.setSource(key) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isActive) Color.White else TextSecondary,
                        style = Typography.labelLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Main Scrollable Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = homeState) {
                is HomeUiState.Loading -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        repeat(3) {
                            ShimmerHomeSection()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gagal memuat anime:\n${state.message}",
                            color = TextSecondary,
                            style = Typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.loadHome() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is HomeUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 100.dp) // space for bottom floating navigation bar
                    ) {
                        // Hero Auto-scroll Banner Carousel
                        val popularItems = state.popular.take(5)
                        if (popularItems.isNotEmpty()) {
                            HeroCarousel(popularItems, onAnimeClick)
                        }

                        // Section "Lanjutkan Nonton" (Local History)
                        val activeHistory = watchHistory.filter { it.source == currentSource }
                        if (activeHistory.isNotEmpty()) {
                            SectionHeader(title = "Lanjutkan Nonton")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(activeHistory) { item ->
                                    ContinueWatchCard(item, onContinueWatchClick)
                                }
                            }
                        }

                        // Section Latest / Terbaru
                        if (state.latest.isNotEmpty()) {
                            SectionHeader(title = "Terbaru")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.latest) { anime ->
                                    AnimeCard(
                                        anime = anime,
                                        onClick = { onAnimeClick(anime) },
                                        modifier = Modifier.width(135.dp)
                                    )
                                }
                            }
                        }

                        // Section Ongoing
                        if (state.ongoing.isNotEmpty()) {
                            SectionHeader(title = "Sedang Tayang")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.ongoing) { anime ->
                                    AnimeCard(
                                        anime = anime,
                                        onClick = { onAnimeClick(anime) },
                                        modifier = Modifier.width(135.dp)
                                    )
                                }
                            }
                        }

                        // Section Completed
                        if (state.completed.isNotEmpty()) {
                            SectionHeader(title = "Selesai Tayang")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.completed) { anime ->
                                    AnimeCard(
                                        anime = anime,
                                        onClick = { onAnimeClick(anime) },
                                        modifier = Modifier.width(135.dp)
                                    )
                                }
                            }
                        }

                        // Section Movies
                        if (state.movies.isNotEmpty()) {
                            SectionHeader(title = "Film & Movie")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.movies) { anime ->
                                    AnimeCard(
                                        anime = anime,
                                        onClick = { onAnimeClick(anime) },
                                        modifier = Modifier.width(135.dp)
                                    )
                                }
                            }
                        }

                        // Section Jadwal Hari Ini (dari endpoint Schedule yg beneran ada)
                        if (state.upcoming.isNotEmpty()) {
                        SectionHeader(title = "Jadwal Hari Ini")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(state.upcoming.take(10)) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime) },
                                    modifier = Modifier.width(135.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    items: List<AnimeItem>,
    onAnimeClick: (AnimeItem) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll logic (4s)
    LaunchedEffect(key1 = pagerState) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            val item = items[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onAnimeClick(item) }
            ) {
                // Background Full-Bleed
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkOverlayGradient)
                )

                // Overlay Content details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(FireGradient, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "POPULER",
                            color = Color.White,
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        color = Color.White,
                        style = Typography.titleLarge,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(50.dp))
                                .clickable { onAnimeClick(item) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Tonton Sekarang",
                                color = Color.Black,
                                style = Typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(items.size) { i ->
                val isActive = pagerState.currentPage == i
                val width = if (isActive) 18.dp else 6.dp
                val color = if (isActive) OrangeAccent else Color.White.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .animateContentSize()
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        style = Typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun ContinueWatchCard(
    item: WatchHistoryEntity,
    onClick: (WatchHistoryEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.animeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Linear Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkOverlayGradient)
                )

                // Play icon centered
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Progress Bar Gradient
                val progress = if (item.durationMillis > 0) item.progressMillis.toFloat() / item.durationMillis else 0f
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(FireGradient)
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.animeTitle,
                    color = Color.White,
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.episodeTitle,
                    color = TextSecondary,
                    style = Typography.labelSmall,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
