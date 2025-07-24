package com.prafullkumar.chronos.core

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String? = null, val throwable: Throwable? = null) :
        Resource<Nothing>()

    data object Loading : Resource<Nothing>()
}