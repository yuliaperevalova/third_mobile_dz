package com.example.third_dz.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.ui.event.FilmDetailEvent
import com.example.third_dz.ui.state.FilmDetailUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun FilmDetailScreen(
    filmId: String,
    state: FilmDetailUiState,
    collections: List<CollectionEntity>,
    memberIds: Set<Long>,
    onToggleCollection: (collectionId: Long, currentlyMember: Boolean) -> Unit,
    onEvent: (FilmDetailEvent) -> Unit,
    onLoadFilm: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(filmId) { onLoadFilm(filmId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Film Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is FilmDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FilmDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: ${state.message}", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onEvent(FilmDetailEvent.Retry) }) { Text("Retry") }
                    }
                }
                is FilmDetailUiState.Empty -> {
                    Text(
                        text = "Film not found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is FilmDetailUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { FilmHeaderCard(state) }
                        item { MyRecordCard(filmId, state.record, onEvent) }
                        item { CollectionsCard(collections, memberIds, onToggleCollection) }
                        item { FilmInfoCard(state) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilmHeaderCard(state: FilmDetailUiState.Success) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(state.film.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(state.film.original_title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.film.original_title_romanised, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider()
            Text(state.film.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FilmInfoCard(state: FilmDetailUiState.Success) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Film Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Divider()
            InfoRow("Director", state.film.director)
            InfoRow("Producer", state.film.producer)
            InfoRow("Release Date", state.film.release_date)
            InfoRow("Running Time", "${state.film.running_time} minutes")
            InfoRow("Rotten Tomatoes Score", state.film.rt_score)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionsCard(
    collections: List<CollectionEntity>,
    memberIds: Set<Long>,
    onToggle: (Long, Boolean) -> Unit
) {
    if (collections.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("In Collections", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Divider()
            collections.forEach { collection ->
                val isMember = collection.id in memberIds
                FilterChip(
                    selected = isMember,
                    onClick = { onToggle(collection.id, isMember) },
                    label = { Text(collection.name) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
private fun MyRecordCard(
    filmId: String,
    record: UserFilmRecord?,
    onEvent: (FilmDetailEvent) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("My Record", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Divider()
            StatusChips(current = record?.status) { status ->
                onEvent(FilmDetailEvent.SetStatus(filmId, status))
            }
            RatingSlider(current = record?.rating) { rating ->
                onEvent(FilmDetailEvent.SetRating(filmId, rating))
            }
            NoteField(filmId = filmId, initial = record?.note.orEmpty(), onEvent = onEvent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChips(current: WatchStatus?, onPick: (WatchStatus) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WatchStatus.entries.forEach { status ->
                FilterChip(
                    selected = current == status,
                    onClick = { onPick(status) },
                    label = { Text(status.name) }
                )
            }
        }
    }
}

@Composable
private fun RatingSlider(current: Int?, onPick: (Int?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rating: ${current?.toString() ?: "—"}", style = MaterialTheme.typography.labelLarge)
            if (current != null) {
                TextButton(onClick = { onPick(null) }) { Text("Clear") }
            }
        }
        Slider(
            value = (current ?: 0).toFloat(),
            onValueChange = { v ->
                val rounded = v.toInt().coerceIn(0, 10)
                onPick(if (rounded == 0) null else rounded)
            },
            valueRange = 0f..10f,
            steps = 9
        )
    }
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
private fun NoteField(filmId: String, initial: String, onEvent: (FilmDetailEvent) -> Unit) {
    var text by remember(filmId) { mutableStateOf(initial) }
    val flow = remember(filmId) { MutableSharedFlow<String>(extraBufferCapacity = 64) }
    LaunchedEffect(filmId) {
        flow.debounce(500).distinctUntilChanged().collect { value ->
            onEvent(FilmDetailEvent.SetNote(filmId, value.ifBlank { null }))
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new
            flow.tryEmit(new)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Note") },
        minLines = 2,
        maxLines = 5
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}
