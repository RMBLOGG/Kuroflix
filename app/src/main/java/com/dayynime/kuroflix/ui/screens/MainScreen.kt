package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayynime.kuroflix.data.local.WatchHistoryEntity
import com.dayynime.kuroflix.data.model.AnimeDetail
import com.dayynime.kuroflix.data.model.AnimeItem
import com.dayynime.kuroflix.data.model.EpisodeItem
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.components.NeonGradient
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.DarkSurface
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel

sealed class AppRoute {
    object MainShell : AppRoute()
    object Schedule : AppRoute()
    object Settings : AppRoute()
    data class Detail(val animeId: String) : AppRoute()
    data class Player(val episode: EpisodeItem, val animeDetail: AnimeDetail) : AppRoute()
}

@Composable
fun MainScreen(viewModel: AnimeViewModel) {
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.MainShell) }
    var selectedTab by remember { mutableStateOf(0) }

    // Navigation Stack for back routing
    val navigationStack = remember { mutableStateListOf<AppRoute>() }

    fun navigateTo(route: AppRoute) {
        navigationStack.add(currentRoute)
        currentRoute = route
    }

    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            currentRoute = navigationStack.removeAt(navigationStack.size - 1)
        } else if (currentRoute != AppRoute.MainShell) {
            currentRoute = AppRoute.MainShell
        }
    }

    // Landscape + fullscreen immersive dikelola di sini (bukan di dalam PlayerScreen)
    // dan di-key dari status "lagi di route Player atau bukan" -- BUKAN dari instance
    // Player-nya. Alasannya: pas autoplay pindah ke episode berikutnya, Compose bikin
    // instance PlayerScreen baru (utk episode baru) SEBELUM instance lama beres animasi
    // keluar. Kalau tiap instance PlayerScreen punya logic "reset ke portrait pas dispose"
    // sendiri-sendiri, instance LAMA yang baru dispose belakangan bakal nge-reset balik ke
    // portrait SETELAH instance BARU udah nge-set landscape -- makanya kejadian kayak di
    // laporan bug: klik "Putar Sekarang" tapi malah balik ke portrait. Dengan di-key dari
    // boolean di level MainScreen, transisi antar-episode gak bakal retrigger apa-apa
    // karena boolean-nya tetap true selama masih di Player, cuma berubah pas bener-bener
    // masuk/keluar dari area Player.
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isPlayerRoute = currentRoute is AppRoute.Player
    LaunchedEffect(isPlayerRoute) {
        if (isPlayerRoute) {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity?.window?.decorView?.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router animations
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(350),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) + fadeIn(tween(250)) togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(350),
                                targetOffsetX = { fullWidth -> -fullWidth }
                            ) + fadeOut(tween(250))
                },
                label = "route_transition"
            ) { route ->
                when (route) {
                    is AppRoute.MainShell -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Render active tab inside the Main shell
                            when (selectedTab) {
                                0 -> HomeScreen(
                                    viewModel = viewModel,
                                    onAnimeClick = { anime -> navigateTo(AppRoute.Detail(anime.id)) },
                                    onScheduleClick = { navigateTo(AppRoute.Schedule) },
                                    onSettingsClick = { navigateTo(AppRoute.Settings) },
                                    onContinueWatchClick = { historyItem ->
                                        // Load detailed object from historical logs
                                        viewModel.loadDetail(historyItem.animeId.substringAfter(":"))
                                        // Fallback mapped detailed anime reference
                                        val mappedDetail = AnimeDetail(
                                            id = historyItem.animeId.substringAfter(":"),
                                            title = historyItem.animeTitle,
                                            description = "",
                                            thumbnail = historyItem.thumbnail,
                                            rating = "",
                                            status = "",
                                            type = "",
                                            genres = emptyList(),
                                            episodes = emptyList(),
                                            source = historyItem.source
                                        )
                                        val mappedEpisode = EpisodeItem(
                                            id = historyItem.episodeId.substringAfter(":"),
                                            title = historyItem.episodeTitle,
                                            number = "",
                                            date = "",
                                            source = historyItem.source
                                        )
                                        navigateTo(AppRoute.Player(mappedEpisode, mappedDetail))
                                    }
                                )

                                1 -> ExploreScreen(
                                    viewModel = viewModel,
                                    onAnimeClick = { anime -> navigateTo(AppRoute.Detail(anime.id)) }
                                )

                                2 -> BookmarkScreen(
                                    viewModel = viewModel,
                                    onAnimeClick = { anime -> navigateTo(AppRoute.Detail(anime.id)) }
                                )

                                3 -> ProfileScreen(
                                    viewModel = viewModel,
                                    onAnimeClick = { anime -> navigateTo(AppRoute.Detail(anime.id)) },
                                    onHistoryClick = { historyItem ->
                                        val mappedDetail = AnimeDetail(
                                            id = historyItem.animeId.substringAfter(":"),
                                            title = historyItem.animeTitle,
                                            description = "",
                                            thumbnail = historyItem.thumbnail,
                                            rating = "",
                                            status = "",
                                            type = "",
                                            genres = emptyList(),
                                            episodes = emptyList(),
                                            source = historyItem.source
                                        )
                                        val mappedEpisode = EpisodeItem(
                                            id = historyItem.episodeId.substringAfter(":"),
                                            title = historyItem.episodeTitle,
                                            number = "",
                                            date = "",
                                            source = historyItem.source
                                        )
                                        navigateTo(AppRoute.Player(mappedEpisode, mappedDetail))
                                    }
                                )
                            }

                            // Custom Floating pill bottom navigation bar
                            FloatingBottomNavigation(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                            )
                        }
                    }

                    is AppRoute.Schedule -> {
                        ScheduleScreen(
                            viewModel = viewModel,
                            onBackClick = { navigateBack() },
                            onAnimeClick = { anime -> navigateTo(AppRoute.Detail(anime.id)) }
                        )
                    }

                    is AppRoute.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = { navigateBack() }
                        )
                    }

                    is AppRoute.Detail -> {
                        DetailScreen(
                            animeId = route.animeId,
                            viewModel = viewModel,
                            onBackClick = { navigateBack() },
                            onEpisodeClick = { episode, detail ->
                                navigateTo(AppRoute.Player(episode, detail))
                            }
                        )
                    }

                    is AppRoute.Player -> {
                        PlayerScreen(
                            episode = route.episode,
                            animeDetail = route.animeDetail,
                            viewModel = viewModel,
                            onBackClick = { navigateBack() },
                            onNextEpisode = { nextEp ->
                                navigateTo(AppRoute.Player(nextEp, route.animeDetail))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, Icons.Outlined.Home),
        NavigationItem("Explore", Icons.Default.Explore, Icons.Outlined.Explore),
        NavigationItem("Bookmark", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
        NavigationItem("Profile", Icons.Default.Person, Icons.Outlined.Person)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(32.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isActive = selectedTab == index
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.94f,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(if (isActive) 40.dp else 34.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isActive) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.title,
                            tint = if (isActive) Color.Black else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
