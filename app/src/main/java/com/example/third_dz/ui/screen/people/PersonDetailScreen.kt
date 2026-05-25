package com.example.third_dz.ui.screen.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.third_dz.data.model.Person

@Composable
fun PersonDetailScreen(
    person: Person?,
    isPinned: Boolean,
    onTogglePin: (String?) -> Unit,
    onFilmClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (person == null) {
        Text("Loading...", modifier = modifier.padding(16.dp))
        return
    }

    var note by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with pin button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { onTogglePin(note.takeIf { it.isNotBlank() }) }) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isPinned) "Unpin" else "Pin"
                )
            }
        }

        // Info card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Gender", person.gender)
                InfoRow("Age", person.age)
                InfoRow("Eye Color", person.eye_color)
                InfoRow("Hair Color", person.hair_color)
            }
        }

        // Note field
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Films
        if (person.films.isNotEmpty()) {
            Text("Films", style = MaterialTheme.typography.titleMedium)
            person.films.forEach { filmUrl ->
                Text(
                    text = filmUrl.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
