package com.dayynime.kuroflix.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayynime.kuroflix.data.model.AnimeItem
import com.dayynime.kuroflix.ui.components.AnimeCard
import com.dayynime.kuroflix.ui.components.FireGradient
import com.dayynime.kuroflix.ui.components.ShimmerPosterItem
import com.dayynime.kuroflix.ui.theme.*
import com.dayynime.kuroflix.ui.viewmodel.AnimeViewModel
import com.dayynime.kuroflix.ui.viewmodel.HomeUiState

@Composable
fun ExploreScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeItem) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchLoading by viewModel.searchLoading.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val genreAnimeList by viewModel.genreAnimeList.collectAsState()
    val homeUiState by viewModel.homeUiState.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

    // Auto-search begitu user ngetik (kayak Aniku), gak perlu pencet tombol
    // search di keyboard. Dikasih sedikit debounce (350ms) biar gak nembak API
    // tiap 1 huruf diketik.
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            kotlinx.coroutines.delay(350)
            viewModel.search()
        }
    }
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categoryAnimeList by viewModel.categoryAnimeList.collectAsState()
    val categoryLoading by viewModel.categoryLoading.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Rounded Pill Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it); if (it.isNotEmpty()) viewModel.selectCategory(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                placeholder = {
                    Text(
                        text = "Cari anime atau donghua...",
                        color = TextSecondary,
                        style = Typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.selectGenre(null)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    disabledContainerColor = DarkSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.search()
                    focusManager.clearFocus()
                })
            )
        }

        // Category Tabs: Ongoing / Completed / Movie / Terbaru
        // "movies" gak ada di Donghua, jadi tab-nya di-disable buat source itu.
        val categoryTabs = listOf(
            "ongoing" to "Ongoing",
            "completed" to "Completed",
            "movies" to "Movie",
            "latest" to "Terbaru"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryTabs) { (value, label) ->
                val isActive = selectedCategory == value
                val isDisabled = value == "movies" && selectedSource == "donghua"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isActive) FireGradient else Brush.linearGradient(listOf(DarkSurface, DarkSurface)))
                        .clickable(enabled = !isDisabled) {
                            viewModel.onSearchQueryChanged("")
                            viewModel.selectGenre(null)
                            viewModel.selectCategory(if (isActive) null else value)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = when {
                            isDisabled -> TextMuted
                            isActive -> Color.White
                            else -> TextSecondary
                        },
                        style = Typography.labelLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Horizontal Genres Filter Bar
        if (genres.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAllActive = selectedGenre == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isAllActive) FireGradient else Brush.linearGradient(listOf(DarkSurface, DarkSurface)))
                            .clickable { viewModel.selectGenre(null) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Semua Genre",
                            color = if (isAllActive) Color.White else TextSecondary,
                            style = Typography.labelLarge,
                            fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                items(genres) { genre ->
                    val isActive = selectedGenre?.slug == genre.slug
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isActive) FireGradient else Brush.linearGradient(listOf(DarkSurface, DarkSurface)))
                            .clickable { viewModel.selectCategory(null); viewModel.selectGenre(genre) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = genre.title,
                            color = if (isActive) Color.White else TextSecondary,
                            style = Typography.labelLarge,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Search Content / Grid Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            when {
                categoryLoading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(12) {
                            Box(modifier = Modifier.padding(2.dp)) {
                                ShimmerPosterItem()
                            }
                        }
                    }
                }

                selectedCategory != null -> {
                    if (categoryAnimeList.isEmpty()) {
                        EmptyStateView(
                            title = if (selectedCategory == "movies" && selectedSource == "donghua")
                                "Donghua gak punya kategori Movie."
                            else "Tidak ada hasil untuk kategori ini."
                        )
                    } else {
                        val categoryHasNext by viewModel.categoryHasNext.collectAsState()
                        val categoryLoadingMore by viewModel.categoryLoadingMore.collectAsState()
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(gridState, categoryAnimeList.size) {
                            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                .collect { lastVisible ->
                                    if (lastVisible != null && lastVisible >= categoryAnimeList.size - 6) {
                                        viewModel.loadMoreCategory()
                                    }
                                }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categoryAnimeList) { anime ->
                                AnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                            }
                            if (categoryHasNext) {
                                item(span = { GridItemSpan(3) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (categoryLoadingMore) {
                                            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                searchLoading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(12) {
                            Box(modifier = Modifier.padding(2.dp)) {
                                ShimmerPosterItem()
                            }
                        }
                    }
                }

                selectedGenre != null -> {
                    // Genre list Succesful result
                    if (genreAnimeList.isEmpty()) {
                        EmptyStateView(title = "Tidak ada hasil untuk genre ini.")
                    } else {
                        val genreHasNext by viewModel.genreHasNext.collectAsState()
                        val genreLoadingMore by viewModel.genreLoadingMore.collectAsState()
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(gridState, genreAnimeList.size) {
                            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                .collect { lastVisible ->
                                    if (lastVisible != null && lastVisible >= genreAnimeList.size - 6) {
                                        viewModel.loadMoreGenreAnime()
                                    }
                                }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(genreAnimeList) { anime ->
                                AnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                            }
                            if (genreHasNext) {
                                item(span = { GridItemSpan(3) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (genreLoadingMore) {
                                            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                query.isNotEmpty() -> {
                    // Search successful result
                    if (searchResults.isEmpty()) {
                        EmptyStateView(title = "Tidak ada hasil pencarian.")
                    } else {
                        val searchHasNext by viewModel.searchHasNext.collectAsState()
                        val searchLoadingMore by viewModel.searchLoadingMore.collectAsState()
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(gridState, searchResults.size) {
                            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                .collect { lastVisible ->
                                    if (lastVisible != null && lastVisible >= searchResults.size - 6) {
                                        viewModel.loadMoreSearch()
                                    }
                                }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(searchResults) { anime ->
                                AnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                            }
                            if (searchHasNext) {
                                item(span = { GridItemSpan(3) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (searchLoadingMore) {
                                            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Empty state dashboard (Shows popular from home as recommendation)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Mau nonton apa hari ini?",
                            color = Color.White,
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 12.dp)
                        )

                        when (val state = homeUiState) {
                            is HomeUiState.Success -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(state.popular) { anime ->
                                        AnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                                    }
                                }
                            }
                            else -> {
                                EmptyStateView(title = "Jelajahi ribuan anime & donghua favoritmu.")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = TextSecondary,
            style = Typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        )
    }
}
