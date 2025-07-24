package com.prafullkumar.chronos.presentation.screens.onBoarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.core.UiState
import com.prafullkumar.chronos.domain.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Empty)
    val uiState = _uiState.asStateFlow()


    fun loginUser(context: Context) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            loginRepository.loginUser(context).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _uiState.value = UiState.Error(result.message ?: "Unknown error")
                    }

                    Resource.Loading -> {
                        _uiState.value = UiState.Loading
                    }

                    is Resource.Success<*> -> {
                        _uiState.value = UiState.Success(result.data as Boolean)
                    }
                }
            }
        }
    }

}