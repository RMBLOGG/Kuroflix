package com.dayynime.kuroflix.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.dayynime.kuroflix.R
import com.dayynime.kuroflix.data.network.SupabaseConfig
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
import com.dayynime.kuroflix.ui.theme.Typography
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.AuthUiState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onLoggedIn: () -> Unit,
    allowSkip: Boolean = true
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun startGoogleSignIn() {
        errorMessage = null
        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(SupabaseConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context = context, request = request)

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken) {
                        onLoggedIn()
                    }
                } else {
                    errorMessage = "Tipe kredensial tidak dikenali."
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginScreen", "Gagal ambil credential Google", e)
                errorMessage = "Gagal login: ${e.message ?: "dibatalkan atau tidak ada akun Google."}"
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("LoginScreen", "Gagal parsing ID token Google", e)
                errorMessage = "Gagal membaca token Google."
            }
        }
    }

    val infinite = rememberInfiniteTransition(label = "login_backdrop")
    val breathe by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Backdrop cahaya senada sama scene terakhir onboarding, biar transisinya nyambung.
        val softGlow = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to OrangeAccent.copy(alpha = 0.35f),
                0.35f to OrangeAccent.copy(alpha = 0.20f),
                0.7f to OrangeAccent.copy(alpha = 0.08f),
                1.0f to Color.Transparent
            )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80 + breathe * 20).dp)
                .background(softGlow, shape = CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_kuroflix),
                    contentDescription = "Kuroflix",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Masuk ke Kuroflix",
                color = Color.White,
                style = Typography.displayLarge,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Sinkronkan bookmark & riwayat tontonan kamu lewat akun Google.",
                color = TextSecondary,
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            when (authState) {
                is AuthUiState.Loading -> {
                    CircularProgressIndicator(color = OrangeAccent)
                }
                else -> {
                    Surface(
                        onClick = { startGoogleSignIn() },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Lanjutkan dengan Google",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            val displayedError = errorMessage ?: (authState as? AuthUiState.Error)?.message
            AnimatedVisibility(visible = displayedError != null, enter = fadeIn()) {
                Text(
                    text = displayedError ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (allowSkip) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = onBackClick) {
                    Text(text = "Nanti saja", color = TextSecondary)
                }
            }
        }
    }
}
