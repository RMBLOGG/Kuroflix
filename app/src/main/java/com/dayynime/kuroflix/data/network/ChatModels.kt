package com.dayynime.kuroflix.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String? = null,
    val user_id: String,
    val user_name: String?,
    val user_avatar: String?,
    val message: String,
    val created_at: String? = null
)
