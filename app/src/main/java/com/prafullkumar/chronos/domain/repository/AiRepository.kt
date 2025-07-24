package com.prafullkumar.chronos.domain.repository

interface AiRepository {
    suspend fun generateCustomAIMessage(prompt: String): Result<String>
}
