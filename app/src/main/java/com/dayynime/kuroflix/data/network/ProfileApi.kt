package com.dayynime.kuroflix.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

interface ProfileApi {

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String, // format: "eq.<uuid>"
        @Query("select") select: String = "*"
    ): List<Profile>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Body body: ProfileUpdateBody,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String, // format: "eq.<uuid>"
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Profile>
}
