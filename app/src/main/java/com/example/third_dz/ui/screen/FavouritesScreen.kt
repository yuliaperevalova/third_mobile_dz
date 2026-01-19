package com.example.third_dz.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.third_dz.ui.state.FavouritesUiState
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    listViewModel: FilmsListViewModel,
    favouritesViewModel: FavouritesViewModel,
    onFilmClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listUiState by listViewModel.uiState.collectAsStateWithLifecycle()
    val favourites by listViewModel.favourites.collectAsStateWithLifecycle()
    
    val favouriteFilms = when (val state = listUiState) {
        is com.example.third_dz.ui.state.FilmsListUiState.Success -> {
            val favouriteFilmsList = state.films.filter { it.id in favourites }
            if (favouriteFilmsList.isEmpty()) {
                FavouritesUiState.Empty
            } else {
                FavouritesUiState.Success(favouriteFilmsList)
            }
        }
        else -> FavouritesUiState.Empty
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (favouriteFilms) {
                is FavouritesUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No favourites yet",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is FavouritesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favouriteFilms.films) { film ->
                            FilmItem(
                                film = film,
                                isFavourite = true,
                                onFilmClick = { onFilmClick(film.id) },
                                onFavouriteClick = { favouritesViewModel.toggleFavourite(film.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

