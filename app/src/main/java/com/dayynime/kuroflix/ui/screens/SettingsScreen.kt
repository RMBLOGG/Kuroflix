package com.dayynime.kuroflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel

@Composable
fun SettingsScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit
) {
    val autoplayEnabled by viewModel.autoplayNextEpisode.collectAsState()
    val defaultSource by viewModel.defaultSource.collectAsState()
    val currentSource by viewModel.selectedSource.collectAsState()

    // Kalau belum pernah diset, tampilin sumber yang lagi aktif sekarang sebagai
    // "terpilih" secara visual, biar radio-nya gak keliatan kosong.
    val effectiveDefault = defaultSource.ifBlank { currentSource }

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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Text(
                text = "Pengaturan",
                color = Color.White,
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section: Pemutaran
            SettingsSectionLabel("Pemutaran")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Putar Episode Berikutnya Otomatis",
                            color = Color.White,
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Lanjut otomatis begitu episode ini selesai",
                            color = TextSecondary,
                            style = Typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = autoplayEnabled,
                    onCheckedChange = { viewModel.setAutoplayNextEpisode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TextMuted.copy(alpha = 0.4f),
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section: Sumber Default
            SettingsSectionLabel("Sumber Streaming Default")
            Text(
                text = "Dipakai otomatis tiap kali app dibuka, gak perlu pilih ulang.",
                color = TextSecondary,
                style = Typography.labelSmall,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
            ) {
                sources.forEachIndexed { index, (key, label) ->
                    val isSelected = effectiveDefault == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDefaultSource(key) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Source,
                                contentDescription = null,
                                tint = if (isSelected) GoldAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextSecondary,
                                style = Typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                            )
                        }
                    }
                    if (index != sources.lastIndex) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            SettingsSectionLabel("Tentang")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Kuroflix",
                    color = Color.White,
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Versi 1.0.0 Stable (Non-Cloud)",
                    color = TextMuted,
                    style = Typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        style = Typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
    )
}
