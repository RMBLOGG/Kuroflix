package com.dayynime.kuroflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dayynime.kuroflix.ui.screens.MainScreen
import com.dayynime.kuroflix.ui.theme.KuroflixTheme
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuroflixTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val padding = innerPadding
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
