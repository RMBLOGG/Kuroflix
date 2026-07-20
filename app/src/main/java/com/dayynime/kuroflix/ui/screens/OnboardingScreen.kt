package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.GoldAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
import com.dayynime.kuroflix.ui.theme.Typography
import kotlin.math.absoluteValue

private data class Scene(
    val icon: ImageVector?,
    val eyebrow: String,
    val title: String,
    val description: String,
    val glow: Color
)

private val scenes = listOf(
    Scene(
        icon = Icons.Filled.PlayCircle,
        eyebrow = "STREAMING",
        title = "Nonton Anime\nFavoritmu",
        description = "Ribuan judul sub Indo, update episode terbaru tiap hari dari beberapa sumber sekaligus.",
        glow = Color(0xFF9B5DE5)
    ),
    Scene(
        icon = Icons.Filled.Bookmark,
        eyebrow = "KOLEKSI",
        title = "Bookmark &\nRiwayat Tontonan",
        description = "Simpan favorit dan lanjut nonton dari episode terakhir, kapan aja, di device mana aja.",
        glow = Color(0xFFB44FD1)
    ),
    Scene(
        icon = Icons.Filled.NotificationsActive,
        eyebrow = "JADWAL",
        title = "Update Rilis\nTerbaru",
        description = "Pantau jadwal tayang anime ongoing biar gak ketinggalan satu episode pun.",
        glow = Color(0xFF6E6BE0)
    ),
    Scene(
        icon = null,
        eyebrow = "SIAP?",
        title = "Kuroflix\nMenantimu",
        description = "Masuk sekali, dan semua koleksi tontonanmu ikut ke mana pun kamu buka Kuroflix.",
        glow = Color(0xFFE0B357)
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { scenes.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val scene = scenes[page]
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).coerceIn(-1f, 1f)

            SceneBackdrop(glow = scene.glow, pageOffset = pageOffset)

            SceneContent(
                scene = scene,
                pageOffset = pageOffset,
                isSettledHere = pagerState.settledPage == page,
                isFinalScene = page == scenes.lastIndex,
                onStart = onFinish
            )
        }

        // Penanda halaman "01 / 04" -- numerik karena ini memang urutan scene.
        Text(
            text = "${(pagerState.currentPage + 1).toString().padStart(2, '0')} / ${scenes.size.toString().padStart(2, '0')}",
            color = TextSecondary,
            style = Typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 48.dp)
        )

        // Indikator progres vertikal di tepi kanan -- gantiin fungsi tombol "Lanjut",
        // sekaligus nunjukin posisi scroll saat ini.
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            scenes.indices.forEach { index ->
                val active = pagerState.currentPage == index
                val height by animateDpAsState(
                    targetValue = if (active) 22.dp else 7.dp,
                    label = "dot_height"
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(height)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) scenes[index].glow else TextSecondary.copy(alpha = 0.25f))
                )
            }
        }
    }
}

/** Backdrop bola cahaya lembut yang perlahan "bernapas", warnanya beda tiap scene. */
@Composable
private fun SceneBackdrop(glow: Color, pageOffset: Float) {
    val infinite = rememberInfiniteTransition(label = "backdrop")
    val breathe by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Backdrop bergerak lebih pelan dari konten -- lapisan parallax belakang.
                translationY = pageOffset * size.height * 0.15f
            }
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = (40 + breathe * 20).dp, y = (-60 + breathe * 30).dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30 - breathe * 15).dp, y = (40 - breathe * 20).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SceneContent(
    scene: Scene,
    pageOffset: Float,
    isSettledHere: Boolean,
    isFinalScene: Boolean,
    onStart: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(isSettledHere) {
        if (isSettledHere) {
            revealed = false
            kotlinx.coroutines.delay(80)
            revealed = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .graphicsLayer {
                // Konten ikut geser & memudar mengikuti posisi scroll asli --
                // bikin transisi terasa hidup, bukan potong-tempel per halaman.
                translationY = pageOffset * size.height * 0.35f
                alpha = 1f - pageOffset.absoluteValue
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (scene.icon != null) {
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(350)) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                IconMedallion(icon = scene.icon, glow = scene.glow)
            }
            Spacer(modifier = Modifier.height(36.dp))
        } else {
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.85f)
            ) {
                Text(
                    text = "薫",
                    color = scene.glow,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(350, delayMillis = 90)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(350, delayMillis = 90)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = scene.eyebrow,
                    color = scene.glow,
                    style = Typography.labelSmall,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = scene.title,
                    color = Color.White,
                    style = Typography.displayLarge,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(350, delayMillis = 160)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(350, delayMillis = 160)
            )
        ) {
            Text(
                text = scene.description,
                color = TextSecondary,
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isFinalScene) {
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(350, delayMillis = 220)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(350, delayMillis = 220)
                )
            ) {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(text = "Mulai", fontWeight = FontWeight.Bold, color = DarkBg)
                }
            }
        } else {
            AnimatedVisibility(visible = revealed, enter = fadeIn(tween(400, delayMillis = 220))) {
                ScrollUpHint(glow = scene.glow)
            }
        }
    }
}

@Composable
private fun IconMedallion(icon: ImageVector, glow: Color) {
    val infinite = rememberInfiniteTransition(label = "ring")
    val ringScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_scale"
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .graphicsLayer { scaleX = ringScale; scaleY = ringScale; alpha = ringAlpha }
                .clip(CircleShape)
                .background(glow.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(colors = listOf(glow.copy(alpha = 0.28f), glow.copy(alpha = 0.08f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = glow, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun ScrollUpHint(glow: Color) {
    val infinite = rememberInfiniteTransition(label = "hint")
    val bounce by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = glow,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { translationY = bounce }
        )
        Text(text = "Geser ke atas", color = TextSecondary, style = Typography.labelSmall)
    }
}
