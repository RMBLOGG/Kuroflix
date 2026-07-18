package com.dayynime.kuroflix.data.model

// Unified UI Models
data class AnimeItem(
    val id: String,
    val title: String,
    val thumbnail: String,
    val rating: String,
    val status: String,
    val type: String,
    val source: String
)

data class AnimeDetail(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val rating: String,
    val status: String,
    val type: String,
    val genres: List<String>,
    val episodes: List<EpisodeItem>,
    val source: String
)

data class EpisodeItem(
    val id: String,
    val title: String,
    val number: String,
    val date: String,
    val source: String
)

data class VideoServer(
    val name: String,
    val embedUrl: String
)

data class VideoSource(
    val url: String,
    val label: String,
    val headers: Map<String, String> = emptyMap(),
    val isEmbed: Boolean = false
)

// Genre item
data class GenreItem(
    val title: String,
    val slug: String
)

// Clean UI Home Data container
data class HomeData(
    val latest: List<AnimeItem> = emptyList(),
    val popular: List<AnimeItem> = emptyList(),
    val ongoing: List<AnimeItem> = emptyList(),
    val completed: List<AnimeItem> = emptyList(),
    val movies: List<AnimeItem> = emptyList()
)
