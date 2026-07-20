package com.dayynime.kuroflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dayynime.kuroflix.data.network.ChatMessage
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.DarkSurface
import com.dayynime.kuroflix.ui.theme.DarkSurfaceVariant
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.AuthUiState

@Composable
fun ChatScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val chatError by viewModel.chatError.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val myUserId = (authState as? AuthUiState.LoggedIn)?.user?.id

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Mulai polling begitu layar ini dibuka, berhenti begitu ditutup --
    // biar gak nguras baterai/egress pas user lagi nonton anime, bukan chat.
    DisposableEffect(Unit) {
        viewModel.startChatPolling()
        onDispose { viewModel.stopChatPolling() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Column {
                    Text(text = "Chat Room", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 17.sp)
                    Text(text = "Ngobrol bareng sesama penonton Kuroflix", color = TextSecondary, fontSize = 11.sp)
                }
            }
        },
        bottomBar = {
            Column {
                if (chatError != null) {
                    Text(
                        text = chatError ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { if (it.length <= 500) input = it },
                        placeholder = { Text("Tulis pesan...", color = TextSecondary) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.sendChatMessage(input)
                                input = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Kirim", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        if (messages.isEmpty() && chatError == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Belum ada obrolan. Jadi yang pertama!", color = TextSecondary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages, key = { _, m -> m.id ?: "${m.user_id}-${m.created_at}" }) { _, msg ->
                    ChatBubble(msg = msg, isMine = msg.user_id == myUserId)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            AvatarBubble(url = msg.user_avatar, name = msg.user_name)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            if (!isMine) {
                Text(
                    text = msg.user_name ?: "Pengguna",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMine) 14.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 14.dp
                        )
                    )
                    .background(if (isMine) OrangeAccent else DarkSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(text = msg.message, color = Color.White, fontSize = 14.sp)
            }
        }
        if (isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarBubble(url = msg.user_avatar, name = msg.user_name)
        }
    }
}

@Composable
private fun AvatarBubble(url: String?, name: String?) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
