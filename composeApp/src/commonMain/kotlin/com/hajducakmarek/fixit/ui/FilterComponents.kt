package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search issues...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    selectedStatus: IssueStatus?,
    selectedWorker: User?,
    workers: List<User>,
    activeFilterCount: Int,
    onStatusClick: (IssueStatus) -> Unit,
    onWorkerClick: (User) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Status filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clear all button
            if (activeFilterCount > 0) {
                FilterChip(
                    selected = false,
                    onClick = onClearFilters,
                    label = { Text("Clear all ($activeFilterCount)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear filters",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Status chips
            IssueStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusClick(status) },
                    label = { Text(status.name.replace("_", " ")) }
                )
            }
        }

        // Worker filters (if any workers exist)
        if (workers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Assigned to:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                workers.forEach { worker ->
                    FilterChip(
                        selected = selectedWorker?.id == worker.id,
                        onClick = { onWorkerClick(worker) },
                        label = { Text(worker.name) },
                        leadingIcon = {
                            Text("👷", modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}
