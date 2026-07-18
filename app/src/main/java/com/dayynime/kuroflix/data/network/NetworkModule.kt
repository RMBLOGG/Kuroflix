package com.dayynime.kuroflix.data.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object NetworkModule {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun createRetrofit(baseUrl: String, context: Context): Retrofit {
        val client = createCachedOkHttpClient(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private var animasuSamehadakuRetrofit: Retrofit? = null
    private var animekompiRetrofit: Retrofit? = null
    private var donghuaRetrofit: Retrofit? = null

    fun getAnimasuApi(context: Context): AnimasuApi {
        if (animasuSamehadakuRetrofit == null) {
            animasuSamehadakuRetrofit = createRetrofit("https://www.sankavollerei.com/anime/", context)
        }
        return animasuSamehadakuRetrofit!!.create(AnimasuApi::class.java)
    }

    fun getSamehadakuApi(context: Context): SamehadakuApi {
        if (animasuSamehadakuRetrofit == null) {
            animasuSamehadakuRetrofit = createRetrofit("https://www.sankavollerei.com/anime/", context)
        }
        return animasuSamehadakuRetrofit!!.create(SamehadakuApi::class.java)
    }

    fun getAnimekompiApi(context: Context): AnimekompiApi {
        if (animekompiRetrofit == null) {
            animekompiRetrofit = createRetrofit("https://www.sankavollerei.web.id/anime/animekompi/", context)
        }
        return animekompiRetrofit!!.create(AnimekompiApi::class.java)
    }

    fun getDonghuaApi(context: Context): DonghuaApi {
        if (donghuaRetrofit == null) {
            donghuaRetrofit = createRetrofit("https://www.sankavollerei.web.id/anime/", context)
        }
        return donghuaRetrofit!!.create(DonghuaApi::class.java)
    }
}
