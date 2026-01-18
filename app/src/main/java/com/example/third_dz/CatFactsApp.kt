package com.example.third_dz

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.third_dz.di.AppModule
import com.example.third_dz.navigation.NavGraph
import com.example.third_dz.ui.viewmodel.FactDetailViewModel
import com.example.third_dz.ui.viewmodel.FactsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

@Composable
fun CatFactsApp() {
    val listViewModel: FactsListViewModel = viewModel {
        FactsListViewModel(AppModule.catFactsRepository)
    }
    
    val detailViewModel: FactDetailViewModel = viewModel {
        FactDetailViewModel(AppModule.catFactsRepository)
    }
    
    val favouritesViewModel: FavouritesViewModel = viewModel {
        FavouritesViewModel(listViewModel)
    }
    
    NavGraph(
        listViewModel = listViewModel,
        detailViewModel = detailViewModel,
        favouritesViewModel = favouritesViewModel
    )
}

