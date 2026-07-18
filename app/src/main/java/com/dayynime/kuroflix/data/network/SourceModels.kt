package com.dayynime.kuroflix.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ================== ANIMASU ==================
@JsonClass(generateAdapter = true)
data class AnimasuItem(
    val title: String? = null,
    val slug: String? = null,
    val poster: String? = null,
    val episode: String? = null,
    val type: String? = null,
    val genres: List<String>? = null,
    val release: String? = null,
    val status: String? = null,
    val episode_count: String? = null,
    val score: String? = null,
    val estimation: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimasuHomeResponse(
    val status: String? = null,
    val ongoing: List<AnimasuItem>? = null,
    val recent: List<AnimasuItem>? = null
)

@JsonClass(generateAdapter = true)
data class AnimasuListResponse(
    val status: String? = null,
    val animes: List<AnimasuItem>? = null
)

@JsonClass(generateAdapter = true)
data class AnimasuGenreItem(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class AnimasuGenreListResponse(val status: String? = null, val genres: List<AnimasuGenreItem>? = null)

@JsonClass(generateAdapter = true)
data class AnimasuDetailEpisode(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class AnimasuDetailGenre(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class AnimasuDetailData(
    val title: String? = null,
    val poster: String? = null,
    val rating: String? = null,
    val synopsis: String? = null,
    val genres: List<AnimasuDetailGenre>? = null,
    val status: String? = null,
    val aired: String? = null,
    val type: String? = null,
    val duration: String? = null,
    val studio: String? = null,
    val episodes: List<AnimasuDetailEpisode>? = null
)

@JsonClass(generateAdapter = true)
data class AnimasuDetailResponse(val status: String? = null, val detail: AnimasuDetailData? = null)

@JsonClass(generateAdapter = true)
data class AnimasuStream(val name: String? = null, val url: String? = null)

@JsonClass(generateAdapter = true)
data class AnimasuEpisodeResponse(
    val status: String? = null,
    val title: String? = null,
    val streams: List<AnimasuStream>? = null
)

// ================== SAMEHADAKU ==================
@JsonClass(generateAdapter = true)
data class SamehadakuGenreRef(val title: String? = null, val genreId: String? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuItem(
    val title: String? = null,
    val poster: String? = null,
    val animeId: String? = null,
    val episodes: String? = null,
    val releasedOn: String? = null,
    val releaseDate: String? = null,
    val type: String? = null,
    val score: String? = null,
    val status: String? = null,
    val genreList: List<SamehadakuGenreRef>? = null
)

@JsonClass(generateAdapter = true)
data class SamehadakuAnimeSection(val animeList: List<SamehadakuItem>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuHomeData(val recent: SamehadakuAnimeSection? = null, val movie: SamehadakuAnimeSection? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuHomeResponse(val status: String? = null, val data: SamehadakuHomeData? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuListData(val animeList: List<SamehadakuItem>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuListResponse(val status: String? = null, val data: SamehadakuListData? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuGenresData(val genreList: List<SamehadakuGenreRef>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuGenresResponse(val status: String? = null, val data: SamehadakuGenresData? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuScore(val value: String? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuSynopsis(val paragraphs: List<String>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuEpisodeRef(val title: String? = null, val episodeId: String? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuDetailData(
    val title: String? = null,
    val poster: String? = null,
    val score: SamehadakuScore? = null,
    val status: String? = null,
    val type: String? = null,
    val duration: String? = null,
    val studios: String? = null,
    val aired: String? = null,
    val synopsis: SamehadakuSynopsis? = null,
    val genreList: List<SamehadakuGenreRef>? = null,
    val episodeList: List<SamehadakuEpisodeRef>? = null
)

@JsonClass(generateAdapter = true)
data class SamehadakuDetailResponse(val status: String? = null, val data: SamehadakuDetailData? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuServerItem(val title: String? = null, val serverId: String? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuQuality(val title: String? = null, val serverList: List<SamehadakuServerItem>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuServer(val qualities: List<SamehadakuQuality>? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuEpisodeData(
    val title: String? = null,
    val poster: String? = null,
    val defaultStreamingUrl: String? = null,
    val server: SamehadakuServer? = null
)

@JsonClass(generateAdapter = true)
data class SamehadakuEpisodeResponse(val status: String? = null, val data: SamehadakuEpisodeData? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuServerLinkData(val url: String? = null)

@JsonClass(generateAdapter = true)
data class SamehadakuServerLinkResponse(val status: String? = null, val data: SamehadakuServerLinkData? = null)

// ================== ANIMEKOMPI ==================
@JsonClass(generateAdapter = true)
data class AnimekompiItem(
    val title: String? = null,
    val slug: String? = null,
    val poster: String? = null,
    val image: String? = null,
    val episode: String? = null,
    val type: String? = null,
    val rating: String? = null,
    val status: String? = null,
    val date: String? = null,
    val time: String? = null,
    val detail_slug: String? = null
) {
    // Endpoint list Animekompi ngebalikin slug EPISODE, bukan slug anime.
    // Contoh: "one-piece-episode-1168-subtitle-indonesia" -> "one-piece"
    fun animeSlug(): String {
        val raw = (detail_slug?.takeIf { it.isNotBlank() } ?: slug ?: "").trim()
        val match = Regex("^(.+?)-episode-\\d").find(raw)
        return match?.groupValues?.get(1) ?: raw.removeSuffix("-subtitle-indonesia")
    }
}

@JsonClass(generateAdapter = true)
data class AnimekompiListResponse(val data: List<AnimekompiItem>? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiGenreItem(val name: String? = null, val value: String? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiGenresResponse(val data: List<AnimekompiGenreItem>? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiDetailGenre(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiDetailEpisode(val title: String? = null, val num: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiMetadata(
    val tipe: String? = null,
    val status: String? = null,
    val dirilis: String? = null,
    val dirilis_2: String? = null,
    val durasi: String? = null,
    val studio: String? = null,
    val season: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimekompiDetailData(
    val title: String? = null,
    val alter_title: String? = null,
    val image: String? = null,
    val rating: String? = null,
    val synopsis: String? = null,
    val metadata: AnimekompiMetadata? = null,
    val genres: List<AnimekompiDetailGenre>? = null,
    val episodes: List<AnimekompiDetailEpisode>? = null
)

@JsonClass(generateAdapter = true)
data class AnimekompiDetailResponse(val data: AnimekompiDetailData? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiMirror(val name: String? = null, val url: String? = null)

@JsonClass(generateAdapter = true)
data class AnimekompiEpisodeData(
    val title: String? = null,
    val mirrors: List<AnimekompiMirror>? = null
)

@JsonClass(generateAdapter = true)
data class AnimekompiEpisodeResponse(val data: AnimekompiEpisodeData? = null)

// ================== DONGHUA ==================
@JsonClass(generateAdapter = true)
data class DonghuaBasicItem(
    val title: String? = null,
    val slug: String? = null,
    val poster: String? = null,
    val status: String? = null,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class DonghuaReleaseItem(
    val title: String? = null,
    val slug: String? = null, // slug EPISODE
    val poster: String? = null,
    val status: String? = null,
    val type: String? = null,
    val current_episode: String? = null
) {
    fun animeSlug(): String {
        val cleaned = (slug ?: "").trim().trimEnd('/')
        val match = Regex("^(.+?)-episode-\\d").find(cleaned)
        return match?.groupValues?.get(1) ?: cleaned.removeSuffix("-subtitle-indonesia")
    }
}

@JsonClass(generateAdapter = true)
data class DonghuaHomeResponse(
    val status: String? = null,
    val latest_release: List<DonghuaReleaseItem>? = null,
    val completed_donghua: List<DonghuaBasicItem>? = null
)

@JsonClass(generateAdapter = true)
data class DonghuaOngoingResponse(val status: String? = null, val ongoing_donghua: List<DonghuaBasicItem>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaCompletedResponse(val status: String? = null, val completed_donghua: List<DonghuaBasicItem>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaListItem(
    val title: String? = null,
    val slug: String? = null,
    val poster: String? = null,
    val status: String? = null,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class DonghuaLatestResponse(val status: String? = null, val latest_donghua: List<DonghuaListItem>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaSearchResponse(val data: List<DonghuaListItem>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaGenreTag(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class DonghuaGenresResponse(val data: List<DonghuaGenreTag>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaDetailGenre(val name: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class DonghuaDetailEpisodeItem(val episode: String? = null, val slug: String? = null)

@JsonClass(generateAdapter = true)
data class DonghuaDetailResponse(
    val status: String? = null,
    val title: String? = null,
    val alter_title: String? = null,
    val poster: String? = null,
    val rating: String? = null,
    val studio: String? = null,
    val duration: String? = null,
    val type: String? = null,
    val season: String? = null,
    val released_on: String? = null,
    val genres: List<DonghuaDetailGenre>? = null,
    val synopsis: String? = null,
    val episodes_list: List<DonghuaDetailEpisodeItem>? = null
)

@JsonClass(generateAdapter = true)
data class DonghuaServerLink(val name: String? = null, val url: String? = null)

@JsonClass(generateAdapter = true)
data class DonghuaStreaming(val main_url: DonghuaServerLink? = null, val servers: List<DonghuaServerLink>? = null)

@JsonClass(generateAdapter = true)
data class DonghuaEpisodeResponse(
    val status: String? = null,
    val episode: String? = null,
    val streaming: DonghuaStreaming? = null
)
