package com.dayynime.kuroflix.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.dayynime.kuroflix.data.network.SupabaseConfig
import com.dayynime.kuroflix.ui.theme.DarkBg
import com.dayynime.kuroflix.ui.theme.OrangeAccent
import com.dayynime.kuroflix.ui.theme.TextSecondary
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
    onLoggedIn: () -> Unit
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

    Scaffold(containerColor = DarkBg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Masuk ke Kuroflix",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sinkronkan bookmark & riwayat tontonan kamu lewat akun Google.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = authState) {
                    is AuthUiState.Loading -> {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                    else -> {
                        Button(
                            onClick = { startGoogleSignIn() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(text = "Lanjutkan dengan Google", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                val displayedError = errorMessage ?: (authState as? AuthUiState.Error)?.message
                if (displayedError != null) {
                    Text(
                        text = displayedError,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp
                    )
                }

                TextButton(onClick = onBackClick) {
                    Text(text = "Nanti saja", color = TextSecondary)
                }
            }
        }
    }
}
