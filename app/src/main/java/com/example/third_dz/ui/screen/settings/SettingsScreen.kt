package com.example.third_dz.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.third_dz.data.model.SortOrder
import com.example.third_dz.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    cacheTtlMinutes: Long,
    wifiOnlyRefresh: Boolean,
    refreshIntervalHours: Long,
    defaultListSort: SortOrder,
    historyLimit: Int,
    onThemeModeChange: (ThemeMode) -> Unit,
    onCacheTtlMinutesChange: (Long) -> Unit,
    onWifiOnlyRefreshChange: (Boolean) -> Unit,
    onRefreshIntervalHoursChange: (Long) -> Unit,
    onDefaultListSortChange: (SortOrder) -> Unit,
    onHistoryLimitChange: (Int) -> Unit,
    onClearAllUserData: () -> Unit,
    onRefreshUniverse: () -> Unit,
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri) -> Unit,
    onBackClick: () -> Unit
) {
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExportBackup) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onImportBackup) }
    var themeExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = themeExpanded,
                onExpandedChange = { themeExpanded = it }
            ) {
                OutlinedTextField(
                    value = when (themeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System default"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Theme") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onThemeModeChange(mode)
                                themeExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Data", style = MaterialTheme.typography.titleMedium)

            OutlinedButton(
                onClick = onRefreshUniverse,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh universe now")
            }

            OutlinedButton(
                onClick = { exportLauncher.launch("ghibli_backup.json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export backup")
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import backup")
            }

            OutlinedButton(
                onClick = onClearAllUserData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear all user data")
            }

            HorizontalDivider()

            Text("List", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = sortExpanded,
                onExpandedChange = { sortExpanded = it }
            ) {
                OutlinedTextField(
                    value = defaultListSort.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default sort order") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label) },
                            onClick = {
                                onDefaultListSortChange(order)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Cache", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = cacheTtlMinutes.toString(),
                    onValueChange = { it.toLongOrNull()?.let(onCacheTtlMinutesChange) },
                    label = { Text("TTL (min)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = refreshIntervalHours.toString(),
                    onValueChange = { it.toLongOrNull()?.let(onRefreshIntervalHoursChange) },
                    label = { Text("Refresh interval (h)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wi-Fi only refresh")
                Spacer(Modifier.weight(1f))
                Switch(checked = wifiOnlyRefresh, onCheckedChange = onWifiOnlyRefreshChange)
            }

            HorizontalDivider()

            Text("History", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = historyLimit.toString(),
                onValueChange = { it.toIntOrNull()?.let(onHistoryLimitChange) },
                label = { Text("History limit") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
