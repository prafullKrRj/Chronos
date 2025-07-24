package com.prafullkumar.chronos.domain.repository

import android.content.Context
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.data.dtos.UserDto
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    fun loginUser(context: Context): Flow<Resource<Boolean>>
    fun signOutUser(): Flow<Resource<Boolean>>
    suspend fun createOrUpdateUser(user: UserDto): Result<Unit>
}
