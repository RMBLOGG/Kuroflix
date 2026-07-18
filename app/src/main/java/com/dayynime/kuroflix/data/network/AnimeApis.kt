package com.dayynime.kuroflix.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimasuApi {
    @GET("animasu/home")
    suspend fun getHome(): AnimasuHomeResponse

    @GET("animasu/popular")
    suspend fun getPopular(@Query("page") page: Int): AnimasuListResponse

    @GET("animasu/movies")
    suspend fun getMovies(@Query("page") page: Int): AnimasuListResponse

    @GET("animasu/ongoing")
    suspend fun getOngoing(@Query("page") page: Int): AnimasuListResponse

    @GET("animasu/completed")
    suspend fun getCompleted(@Query("page") page: Int): AnimasuListResponse

    @GET("animasu/latest")
    suspend fun getLatest(@Query("page") page: Int): AnimasuListResponse

    @GET("animasu/search/{keyword}")
    suspend fun search(@Path("keyword") keyword: String, @Query("page") page: Int): AnimasuListResponse

    @GET("animasu/genres")
    suspend fun getGenres(): AnimasuGenreListResponse

    @GET("animasu/genre/{slug}")
    suspend fun getGenreAnime(@Path("slug") slug: String, @Query("page") page: Int): AnimasuListResponse

    @GET("animasu/detail/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): AnimasuDetailResponse

    @GET("animasu/episode/{slug}")
    suspend fun getEpisode(@Path("slug") slug: String): AnimasuEpisodeResponse

    @GET("animasu/schedule")
    suspend fun getSchedule(): AnimasuScheduleResponse
}

interface SamehadakuApi {
    @GET("samehadaku/home")
    suspend fun getHome(): SamehadakuHomeResponse

    @GET("samehadaku/recent")
    suspend fun getRecent(@Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/popular")
    suspend fun getPopular(@Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/movies")
    suspend fun getMovies(@Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/ongoing")
    suspend fun getOngoing(@Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/completed")
    suspend fun getCompleted(@Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/search")
    suspend fun search(@Query("q") query: String, @Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/genres")
    suspend fun getGenres(): SamehadakuGenresResponse

    @GET("samehadaku/genres/{genreId}")
    suspend fun getGenreAnime(@Path("genreId") genreId: String, @Query("page") page: Int): SamehadakuListResponse

    @GET("samehadaku/anime/{animeId}")
    suspend fun getDetail(@Path("animeId") animeId: String): SamehadakuDetailResponse

    @GET("samehadaku/episode/{episodeId}")
    suspend fun getEpisode(@Path("episodeId") episodeId: String): SamehadakuEpisodeResponse

    @GET("samehadaku/server/{serverId}")
    suspend fun getServerVideo(@Path("serverId") serverId: String): SamehadakuServerLinkResponse

    @GET("samehadaku/schedule")
    suspend fun getSchedule(): SamehadakuScheduleResponse
}

interface AnimekompiApi {
    @GET("home")
    suspend fun getHome(): AnimekompiListResponse

    @GET("terbaru")
    suspend fun getLatest(@Query("page") page: Int): AnimekompiListResponse

    @GET("order/popular")
    suspend fun getPopular(@Query("page") page: Int): AnimekompiListResponse

    @GET("movie")
    suspend fun getMovies(@Query("page") page: Int): AnimekompiListResponse

    @GET("status/ongoing")
    suspend fun getOngoing(@Query("page") page: Int): AnimekompiListResponse

    @GET("status/completed")
    suspend fun getCompleted(@Query("page") page: Int): AnimekompiListResponse

    @GET("search")
    suspend fun search(@Query("q") query: String, @Query("page") page: Int): AnimekompiListResponse

    @GET("genres")
    suspend fun getGenres(): AnimekompiGenresResponse

    @GET("genre/{slug}")
    suspend fun getGenreAnime(@Path("slug") slug: String, @Query("page") page: Int): AnimekompiListResponse

    @GET("detail/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): AnimekompiDetailResponse

    @GET("episode/{slug}")
    suspend fun getEpisode(@Path("slug") slug: String): AnimekompiEpisodeResponse

    @GET("schedule")
    suspend fun getSchedule(): AnimekompiScheduleResponse
}

interface DonghuaApi {
    @GET("donghua/home/{page}")
    suspend fun getHome(@Path("page") page: Int): DonghuaHomeResponse

    @GET("donghua/ongoing/{page}")
    suspend fun getOngoing(@Path("page") page: Int): DonghuaOngoingResponse

    @GET("donghua/completed/{page}")
    suspend fun getCompleted(@Path("page") page: Int): DonghuaCompletedResponse

    @GET("donghua/latest/{page}")
    suspend fun getLatest(@Path("page") page: Int): DonghuaLatestResponse

    @GET("donghua/search/{query}")
    suspend fun search(@Path("query") query: String): DonghuaSearchResponse

    @GET("donghua/detail/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): DonghuaDetailResponse

    @GET("donghua/episode/{slug}")
    suspend fun getEpisode(@Path("slug") slug: String): DonghuaEpisodeResponse

    @GET("donghua/genres")
    suspend fun getGenres(): DonghuaGenresResponse

    @GET("donghua/schedule")
    suspend fun getSchedule(): DonghuaScheduleResponse
}
