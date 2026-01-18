package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.CatFactsRepository
import com.example.third_dz.ui.state.FactDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FactDetailViewModel(
    private val repository: CatFactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FactDetailUiState>(FactDetailUiState.Loading)
    val uiState: StateFlow<FactDetailUiState> = _uiState.asStateFlow()

    fun loadFact(factId: String) {
        viewModelScope.launch {
            _uiState.value = FactDetailUiState.Loading
            try {
                val fact = repository.getFactById(factId)
                _uiState.value = FactDetailUiState.Success(fact)
            } catch (e: Exception) {
                _uiState.value = FactDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

