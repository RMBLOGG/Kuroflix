package com.dayynime.kuroflix.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseAuthApi {

    // Dipakai buat login pakai Google ID Token (dari Credential Manager).
    // GoTrue validasi id_token ke Google, terus bikin/samain user Supabase-nya.
    @POST("auth/v1/token?grant_type=id_token")
    suspend fun signInWithIdToken(
        @Body request: IdTokenSignInRequest,
        @Header("apikey") apiKey: String
    ): AuthResponse

    // Dipakai buat refresh access_token pakai refresh_token yang tersimpan,
    // biar user gak perlu login ulang tiap token expired (default 1 jam).
    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
        @Header("apikey") apiKey: String
    ): AuthResponse
}
