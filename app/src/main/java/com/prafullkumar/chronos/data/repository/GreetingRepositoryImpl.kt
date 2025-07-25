package com.prafullkumar.chronos.data.repository

import android.util.Log
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.data.api.PollinationsApiService
import com.prafullkumar.chronos.domain.repository.GreetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GreetingRepositoryImpl @Inject constructor(
    private val apiService: PollinationsApiService
) : GreetingRepository {

    override suspend fun generateGreeting(prompt: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading)
            val response = apiService.generateGreeting(prompt)
            Log.d("GreetingRepositoryImpl", "Generated greeting: $response")
            emit(Resource.Success(response ?: ""))
        } catch (e: Exception) {
            Log.d("GreetingRepositoryImpl", "Error generating greeting: ${e.message}")
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}