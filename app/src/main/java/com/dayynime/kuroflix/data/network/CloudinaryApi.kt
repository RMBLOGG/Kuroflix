package com.dayynime.kuroflix.data.network

import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

@JsonClass(generateAdapter = true)
data class CloudinaryUploadResponse(
    val secure_url: String? = null,
    val public_id: String? = null
)

interface CloudinaryApi {
    @Multipart
    @POST
    suspend fun uploadImage(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}
