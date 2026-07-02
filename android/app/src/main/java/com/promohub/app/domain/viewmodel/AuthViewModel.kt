package com.promohub.app.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promohub.app.data.repository.PromoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val username: String? = null
)

class AuthViewModel(private val repository: PromoRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            repository.token.collect { token ->
                if (token != null) {
                    _state.value = _state.value.copy(isAuthenticated = true)
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.login(username, password)
            result.onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = true
                )
            }
            result.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = it.message
                )
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.register(username, email, password)
            result.onSuccess {
                login(username, password)
            }
            result.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = it.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = AuthState()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
