package com.dayynime.kuroflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dayynime.kuroflix.data.local.WatchHistoryEntity
import com.dayynime.kuroflix.data.model.*
import com.dayynime.kuroflix.data.network.VideoExtractor
import com.dayynime.kuroflix.data.repository.AnimeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val latest: List<AnimeItem>,
        val popular: List<AnimeItem>,
        val ongoing: List<AnimeItem>,
        val completed: List<AnimeItem>,
        val movies: List<AnimeItem>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val detail: AnimeDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface PlayerUiState {
    object Idle : PlayerUiState
    object Loading : PlayerUiState
    data class Success(
        val servers: List<VideoServer>,
        val selectedServer: VideoServer?,
        val videoSource: VideoSource
    ) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}

class AnimeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AnimeRepository(application)

    // Current global source selection (animasu, samehadaku, animekompi, donghua)
    private val _selectedSource = MutableStateFlow("animasu")
    val selectedSource: StateFlow<String> = _selectedSource.asStateFlow()

    // Home screen UI state
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // Detail screen UI state
    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    // Player screen UI state
    private val _playerUiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    // Explore / Search UI State
    val searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<AnimeItem>>(emptyList())
    val searchResults: StateFlow<List<AnimeItem>> = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    // Genre state
    private val _genres = MutableStateFlow<List<GenreItem>>(emptyList())
    val genres: StateFlow<List<GenreItem>> = _genres.asStateFlow()

    private val _selectedGenre = MutableStateFlow<GenreItem?>(null)
    val selectedGenre: StateFlow<GenreItem?> = _selectedGenre.asStateFlow()

    private val _genreAnimeList = MutableStateFlow<List<AnimeItem>>(emptyList())
    val genreAnimeList: StateFlow<List<AnimeItem>> = _genreAnimeList.asStateFlow()

    // Local Data states
    val bookmarks: StateFlow<List<AnimeItem>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<WatchHistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load initial home page and genres
        loadHome()
        loadGenres()
    }

    fun setSource(source: String) {
        if (_selectedSource.value != source) {
            _selectedSource.value = source
            loadHome()
            loadGenres()
            _searchResults.value = emptyList()
            searchQuery.value = ""
            _selectedGenre.value = null
            _genreAnimeList.value = emptyList()
        }
    }

    fun loadHome() {
        viewModelScope.launch {
            _homeUiState.value = HomeUiState.Loading
            repository.getHome(_selectedSource.value)
                .catch { e ->
                    Log.e("AnimeViewModel", "Error loading home", e)
                    _homeUiState.value = HomeUiState.Error(e.message ?: "Unknown network error")
                }
                .collect { homeData ->
                    _homeUiState.value = HomeUiState.Success(
                        latest = homeData.latest,
                        popular = homeData.popular,
                        ongoing = homeData.ongoing,
                        completed = homeData.completed,
                        movies = homeData.movies
                    )
                }
        }
    }

    fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres(_selectedSource.value)
                .catch { Log.e("AnimeViewModel", "Error loading genres", it) }
                .collect {
                    _genres.value = it
                }
        }
    }

    fun selectGenre(genre: GenreItem?) {
        _selectedGenre.value = genre
        if (genre == null) {
            _genreAnimeList.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searchLoading.value = true
            repository.getGenreAnime(_selectedSource.value, genre.slug)
                .catch { e ->
                    Log.e("AnimeViewModel", "Error loading genre anime", e)
                    _searchLoading.value = false
                }
                .collect {
                    _genreAnimeList.value = it
                    _searchLoading.value = false
                }
        }
    }

    fun loadDetail(slug: String) {
        viewModelScope.launch {
            _detailUiState.value = DetailUiState.Loading
            repository.getDetail(_selectedSource.value, slug)
                .catch { e ->
                    Log.e("AnimeViewModel", "Error loading details", e)
                    _detailUiState.value = DetailUiState.Error(e.message ?: "Unknown error")
                }
                .collect { detail ->
                    _detailUiState.value = DetailUiState.Success(detail)
                }
        }
    }

    fun search() {
        val q = searchQuery.value
        if (q.isEmpty()) return
        viewModelScope.launch {
            _searchLoading.value = true
            _selectedGenre.value = null
            _genreAnimeList.value = emptyList()
            repository.search(_selectedSource.value, q)
                .catch { e ->
                    Log.e("AnimeViewModel", "Search error", e)
                    _searchLoading.value = false
                }
                .collect {
                    _searchResults.value = it
                    _searchLoading.value = false
                }
        }
    }

    // Episode & Video resolution
    fun loadVideoSource(episodeId: String, animeDetail: AnimeDetail) {
        viewModelScope.launch {
            _playerUiState.value = PlayerUiState.Loading
            repository.getEpisode(_selectedSource.value, episodeId)
                .catch { e ->
                    Log.e("AnimeViewModel", "Error loading episode info", e)
                    _playerUiState.value = PlayerUiState.Error("Gagal mengambil server video: ${e.message}")
                }
                .collect { servers ->
                    if (servers.isEmpty()) {
                        _playerUiState.value = PlayerUiState.Error("Tidak ada server video tersedia.")
                        return@collect
                    }
                    // Select first server as default
                    selectServer(servers.first(), servers, animeDetail)
                }
        }
    }

    fun selectServer(server: VideoServer, servers: List<VideoServer>, animeDetail: AnimeDetail) {
        viewModelScope.launch {
            _playerUiState.value = PlayerUiState.Loading
            
            // Check if Samehadaku server needs further resolution
            var finalEmbedUrl = server.embedUrl
            if (_selectedSource.value == "samehadaku" && !finalEmbedUrl.startsWith("http")) {
                // If the link is a server ID, fetch the resolved embedUrl from the Samehadaku API
                val resolvedEmbed = withContext(Dispatchers.IO) {
                    repository.getSamehadakuServerVideo(finalEmbedUrl)
                }
                if (resolvedEmbed.isNotEmpty()) {
                    finalEmbedUrl = resolvedEmbed
                }
            }

            try {
                // Resolve direct link — WAJIB di Dispatchers.IO karena extractor-extractor
                // di dalam VideoExtractor (mp4upload, blogger, packed-JS, dst) ngelakuin
                // blocking network call (OkHttp .execute()) yang bakal crash
                // NetworkOnMainThreadException kalau dijalanin di Main/viewModelScope
                // langsung. Sebelum ini di-fix, crash-nya ke-catch diam-diam di dalam
                // VideoExtractor sendiri, jadi kelihatannya cuma "gagal resolve" biasa
                // padahal sebenarnya crash thread - makanya SEMUA host yang butuh fetch
                // HTML gagal, cuma yang punya fast-path (mis. wibufile direct .mp4/.m3u8)
                // yang lolos karena gak butuh network call sama sekali.
                // Referer: cuma Samehadaku yang punya domain pasti (persis dari Aniku,
                // udah kebukti bener). 3 source lain sengaja di-null — VideoExtractor
                // sendiri per-host udah punya fallback yang lebih akurat (ok.ru/rumble/
                // blogger hardcode referer-nya sendiri, host lain fallback pakai embedUrl
                // itu sendiri sebagai referer) daripada nebak domain situs sumber asli.
                val referer = when (_selectedSource.value) {
                    "samehadaku" -> "https://v2.samehadaku.how/"
                    else -> null
                }
                val videoSource = withContext(Dispatchers.IO) {
                    VideoExtractor.resolveVideoUrl(finalEmbedUrl, referer, getApplication())
                }
                _playerUiState.value = PlayerUiState.Success(
                    servers = servers,
                    selectedServer = server,
                    videoSource = videoSource
                )
            } catch (e: Exception) {
                Log.e("AnimeViewModel", "Error resolving video URL", e)
                // Fallback to the embed url as embed webview
                _playerUiState.value = PlayerUiState.Success(
                    servers = servers,
                    selectedServer = server,
                    videoSource = VideoSource(finalEmbedUrl, server.name, isEmbed = true)
                )
            }
        }
    }

    // Bookmarks operations
    fun isBookmarked(slug: String): Flow<Boolean> {
        val compositeId = "${_selectedSource.value}:$slug"
        return repository.isBookmarked(compositeId)
    }

    fun toggleBookmark(anime: AnimeItem) {
        viewModelScope.launch {
            val compositeId = "${anime.source}:${anime.id}"
            val exists = bookmarks.value.any { "${it.source}:${it.id}" == compositeId }
            if (exists) {
                repository.deleteBookmark(compositeId)
            } else {
                repository.saveBookmark(anime)
            }
        }
    }

    fun toggleBookmark(detail: AnimeDetail) {
        viewModelScope.launch {
            val compositeId = "${detail.source}:${detail.id}"
            val exists = bookmarks.value.any { "${it.source}:${it.id}" == compositeId }
            if (exists) {
                repository.deleteBookmark(compositeId)
            } else {
                repository.saveBookmark(
                    AnimeItem(
                        id = detail.id,
                        title = detail.title,
                        thumbnail = detail.thumbnail,
                        rating = detail.rating,
                        status = detail.status,
                        type = detail.type,
                        source = detail.source
                    )
                )
            }
        }
    }

    // Watch progress / History operations
    fun saveProgress(
        animeId: String,
        episodeId: String,
        animeTitle: String,
        episodeTitle: String,
        thumbnail: String,
        progressMillis: Long,
        durationMillis: Long
    ) {
        viewModelScope.launch {
            repository.saveWatchHistory(
                animeId = animeId,
                episodeId = episodeId,
                animeTitle = animeTitle,
                episodeTitle = episodeTitle,
                thumbnail = thumbnail,
                progressMillis = progressMillis,
                durationMillis = durationMillis,
                source = _selectedSource.value
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
