package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.CatFactsRepository
import com.example.third_dz.ui.state.FactDetailUiState
import kotlinx.coroutines.launch

class FactDetailViewModel(
    private val repository: CatFactsRepository
) : ViewModel() {

    var uiState: FactDetailUiState = FactDetailUiState.Loading
        private set

    fun loadFact(factId: String) {
        viewModelScope.launch {
            uiState = FactDetailUiState.Loading
            try {
                val fact = repository.getFactById(factId)
                uiState = FactDetailUiState.Success(fact)
            } catch (e: Exception) {
                uiState = FactDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

