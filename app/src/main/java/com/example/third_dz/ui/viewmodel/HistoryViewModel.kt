package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.RecentViewRepository
import com.example.third_dz.domain.usecase.recent.ClearHistoryUseCase
import com.example.third_dz.domain.usecase.recent.ObserveRecentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryItem(
    val id: Long,
    val filmId: String,
    val filmTitle: String,
    val openedAt: Long
)

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeRecent: ObserveRecentUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val recentViewRepository: RecentViewRepository,
    filmRepository: GhibliFilmsRepository
) : ViewModel() {

    val state: StateFlow<HistoryUiState> = combine(
        observeRecent(),
        filmRepository.getFilmsFlow()
    ) { recentViews, films ->
        val filmMap = films.associateBy { it.id }
        val items = recentViews.map { entity ->
            HistoryItem(
                id = entity.id,
                filmId = entity.filmId,
                filmTitle = filmMap[entity.filmId]?.title ?: entity.filmId,
                openedAt = entity.openedAt
            )
        }
        HistoryUiState(items = items, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun clearAll() {
        viewModelScope.launch { clearHistory() }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch { recentViewRepository.deleteById(id) }
    }
}
