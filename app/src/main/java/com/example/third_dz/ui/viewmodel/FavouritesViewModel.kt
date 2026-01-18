package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.third_dz.data.model.Fact
import com.example.third_dz.ui.state.FavouritesUiState

class FavouritesViewModel(
    private val listViewModel: FactsListViewModel
) : ViewModel() {

    fun getFavouritesState(facts: List<Fact>): FavouritesUiState {
        val favouriteIds = listViewModel.getFavourites()
        val favouriteFacts = facts.filter { it.id in favouriteIds }
        return if (favouriteFacts.isEmpty()) {
            FavouritesUiState.Empty
        } else {
            FavouritesUiState.Success(favouriteFacts)
        }
    }

    fun toggleFavourite(factId: String) {
        listViewModel.toggleFavourite(factId)
    }

    fun isFavourite(factId: String): Boolean {
        return listViewModel.isFavourite(factId)
    }
}

