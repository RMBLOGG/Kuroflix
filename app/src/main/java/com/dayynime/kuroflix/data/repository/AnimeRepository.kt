package com.dayynime.kuroflix.data.repository

import android.content.Context
import com.dayynime.kuroflix.data.local.*
import com.dayynime.kuroflix.data.model.*
import com.dayynime.kuroflix.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AnimeRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val bookmarkDao = db.bookmarkDao()
    private val watchHistoryDao = db.watchHistoryDao()

    private val animasuApi = NetworkModule.getAnimasuApi(context)
    private val samehadakuApi = NetworkModule.getSamehadakuApi(context)
    private val animekompiApi = NetworkModule.getAnimekompiApi(context)
    private val donghuaApi = NetworkModule.getDonghuaApi(context)

    // Data normalizers for Animasu
    private fun AnimasuItem.toAnimeItem() = AnimeItem(
        id = slug ?: "", title = title ?: "No Title", thumbnail = poster ?: "",
        rating = score ?: "", status = status ?: "", type = type ?: "TV", source = "animasu"
    )

    private fun AnimasuDetailData.toAnimeDetail(slug: String) = AnimeDetail(
        id = slug,
        title = title ?: "No Title",
        description = synopsis ?: "",
        thumbnail = poster ?: "",
        rating = rating ?: "",
        status = status ?: "",
        type = type ?: "TV",
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        episodes = episodes?.map { EpisodeItem(id = it.slug ?: "", title = it.name ?: "Episode", number = it.name ?: "", date = "", source = "animasu") } ?: emptyList(),
        source = "animasu"
    )

    // Data normalizers for Samehadaku
    private fun SamehadakuItem.toAnimeItem() = AnimeItem(
        id = animeId ?: "", title = title ?: "No Title", thumbnail = poster ?: "",
        rating = score ?: "", status = status ?: "", type = type ?: "TV", source = "samehadaku"
    )

    private fun SamehadakuDetailData.toAnimeDetail(slug: String) = AnimeDetail(
        id = slug,
        title = title ?: "No Title",
        description = synopsis?.paragraphs?.joinToString("\n") ?: "",
        thumbnail = poster ?: "",
        rating = score?.value ?: "",
        status = status ?: "",
        type = type ?: "TV",
        genres = genreList?.mapNotNull { it.title } ?: emptyList(),
        episodes = episodeList?.map { EpisodeItem(id = it.episodeId ?: "", title = it.title ?: "Episode", number = it.title ?: "", date = "", source = "samehadaku") } ?: emptyList(),
        source = "samehadaku"
    )

    // Data normalizers for Animekompi
    private fun AnimekompiItem.toAnimeItem() = AnimeItem(
        id = animeSlug(), title = title ?: "No Title", thumbnail = poster ?: image ?: "",
        rating = rating ?: "", status = status ?: "", type = type ?: "TV", source = "animekompi"
    )

    private fun AnimekompiDetailData.toAnimeDetail(slug: String) = AnimeDetail(
        id = slug,
        title = title ?: "No Title",
        description = synopsis ?: "",
        thumbnail = image ?: "",
        rating = rating ?: "",
        status = metadata?.status ?: "",
        type = metadata?.tipe ?: "TV",
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        episodes = episodes?.map { EpisodeItem(id = it.slug ?: "", title = it.title ?: "Episode", number = it.num ?: "", date = "", source = "animekompi") } ?: emptyList(),
        source = "animekompi"
    )

    // Data normalizers for Donghua
    private fun DonghuaBasicItem.toAnimeItem() = AnimeItem(
        id = (slug ?: "").trimEnd('/'), title = title ?: "No Title", thumbnail = poster ?: "",
        rating = "", status = status ?: "", type = type ?: "Donghua", source = "donghua"
    )

    private fun DonghuaListItem.toAnimeItem() = AnimeItem(
        id = (slug ?: "").trimEnd('/'), title = title ?: "No Title", thumbnail = poster ?: "",
        rating = "", status = status ?: "", type = type ?: "Donghua", source = "donghua"
    )

    private fun DonghuaReleaseItem.toAnimeItem() = AnimeItem(
        id = animeSlug(), title = title ?: "No Title", thumbnail = poster ?: "",
        rating = "", status = status ?: "", type = type ?: "Donghua", source = "donghua"
    )

    private fun DonghuaDetailResponse.toAnimeDetail(slug: String) = AnimeDetail(
        id = slug,
        title = title ?: "No Title",
        description = synopsis ?: "",
        thumbnail = poster ?: "",
        rating = rating ?: "",
        status = status ?: "",
        type = type ?: "Donghua",
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        episodes = episodes_list?.map { EpisodeItem(id = (it.slug ?: "").trimEnd('/'), title = it.episode ?: "Episode", number = it.episode ?: "", date = "", source = "donghua") } ?: emptyList(),
        source = "donghua"
    )

    // Bookmarks Local operations
    val bookmarks: Flow<List<AnimeItem>> = bookmarkDao.getAllBookmarks().map { entities ->
        entities.map {
            AnimeItem(
                id = it.id,
                title = it.title,
                thumbnail = it.thumbnail,
                rating = it.rating,
                status = "",
                type = "",
                source = it.source
            )
        }
    }

    fun isBookmarked(compositeId: String): Flow<Boolean> = bookmarkDao.isBookmarked(compositeId)

    suspend fun saveBookmark(anime: AnimeItem) {
        val compositeId = "${anime.source}:${anime.id}"
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                id = compositeId,
                title = anime.title,
                thumbnail = anime.thumbnail,
                rating = anime.rating,
                source = anime.source
            )
        )
    }

    suspend fun deleteBookmark(compositeId: String) {
        bookmarkDao.deleteBookmark(compositeId)
    }

    // Watch History local operations
    val history: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getAllHistory()

    fun getHistoryForAnime(animeId: String): Flow<List<WatchHistoryEntity>> = 
        watchHistoryDao.getHistoryForAnime(animeId)

    suspend fun saveWatchHistory(
        animeId: String,
        episodeId: String,
        animeTitle: String,
        episodeTitle: String,
        thumbnail: String,
        progressMillis: Long,
        durationMillis: Long,
        source: String
    ) {
        val compositeEpisodeId = "$source:$episodeId"
        watchHistoryDao.insertHistory(
            WatchHistoryEntity(
                episodeId = compositeEpisodeId,
                animeId = "$source:$animeId",
                animeTitle = animeTitle,
                episodeTitle = episodeTitle,
                thumbnail = thumbnail,
                progressMillis = progressMillis,
                durationMillis = durationMillis,
                source = source
            )
        )
    }

    suspend fun clearHistory() {
        watchHistoryDao.clearAllHistory()
    }

    // Home routing
    fun getHome(source: String): Flow<HomeData> = flow {
        val homeData = when (source.lowercase()) {
            "animasu" -> {
                val res = animasuApi.getHome()
                val ongoing = res.ongoing?.map { it.toAnimeItem() } ?: emptyList()
                val recent = res.recent?.map { it.toAnimeItem() } ?: emptyList()
                HomeData(
                    latest = recent,
                    ongoing = ongoing,
                    popular = ongoing,
                    completed = emptyList(),
                    movies = emptyList()
                )
            }
            "samehadaku" -> {
                val res = samehadakuApi.getHome()
                val recent = res.data?.recent?.animeList?.map { it.toAnimeItem() } ?: emptyList()
                val movie = res.data?.movie?.animeList?.map { it.toAnimeItem() } ?: emptyList()
                HomeData(
                    latest = recent,
                    popular = recent,
                    ongoing = recent,
                    completed = emptyList(),
                    movies = movie
                )
            }
            "animekompi" -> {
                val res = animekompiApi.getHome()
                val list = res.data?.map { it.toAnimeItem() } ?: emptyList()
                HomeData(
                    latest = list,
                    popular = list,
                    ongoing = list,
                    completed = emptyList(),
                    movies = emptyList()
                )
            }
            "donghua" -> {
                val res = donghuaApi.getHome(1)
                val latestRelease = res.latest_release?.map { it.toAnimeItem() } ?: emptyList()
                val completed = res.completed_donghua?.map { it.toAnimeItem() } ?: emptyList()
                HomeData(
                    latest = latestRelease,
                    popular = latestRelease,
                    ongoing = latestRelease,
                    completed = completed,
                    movies = emptyList()
                )
            }
            else -> HomeData()
        }
        emit(homeData)
    }

    // Detail routing
    fun getDetail(source: String, slug: String): Flow<AnimeDetail> = flow {
        val detail = when (source.lowercase()) {
            "animasu" -> {
                val data = animasuApi.getDetail(slug).detail
                data?.toAnimeDetail(slug)
            }
            "samehadaku" -> {
                val data = samehadakuApi.getDetail(slug).data
                data?.toAnimeDetail(slug)
            }
            "animekompi" -> {
                val data = animekompiApi.getDetail(slug).data
                data?.toAnimeDetail(slug)
            }
            "donghua" -> {
                val data = donghuaApi.getDetail(slug)
                data.toAnimeDetail(slug)
            }
            else -> null
        }
        if (detail != null) {
            emit(detail)
        } else {
            throw Exception("Failed to load anime details from $source")
        }
    }

    // Episode routing
    fun getEpisode(source: String, episodeId: String): Flow<List<VideoServer>> = flow {
        val servers = when (source.lowercase()) {
            "animasu" -> {
                val response = animasuApi.getEpisode(episodeId)
                response.streams?.mapNotNull { stream ->
                    val name = stream.name ?: "Server"
                    val url = stream.url ?: ""
                    if (url.isNotEmpty()) VideoServer(name, url) else null
                } ?: emptyList()
            }
            "samehadaku" -> {
                val response = samehadakuApi.getEpisode(episodeId)
                val episodeData = response.data
                val serversList = mutableListOf<VideoServer>()
                if (episodeData != null) {
                    episodeData.server?.qualities?.forEach { quality ->
                        val qualityTitle = quality.title ?: ""
                        quality.serverList?.forEach { serverItem ->
                            val name = "${serverItem.title ?: "Server"} ($qualityTitle)"
                            val serverId = serverItem.serverId ?: ""
                            if (serverId.isNotEmpty()) {
                                serversList.add(VideoServer(name, serverId))
                            }
                        }
                    }
                    val defUrl = episodeData.defaultStreamingUrl ?: ""
                    if (serversList.isEmpty() && defUrl.isNotEmpty()) {
                        serversList.add(VideoServer("Default Server", defUrl))
                    }
                }
                serversList
            }
            "animekompi" -> {
                val response = animekompiApi.getEpisode(episodeId)
                response.data?.mirrors?.mapNotNull { mirror ->
                    val name = mirror.name ?: "Server"
                    val url = mirror.url ?: ""
                    if (url.isNotEmpty()) VideoServer(name, url) else null
                } ?: emptyList()
            }
            "donghua" -> {
                val response = donghuaApi.getEpisode(episodeId)
                val streaming = response.streaming
                val serversList = mutableListOf<VideoServer>()
                if (streaming != null) {
                    val main = streaming.main_url
                    if (main != null) {
                        val name = main.name ?: "Main Server"
                        val url = main.url ?: ""
                        if (url.isNotEmpty()) serversList.add(VideoServer(name, url))
                    }
                    streaming.servers?.forEach { s ->
                        val name = s.name ?: "Server"
                        val url = s.url ?: ""
                        if (url.isNotEmpty()) serversList.add(VideoServer(name, url))
                    }
                }
                serversList
            }
            else -> emptyList()
        }
        emit(servers)
    }

    // Samehadaku special: getServerVideo
    suspend fun getSamehadakuServerVideo(serverId: String): String {
        return try {
            val response = samehadakuApi.getServerVideo(serverId)
            response.data?.url ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Search routing
    fun search(source: String, query: String, page: Int = 1): Flow<List<AnimeItem>> = flow {
        val list = when (source.lowercase()) {
            "animasu" -> animasuApi.search(query, page).animes?.map { it.toAnimeItem() } ?: emptyList()
            "samehadaku" -> samehadakuApi.search(query, page).data?.animeList?.map { it.toAnimeItem() } ?: emptyList()
            "animekompi" -> animekompiApi.search(query, page).data?.map { it.toAnimeItem() } ?: emptyList()
            "donghua" -> donghuaApi.search(query).data?.map { it.toAnimeItem() } ?: emptyList()
            else -> emptyList()
        }
        emit(list)
    }

    // Genre items
    fun getGenres(source: String): Flow<List<GenreItem>> = flow {
        val list = when (source.lowercase()) {
            "animasu" -> animasuApi.getGenres().genres?.mapNotNull { 
                val title = it.name ?: ""
                val slug = it.slug ?: ""
                if (title.isNotEmpty() && slug.isNotEmpty()) GenreItem(title, slug) else null
            } ?: emptyList()
            "samehadaku" -> samehadakuApi.getGenres().data?.genreList?.mapNotNull { 
                val title = it.title ?: ""
                val slug = it.genreId ?: ""
                if (title.isNotEmpty() && slug.isNotEmpty()) GenreItem(title, slug) else null
            } ?: emptyList()
            "animekompi" -> animekompiApi.getGenres().data?.mapNotNull { 
                val title = it.name ?: ""
                val slug = it.value ?: ""
                if (title.isNotEmpty() && slug.isNotEmpty()) GenreItem(title, slug) else null
            } ?: emptyList()
            "donghua" -> donghuaApi.getGenres().data?.mapNotNull { 
                val title = it.name ?: ""
                val slug = it.slug ?: ""
                if (title.isNotEmpty() && slug.isNotEmpty()) GenreItem(title, slug) else null
            } ?: emptyList()
            else -> emptyList()
        }
        emit(list)
    }

    // Genre anime
    fun getGenreAnime(source: String, genreSlug: String, page: Int = 1): Flow<List<AnimeItem>> = flow {
        val list = when (source.lowercase()) {
            "animasu" -> animasuApi.getGenreAnime(genreSlug, page).animes?.map { it.toAnimeItem() } ?: emptyList()
            "samehadaku" -> samehadakuApi.getGenreAnime(genreSlug, page).data?.animeList?.map { it.toAnimeItem() } ?: emptyList()
            "animekompi" -> animekompiApi.getGenreAnime(genreSlug, page).data?.map { it.toAnimeItem() } ?: emptyList()
            "donghua" -> emptyList()
            else -> emptyList()
        }
        emit(list)
    }
}
