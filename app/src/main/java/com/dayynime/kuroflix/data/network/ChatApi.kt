package com.dayynime.kuroflix.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApi {

    // Ambil pesan terbaru, urut lama->baru, dibatasin 100 biar gak berat.
    @GET("rest/v1/chat_messages")
    suspend fun getMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.asc",
        @Query("limit") limit: Int = 100
    ): List<ChatMessage>

    // Ambil pesan yang lebih baru dari waktu tertentu -- dipakai buat polling
    // biar gak narik ulang 100 pesan tiap beberapa detik (hemat egress).
    @GET("rest/v1/chat_messages")
    suspend fun getMessagesAfter(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("created_at") createdAfter: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.asc"
    ): List<ChatMessage>

    @POST("rest/v1/chat_messages")
    suspend fun sendMessage(
        @Body message: ChatMessage,
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ChatMessage>

    // RLS di server yang jamin cuma bisa hapus pesan sendiri, tapi tetap kita
    // filter by id spesifik biar gak ada yang kehapus kepencet.
    @DELETE("rest/v1/chat_messages")
    suspend fun deleteMessage(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String // format: "eq.<uuid>"
    )
}
