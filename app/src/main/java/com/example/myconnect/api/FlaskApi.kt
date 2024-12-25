package com.example.myconnect.api

import com.example.myconnect.model.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface FlaskApi {
    @Multipart
    @POST("/process_image")
    fun processImage(
        @Part image: MultipartBody.Part,
        @Part("question") question: RequestBody
    ): Call<ApiResponse>
}

