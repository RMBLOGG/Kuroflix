package com.dayynime.kuroflix.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IdTokenSignInRequest(
    val provider: String = "google",
    val id_token: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val refresh_token: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    val data: Map<String, String?>
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    val user: SupabaseUser? = null
)
