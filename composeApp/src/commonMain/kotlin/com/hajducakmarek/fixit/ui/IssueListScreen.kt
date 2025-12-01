package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: IssueListViewModel,
    onAddClick: () -> Unit = {},
    onIssueClick: (Issue) -> Unit = {}
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    // Refresh when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadIssues()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issues") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter chips
            FilterChipsRow(
                selectedStatus = selectedStatus,
                selectedWorker = selectedWorker,
                workers = workers,
                activeFilterCount = activeFilterCount,
                onStatusClick = viewModel::onStatusFilterChanged,
                onWorkerClick = viewModel::onWorkerFilterChanged,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Issues list
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                issues.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (activeFilterCount > 0) {
                                "No issues match your filters"
                            } else {
                                "No issues yet. Tap + to create one."
                            }
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(issues) { issue ->
                            IssueCard(
                                issue = issue,
                                onClick = { onIssueClick(issue) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(
    issue: Issue,
    onClick: () -> Unit = {}
) {
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    val hasPhoto = issue.photoPath.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (hasPhoto) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IssueImage(
                    photoPath = issue.photoPath,
                    contentDescription = "Issue photo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFullScreenPhoto = true }
                )
                Spacer(modifier = Modifier.width(16.dp))

                IssueDetails(issue = issue, modifier = Modifier.weight(1f))
            }

            if (showFullScreenPhoto) {
                FullScreenPhotoDialog(
                    photoPath = issue.photoPath,
                    onDismiss = { showFullScreenPhoto = false }
                )
            }
        } else {
            IssueDetails(
                issue = issue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun IssueDetails(issue: Issue, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = issue.flatNumber,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = issue.description,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = issue.status.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = when(issue.status) {
                com.hajducakmarek.fixit.models.IssueStatus.OPEN ->
                    MaterialTheme.colorScheme.error
                com.hajducakmarek.fixit.models.IssueStatus.IN_PROGRESS ->
                    MaterialTheme.colorScheme.tertiary
                com.hajducakmarek.fixit.models.IssueStatus.FIXED ->
                    MaterialTheme.colorScheme.primary
                com.hajducakmarek.fixit.models.IssueStatus.VERIFIED ->
                    MaterialTheme.colorScheme.secondary
            }
        )
    }
}