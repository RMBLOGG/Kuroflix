package com.dayynime.kuroflix.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.dayynime.kuroflix.ui.theme.DarkSurface
import com.dayynime.kuroflix.ui.theme.GoldAccent
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.ProfileUpdateState

@Composable
fun EditProfileDialog(
    viewModel: AnimeViewModel,
    currentName: String?,
    currentAvatarUrl: String?,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName ?: "") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    val updateState by viewModel.profileUpdateState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pickedImageUri = uri }

    // Sukses -> tutup dialog otomatis & reset state biar gak nyangkut kalau dialog dibuka lagi.
    LaunchedEffect(updateState) {
        if (updateState is ProfileUpdateState.Success) {
            viewModel.resetProfileUpdateState()
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profil",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            photoPicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        pickedImageUri != null -> AsyncImage(
                            model = pickedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        !currentAvatarUrl.isNullOrBlank() -> AsyncImage(
                            model = currentAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        else -> Text(
                            text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Ganti foto",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = OrangeAccent,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = OrangeAccent,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                if (updateState is ProfileUpdateState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (updateState as ProfileUpdateState.Error).message,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                newName = name.takeIf { it.isNotBlank() && it != currentName },
                                newAvatarUri = pickedImageUri
                            )
                        },
                        enabled = updateState !is ProfileUpdateState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (updateState is ProfileUpdateState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Simpan")
                        }
                    }
                }
            }
        }
    }
}
