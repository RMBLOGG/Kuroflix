package com.dayynime.kuroflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayynime.kuroflix.ui.components.AnimeCard
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.components.ShimmerPosterItem
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.data.model.AnimeItem

private val DAYS = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

@Composable
fun ScheduleScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onAnimeClick: (AnimeItem) -> Unit
) {
    val selectedSource by viewModel.selectedSource.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val scheduleMap by viewModel.scheduleMap.collectAsState()
    val scheduleLoading by viewModel.scheduleLoading.collectAsState()

    // Reload jadwal tiap ganti source (Animasu/Samehadaku/Animekompi/Donghua)
    LaunchedEffect(selectedSource) {
        viewModel.loadSchedule()
    }

    val animeToday = scheduleMap[selectedDay].orEmpty()

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = androidx.compose.ui.graphics.Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Jadwal Rilis",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold,
                style = Typography.titleLarge
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DAYS) { day ->
                val isActive = day == selectedDay
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isActive) FireGradient
                            else androidx.compose.ui.graphics.Brush.linearGradient(listOf(DarkSurface, DarkSurface))
                        )
                        .clickable { viewModel.selectDay(day) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = day,
                        color = if (isActive) androidx.compose.ui.graphics.Color.White else TextSecondary,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        style = Typography.labelLarge
                    )
                }
            }
        }

        when {
            scheduleLoading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(9) {
                        Box(modifier = Modifier.padding(2.dp)) { ShimmerPosterItem() }
                    }
                }
            }
            animeToday.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gak ada anime yang rilis hari $selectedDay.",
                        color = TextSecondary,
                        style = Typography.bodyMedium
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animeToday) { anime ->
                        AnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                    }
                }
            }
        }
    }
}
