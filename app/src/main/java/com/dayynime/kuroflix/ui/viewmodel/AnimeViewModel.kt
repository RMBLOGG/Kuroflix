package com.dayynime.kuroflix.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dayynime.kuroflix.data.local.WatchHistoryEntity
import com.dayynime.kuroflix.data.local.PreferencesManager
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
        val movies: List<AnimeItem>,
        val upcoming: List<AnimeItem> = emptyList()
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
    private val preferencesManager = PreferencesManager(application)

    // Preferensi default server streaming, GLOBAL lintas 4 sumber data.
    // "" berarti "Otomatis" (server pertama, behavior lama).
    val preferredServerKeyword: StateFlow<String> = preferencesManager.preferredServerKeyword
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setPreferredServer(keyword: String) {
        viewModelScope.launch { preferencesManager.setPreferredServer(keyword) }
    }

    // Autoplay episode berikutnya setelah episode saat ini selesai (dipakai di PlayerScreen)
    val autoplayNextEpisode: StateFlow<Boolean> = preferencesManager.autoplayNextEpisode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setAutoplayNextEpisode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoplayNextEpisode(enabled) }
    }

    // Sumber default yang otomatis dipilih tiap app dibuka (diset dari SettingsScreen)
    val defaultSource: StateFlow<String> = preferencesManager.defaultSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setDefaultSource(source: String) {
        viewModelScope.launch { preferencesManager.setDefaultSource(source) }
        setSource(source)
    }

    // Daftar server yang BENERAN ADA buat anime yang lagi dibuka (diambil dari
    // episode pertama sebagai sampel) — dipakai buat isi dropdown "Server Default"
    // di DetailScreen, supaya cuma nampilin host yang relevan sama anime/source
    // itu, bukan daftar gabungan semua host yang mungkin gak semuanya kepake.
    private val _availableServers = MutableStateFlow<List<VideoServer>>(emptyList())
    val availableServers: StateFlow<List<VideoServer>> = _availableServers.asStateFlow()

    // Jadwal rilis mingguan
    private val _scheduleMap = MutableStateFlow<Map<String, List<AnimeItem>>>(emptyMap())
    val scheduleMap: StateFlow<Map<String, List<AnimeItem>>> = _scheduleMap.asStateFlow()
    private val _scheduleLoading = MutableStateFlow(false)
    val scheduleLoading: StateFlow<Boolean> = _scheduleLoading.asStateFlow()
    private val _selectedDay = MutableStateFlow(
        // Default: hari ini, biar buka Jadwal langsung nunjukin anime yang tayang hari ini
        listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")[
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
        ]
    )
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    fun selectDay(day: String) {
        _selectedDay.value = day
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _scheduleLoading.value = true
            withContext(Dispatchers.IO) {
                repository.getSchedule(_selectedSource.value)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading schedule", e)
                        _scheduleMap.value = emptyMap()
                    }
                    .collect { map -> _scheduleMap.value = map }
            }
            _scheduleLoading.value = false
        }
    }


    fun loadAvailableServers(animeDetail: AnimeDetail) {
        viewModelScope.launch {
            _availableServers.value = emptyList()
            val sampleEpisode = animeDetail.episodes.firstOrNull() ?: return@launch
            withContext(Dispatchers.IO) {
                repository.getEpisode(_selectedSource.value, sampleEpisode.id)
                    .catch { e -> Log.e("AnimeViewModel", "Gagal load daftar server buat dropdown", e) }
                    .collect { servers ->
                        // Server sering beda-beda kualitas dengan nama sama (mis. "Mp4Upload"
                        // muncul di beberapa episode) — cukup ambil nama unik-nya aja.
                        _availableServers.value = servers.distinctBy { it.name.lowercase() }
                    }
            }
        }
    }

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

    private val _searchPage = MutableStateFlow(1)
    private val _searchHasNext = MutableStateFlow(false)
    val searchHasNext: StateFlow<Boolean> = _searchHasNext.asStateFlow()
    private val _searchLoadingMore = MutableStateFlow(false)
    val searchLoadingMore: StateFlow<Boolean> = _searchLoadingMore.asStateFlow()

    // Genre state
    private val _genres = MutableStateFlow<List<GenreItem>>(emptyList())
    val genres: StateFlow<List<GenreItem>> = _genres.asStateFlow()

    private val _selectedGenre = MutableStateFlow<GenreItem?>(null)
    val selectedGenre: StateFlow<GenreItem?> = _selectedGenre.asStateFlow()

    private val _genreAnimeList = MutableStateFlow<List<AnimeItem>>(emptyList())
    val genreAnimeList: StateFlow<List<AnimeItem>> = _genreAnimeList.asStateFlow()

    private val _genrePage = MutableStateFlow(1)
    private val _genreHasNext = MutableStateFlow(false)
    val genreHasNext: StateFlow<Boolean> = _genreHasNext.asStateFlow()
    private val _genreLoadingMore = MutableStateFlow(false)
    val genreLoadingMore: StateFlow<Boolean> = _genreLoadingMore.asStateFlow()

    // Category browsing state (Ongoing / Completed / Movie / Terbaru).
    // null = belum ada tab kategori yang dipilih (default: tampilan Home biasa).
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _categoryAnimeList = MutableStateFlow<List<AnimeItem>>(emptyList())
    val categoryAnimeList: StateFlow<List<AnimeItem>> = _categoryAnimeList.asStateFlow()

    private val _categoryLoading = MutableStateFlow(false)
    val categoryLoading: StateFlow<Boolean> = _categoryLoading.asStateFlow()

    private val _categoryPage = MutableStateFlow(1)
    private val _categoryHasNext = MutableStateFlow(false)
    val categoryHasNext: StateFlow<Boolean> = _categoryHasNext.asStateFlow()
    private val _categoryLoadingMore = MutableStateFlow(false)
    val categoryLoadingMore: StateFlow<Boolean> = _categoryLoadingMore.asStateFlow()

    // Local Data states
    val bookmarks: StateFlow<List<AnimeItem>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<WatchHistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Kalau user pernah nyetel sumber default di Settings, langsung pakai itu
        // sebelum load home pertama kali -- biar gak perlu pilih ulang tiap buka app.
        viewModelScope.launch {
            val saved = preferencesManager.defaultSource.first()
            if (saved.isNotBlank()) {
                _selectedSource.value = saved
            }
            loadHome()
            loadGenres()
        }
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
            _selectedCategory.value = null
            _categoryAnimeList.value = emptyList()
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        _categoryPage.value = 1
        _categoryHasNext.value = false
        if (category == null) {
            _categoryAnimeList.value = emptyList()
            return
        }
        viewModelScope.launch {
            _categoryLoading.value = true
            _categoryAnimeList.value = emptyList()
            withContext(Dispatchers.IO) {
                repository.getCategory(_selectedSource.value, category, page = 1)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading category $category", e)
                        _categoryAnimeList.value = emptyList()
                    }
                    .collect { (items, hasNext) ->
                        _categoryAnimeList.value = items
                        _categoryHasNext.value = hasNext
                    }
            }
            _categoryLoading.value = false
        }
    }

    fun loadMoreCategory() {
        val category = _selectedCategory.value ?: return
        if (_categoryLoadingMore.value || !_categoryHasNext.value) return
        viewModelScope.launch {
            _categoryLoadingMore.value = true
            val nextPage = _categoryPage.value + 1
            withContext(Dispatchers.IO) {
                repository.getCategory(_selectedSource.value, category, page = nextPage)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading more category", e)
                        _categoryHasNext.value = false
                    }
                    .collect { (items, hasNext) ->
                        _categoryAnimeList.value = _categoryAnimeList.value + items
                        _categoryHasNext.value = hasNext
                        _categoryPage.value = nextPage
                    }
            }
            _categoryLoadingMore.value = false
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
                        movies = homeData.movies,
                        upcoming = homeData.upcoming
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
        _genrePage.value = 1
        _genreHasNext.value = false
        if (genre == null) {
            _genreAnimeList.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searchLoading.value = true
            _genreAnimeList.value = emptyList()
            withContext(Dispatchers.IO) {
                repository.getGenreAnime(_selectedSource.value, genre.slug, page = 1)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading genre anime", e)
                        _searchLoading.value = false
                    }
                    .collect { (items, hasNext) ->
                        _genreAnimeList.value = items
                        _genreHasNext.value = hasNext
                        _searchLoading.value = false
                    }
            }
        }
    }

    fun loadMoreGenreAnime() {
        val genre = _selectedGenre.value ?: return
        if (_genreLoadingMore.value || !_genreHasNext.value) return
        viewModelScope.launch {
            _genreLoadingMore.value = true
            val nextPage = _genrePage.value + 1
            withContext(Dispatchers.IO) {
                repository.getGenreAnime(_selectedSource.value, genre.slug, page = nextPage)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading more genre anime", e)
                        _genreHasNext.value = false
                    }
                    .collect { (items, hasNext) ->
                        _genreAnimeList.value = _genreAnimeList.value + items
                        _genreHasNext.value = hasNext
                        _genrePage.value = nextPage
                    }
            }
            _genreLoadingMore.value = false
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

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        if (newQuery.isNotEmpty()) {
            _searchLoading.value = true
        } else {
            _searchLoading.value = false
            _searchResults.value = emptyList()
        }
    }

    fun search() {
        val q = searchQuery.value
        if (q.isEmpty()) return
        _searchPage.value = 1
        _searchHasNext.value = false
        viewModelScope.launch {
            _searchLoading.value = true
            _selectedGenre.value = null
            _genreAnimeList.value = emptyList()
            withContext(Dispatchers.IO) {
                repository.search(_selectedSource.value, q, page = 1)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Search error", e)
                        _searchLoading.value = false
                    }
                    .collect { (items, hasNext) ->
                        _searchResults.value = items
                        _searchHasNext.value = hasNext
                        _searchLoading.value = false
                    }
            }
        }
    }

    fun loadMoreSearch() {
        val q = searchQuery.value
        if (q.isEmpty() || _searchLoadingMore.value || !_searchHasNext.value) return
        viewModelScope.launch {
            _searchLoadingMore.value = true
            val nextPage = _searchPage.value + 1
            withContext(Dispatchers.IO) {
                repository.search(_selectedSource.value, q, page = nextPage)
                    .catch { e ->
                        Log.e("AnimeViewModel", "Error loading more search results", e)
                        _searchHasNext.value = false
                    }
                    .collect { (items, hasNext) ->
                        _searchResults.value = _searchResults.value + items
                        _searchHasNext.value = hasNext
                        _searchPage.value = nextPage
                    }
            }
            _searchLoadingMore.value = false
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
                    // Coba pilih server sesuai preferensi default user (berlaku sama
                    // di 4 sumber data karena servers udah dinormalisasi jadi VideoServer
                    // yang seragam). Kalau preferensinya "Otomatis" atau server pilihan
                    // gak tersedia di episode ini, fallback ke server pertama seperti biasa.
                    val preferred = preferencesManager.findPreferred(servers, preferredServerKeyword.value)
                    // "Otomatis" prioritas: RELIABLE (murni regex, jarang gagal) dulu,
                    // baru SHAKY (Blogger/Abyss - bisa ExoPlayer tapi via WebView yang
                    // gampang gagal), baru bener-bener fallback ke server pertama.
                    val autoDefault = servers.firstOrNull {
                        VideoExtractor.getPlaybackConfidence("${it.name} ${it.embedUrl}") == VideoExtractor.PlaybackConfidence.RELIABLE
                    } ?: servers.firstOrNull {
                        VideoExtractor.getPlaybackConfidence("${it.name} ${it.embedUrl}") == VideoExtractor.PlaybackConfidence.SHAKY
                    } ?: servers.first()
                    selectServer(preferred ?: autoDefault, servers, animeDetail)
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
