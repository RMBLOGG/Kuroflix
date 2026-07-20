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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.dayynime.kuroflix.ui.viewmodel.AuthUiState
import com.dayynime.kuroflix.ui.viewmodel.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeItem) -> Unit,
    onContinueWatchClick: (WatchHistoryEntity) -> Unit,
    onScheduleClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    val currentSource by viewModel.selectedSource.collectAsState()
    val homeState by viewModel.homeUiState.collectAsState()
    val watchHistory by viewModel.history.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val loggedInUser = (authState as? AuthUiState.LoggedIn)?.user

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val sourceLabels = mapOf(
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
            if (loggedInUser != null) {
                // Sudah login -- tampilin identitas akun (foto profil Google + nama),
                // gantiin wordmark biar header terasa personal.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUrl = loggedInUser.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = (loggedInUser.name ?: loggedInUser.email)
                                ?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "K"
                            Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KUROFLIX",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = loggedInUser.name ?: loggedInUser.email ?: "Pengguna",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 160.dp)
                        )
                    }
                }
            } else {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kuro",
                            style = Typography.displayLarge,
                            fontSize = 26.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "flix",
                            style = Typography.displayLarge,
                            fontSize = 26.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge BETA -- nandain app ini masih tahap pengembangan,
                        // styling-nya disamain sama badge "POPULER" di hero card
                        // (outline tipis, bukan blok solid) biar konsisten.
                        Box(
                            modifier = Modifier
                                .background(Color.Transparent, RoundedCornerShape(50.dp))
                                .border(1.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BETA",
                                color = GoldAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    // Sumber aktif sekarang ditampilin di sini aja (kecil, non-interaktif) --
                    // gantinya tab pill yang dulu di header. Ganti sumbernya lewat Settings.
                    Text(
                        text = sourceLabels[currentSource] ?: currentSource,
                        color = TextSecondary,
                        style = Typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onChatClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat Room",
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
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Main Scrollable Content -- swipe ke bawah buat refresh manual
        // (dulu ada icon refresh terpisah di header, sekarang gestur ini yang gantiin,
        // biar slot icon di header lega buat Settings).
        var isRefreshing by remember { mutableStateOf(false) }
        val pullState = rememberPullToRefreshState()

        LaunchedEffect(isRefreshing) {
            if (isRefreshing) {
                viewModel.loadHome()
                delay(600)
                isRefreshing = false
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
            state = pullState,
            modifier = Modifier.fillMaxSize()
        ) {
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
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "POPULER",
                            color = GoldAccent,
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
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
