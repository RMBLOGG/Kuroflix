package com.dayynime.kuroflix.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profile(
    val id: String,
    val full_name: String?,
    val avatar_url: String?,
    val updated_at: String? = null
)

@JsonClass(generateAdapter = true)
data class ProfileUpdateBody(
    val full_name: String? = null,
    val avatar_url: String? = null
)
