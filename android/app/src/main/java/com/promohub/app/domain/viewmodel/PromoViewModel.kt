package com.promohub.app.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promohub.app.data.local.PromoCodeEntity
import com.promohub.app.data.remote.RefreshResponse
import com.promohub.app.data.remote.VoteResponse
import com.promohub.app.data.repository.PromoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PromoListState(
    val promocodes: List<PromoCodeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedService: String? = null,
    val searchQuery: String = ""
)

class PromoViewModel(private val repository: PromoRepository) : ViewModel() {

    private val _state = MutableStateFlow(PromoListState())
    val state: StateFlow<PromoListState> = _state

    init {
        loadPromoCodes()
        observeLocalData()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            repository.getPromoCodes().collect { list ->
                _state.value = _state.value.copy(promocodes = list)
            }
        }
    }

    fun loadPromoCodes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.refreshPromoCodes()
            result.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = it.message
                )
            }
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun filterByService(service: String?) {
        _state.value = _state.value.copy(selectedService = service)
        viewModelScope.launch {
            if (service != null) {
                repository.refreshPromoCodesByService(service)
            } else {
                repository.refreshPromoCodes()
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isNotBlank()) {
                repository.searchOnline(query)
            } else {
                repository.refreshPromoCodes()
            }
        }
    }

    fun toggleFavorite(promocodeId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(promocodeId)
            repository.refreshPromoCodes()
        }
    }

    fun votePromocode(
        id: Int,
        isWorking: Boolean,
        onResult: (Result<VoteResponse>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.votePromocode(id, isWorking)
            // Если код скрыли — обновляем список, чтобы он пропал с экрана
            result.onSuccess { if (it.removed) repository.refreshPromoCodes() }
            onResult(result)
        }
    }

    fun refreshFromSources(onResult: (Result<RefreshResponse>) -> Unit = {}) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.refreshFromSources()
            _state.value = _state.value.copy(isLoading = false)
            onResult(result)
        }
    }

    fun getFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.getFavorites()
            result.onSuccess { list ->
                _state.value = _state.value.copy(
                    promocodes = list,
                    isLoading = false
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

    fun getHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.getHistory()
            result.onSuccess { list ->
                _state.value = _state.value.copy(
                    promocodes = list,
                    isLoading = false
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

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _state.value = _state.value.copy(promocodes = emptyList())
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
