package com.example.third_dz.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.model.Film
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmsListScreen(
    state: FilmsListUiState,
    searchQuery: String,
    statusFilter: WatchStatus?,
    onEvent: (FilmsListEvent) -> Unit,
    onFilmClick: (String) -> Unit,
    onFavouritesClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRefreshing = (state as? FilmsListUiState.Success)?.isRefreshing == true
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studio Ghibli Films") },
                actions = {
                    IconButton(onClick = { onEvent(FilmsListEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onCollectionsClick) {
                        Icon(Icons.Default.Folder, contentDescription = "Collections")
                    }
                    IconButton(onClick = onFavouritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favourites")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onEvent(FilmsListEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search films...") },
                singleLine = true
            )
            StatusFilterRow(current = statusFilter) { selected ->
                onEvent(FilmsListEvent.StatusFilterChanged(selected))
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is FilmsListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is FilmsListUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Error: ${state.message}", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onEvent(FilmsListEvent.Refresh) }) { Text("Retry") }
                        }
                    }
                    is FilmsListUiState.Empty -> {
                        Text("No films found", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
                    }
                    is FilmsListUiState.Success -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = { onEvent(FilmsListEvent.Refresh) }
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.films) { film ->
                                    FilmItem(
                                        film = film,
                                        record = state.records[film.id],
                                        onFilmClick = { onFilmClick(film.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterRow(current: WatchStatus?, onPick: (WatchStatus?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = current == null,
                onClick = { onPick(null) },
                label = { Text("All") }
            )
        }
        items(WatchStatus.entries) { status ->
            FilterChip(
                selected = current == status,
                onClick = { onPick(if (current == status) null else status) },
                label = { Text(status.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilmItem(
    film: Film,
    record: UserFilmRecord?,
    onFilmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onFilmClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(film.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(film.original_title_romanised, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(film.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                }
                if (record?.status != null) {
                    AssistChip(
                        onClick = onFilmClick,
                        label = { Text(record.status.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Director", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(film.director, style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("Release", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(film.release_date, style = MaterialTheme.typography.bodySmall)
                }
                Column {
                    Text("Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(film.rt_score, style = MaterialTheme.typography.bodySmall)
                }
                if (record?.rating != null) {
                    Column {
                        Text("Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${record.rating}/10", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
