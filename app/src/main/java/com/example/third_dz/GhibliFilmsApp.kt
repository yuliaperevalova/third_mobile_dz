package com.example.third_dz

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.third_dz.di.AppModule
import com.example.third_dz.navigation.NavGraph
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

@Composable
fun GhibliFilmsApp() {
    val listViewModel: FilmsListViewModel = viewModel {
        FilmsListViewModel(AppModule.ghibliFilmsRepository)
    }
    
    val detailViewModel: FilmDetailViewModel = viewModel {
        FilmDetailViewModel(AppModule.ghibliFilmsRepository)
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

