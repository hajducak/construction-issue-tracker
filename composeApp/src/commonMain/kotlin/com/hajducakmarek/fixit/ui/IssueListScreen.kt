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
import com.hajducakmarek.fixit.models.UserRole
import kotlinx.datetime.toLocalDateTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: IssueListViewModel,
    currentUser: com.hajducakmarek.fixit.models.User,
    onAddClick: () -> Unit = {},
    onIssueClick: (Issue) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh when screen appears
    LaunchedEffect(loadError) {
        loadError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadIssues()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issues") },
                actions = {
                    Text(
                        text = "${currentUser.name} (${currentUser.role.name})",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onLogout) {
                        Text("🚪")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser.role == UserRole.MANAGER) {
                FloatingActionButton(onClick = onAddClick) {
                    Text("+")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                workers = if (currentUser.role == UserRole.MANAGER) workers else emptyList(),  // Hide for workers
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    activeFilterCount > 0 -> "No issues match your filters"
                                    currentUser.role == UserRole.WORKER -> "No issues assigned to you yet"
                                    else -> "No issues yet. Tap + to create one."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
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
    val hasPhoto = "".isNotEmpty()

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
                    photoPath = "",
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
                    photoPath = "",
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = issue.flatNumber,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Priority badge
            PriorityBadge(priority = issue.priority)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = issue.description,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            // Due date indicator
            issue.dueDate?.let { dueDate ->
                DueDateIndicator(dueDate = dueDate, status = issue.status)
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: com.hajducakmarek.fixit.models.IssuePriority) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (priority) {
                com.hajducakmarek.fixit.models.IssuePriority.LOW ->
                    MaterialTheme.colorScheme.secondaryContainer
                com.hajducakmarek.fixit.models.IssuePriority.MEDIUM ->
                    MaterialTheme.colorScheme.tertiaryContainer
                com.hajducakmarek.fixit.models.IssuePriority.HIGH ->
                    MaterialTheme.colorScheme.primaryContainer
                com.hajducakmarek.fixit.models.IssuePriority.URGENT ->
                    MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = getPriorityIcon(priority),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = priority.name,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun DueDateIndicator(
    dueDate: Long,
    status: com.hajducakmarek.fixit.models.IssueStatus
) {
    val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val isOverdue = dueDate < now && status != com.hajducakmarek.fixit.models.IssueStatus.VERIFIED

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverdue) "⚠️" else "📅",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = formatDate(dueDate),
            style = MaterialTheme.typography.labelSmall,
            color = if (isOverdue) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun getPriorityIcon(priority: com.hajducakmarek.fixit.models.IssuePriority): String {
    return when (priority) {
        com.hajducakmarek.fixit.models.IssuePriority.LOW -> "🟢"
        com.hajducakmarek.fixit.models.IssuePriority.MEDIUM -> "🟡"
        com.hajducakmarek.fixit.models.IssuePriority.HIGH -> "🟠"
        com.hajducakmarek.fixit.models.IssuePriority.URGENT -> "🔴"
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
}