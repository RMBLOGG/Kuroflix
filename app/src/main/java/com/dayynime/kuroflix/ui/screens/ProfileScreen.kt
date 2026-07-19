package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
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
import com.dayynime.kuroflix.data.local.WatchHistoryEntity
import com.dayynime.kuroflix.data.model.AnimeItem
import com.dayynime.kuroflix.ui.components.AnimeCard
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.AuthUiState

@Composable
fun ProfileScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeItem) -> Unit,
    onLoginClick: () -> Unit = {},
    onHistoryClick: (WatchHistoryEntity) -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val historyList by viewModel.history.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val uniqueAnimeCount = historyList.distinctBy { it.animeId }.size

    // Count-up animations when the page is opened
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        startAnimation = true
    }

    val animatedAnimeCount by animateIntAsState(
        targetValue = if (startAnimation) uniqueAnimeCount else 0,
        animationSpec = tween(1200),
        label = "anime_count"
    )
    val animatedEpisodeCount by animateIntAsState(
        targetValue = if (startAnimation) historyList.size else 0,
        animationSpec = tween(1200),
        label = "episodes_count"
    )

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val backdropImage = bookmarks.firstOrNull()?.thumbnail ?: historyList.firstOrNull()?.thumbnail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Banner backdrop dengan avatar overlap -- ala reference: gambar besar di atas,
        // avatar bulat "menembus" garis bawah banner, nama + handle + stat di bawahnya.
        // Semua ditaruh dalam satu Box (tinggi tetap) biar overlap-nya rapi tanpa
        // ninggalin ruang kosong ke elemen berikutnya.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .align(Alignment.TopCenter)
            ) {
                if (backdropImage != null) {
                    AsyncImage(
                        model = backdropImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(DarkSurfaceVariant))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    DarkBg
                                )
                            )
                        )
                )
            }

            // Tombol Masuk/Keluar kecil, nempel di pojok kanan atas banner --
            // gak makan tempat sendiri, jadi bagian dari header yang sama.
            val loggedInUser = (authState as? AuthUiState.LoggedIn)?.user
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable {
                        if (loggedInUser != null) viewModel.logout() else onLoginClick()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = if (loggedInUser != null) "Keluar" else "Masuk dengan Google",
                    color = Color.White,
                    style = Typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(2.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUrl = loggedInUser?.avatarUrl
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
                            val initials = (loggedInUser?.name ?: loggedInUser?.email)
                                ?.trim()
                                ?.firstOrNull()
                                ?.uppercaseChar()
                                ?.toString() ?: "KF"
                            Text(
                                text = initials,
                                color = Color.White,
                                style = Typography.displayLarge,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = loggedInUser?.name ?: loggedInUser?.email ?: "Kuroflix User",
                    color = Color.White,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = loggedInUser?.email ?: "@kuroflix",
                    color = GoldAccent,
                    style = Typography.labelSmall
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stat row -- mirip layout followers/watched/following di referensi
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(value = bookmarks.size.toString(), label = "Bookmark")
                    ProfileStat(value = animatedAnimeCount.toString(), label = "Anime Watch")
                    ProfileStat(value = animatedEpisodeCount.toString(), label = "Episode")
                }
            }
        }

        // Section Bookmark Saya (styled as "Favorite Shows" a la referensi)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorite Shows",
                color = Color.White,
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${bookmarks.size} Anime",
                color = TextSecondary,
                style = Typography.labelSmall
            )
        }

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp)
                    .background(DarkSurface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada anime yang di-bookmark.",
                    color = TextMuted,
                    style = Typography.bodyMedium
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarks) { anime ->
                    AnimeCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }

        // Section Riwayat Nonton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = NeonBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Riwayat Tontonan",
                    color = Color.White,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (historyList.isNotEmpty()) {
                IconButton(onClick = { showClearHistoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Hapus Riwayat",
                        tint = RedAccent
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp)
                    .background(DarkSurface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat tontonan.",
                    color = TextMuted,
                    style = Typography.bodyMedium
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { item ->
                    HistoryItemCard(item, onHistoryClick)
                }
            }
        }

        // Section Preferences and settings Info
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Informasi",
                        tint = OrangeAccent
                    )
                    Text(
                        text = "Tentang Kuroflix",
                        color = Color.White,
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kuroflix adalah aplikasi client offline-first untuk menyaksikan ribuan anime dan donghua favorit langsung di smartphone Android-mu. Desain Material 3 Dark beranimasi penuh.",
                    color = TextSecondary,
                    style = Typography.bodyMedium,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Versi: 1.0.0 Stable (Non-Cloud)",
                    color = TextMuted,
                    style = Typography.labelSmall
                )
            }
        }
    }

    // Confirmation dialog for clearing history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Hapus Semua Riwayat?") },
            text = { Text("Tindakan ini tidak bisa dibatalkan. Seluruh riwayat tontonan lokalmu akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    showClearHistoryDialog = false
                }) {
                    Text("Hapus", color = RedAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = TextSecondary
        )
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            style = Typography.labelSmall,
            fontSize = 11.sp
        )
    }
}

@Composable
fun HistoryItemCard(
    item: WatchHistoryEntity,
    onClick: (WatchHistoryEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBg)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = item.animeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.animeTitle,
                    color = Color.White,
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
