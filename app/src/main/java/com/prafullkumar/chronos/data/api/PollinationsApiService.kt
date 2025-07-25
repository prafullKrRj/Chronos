package com.prafullkumar.chronos.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PollinationsApiService {
    @GET("prompt/{prompt}")
    suspend fun generateGreeting(@Path("prompt") prompt: String): Response<String>
}

