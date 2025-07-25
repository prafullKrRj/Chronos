package com.prafullkumar.chronos.domain.repository

import com.prafullkumar.chronos.core.Resource
import kotlinx.coroutines.flow.Flow

interface GreetingRepository {
    suspend fun generateGreeting(prompt: String): Flow<Resource<String>>
}

