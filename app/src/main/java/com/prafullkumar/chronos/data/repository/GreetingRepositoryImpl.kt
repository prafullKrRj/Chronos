package com.prafullkumar.chronos.data.repository

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
            if (response.isSuccessful) {
                val greeting = response.body() ?: "Unable to generate greeting"
                emit(Resource.Success(greeting))
            } else {
                emit(Resource.Error("Failed to generate greeting: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error occurred"))
        }
    }
}

