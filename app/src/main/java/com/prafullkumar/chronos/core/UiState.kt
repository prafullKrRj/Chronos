package com.prafullkumar.chronos.core

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String? = null, val throwable: Throwable? = null) :
        UiState<Nothing>

    data object Empty : UiState<Nothing>
}

val <T> UiState<T>.data: T?
    get() = when (this) {
        is UiState.Success -> data
        else -> null
    }
val <T> UiState<T>.loading: Boolean
    get() = this is UiState.Loading

val <T> UiState<T>.error: String?
    get() = when (this) {
        is UiState.Error -> message
        else -> null
    }
val <T> UiState<T>.hasSuccess: Boolean
    get() = this is UiState.Success