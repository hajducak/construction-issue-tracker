package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Switch
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.utils.PdfExporter
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.IssuePriority
import kotlinx.datetime.toLocalDateTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: IssueListViewModel,
    currentUser: com.hajducakmarek.fixit.models.User,
    onAddClick: () -> Unit = {},
    onIssueClick: (Issue) -> Unit = {},
    onLogout: () -> Unit = {},
    pdfExporter: PdfExporter
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val isExportingAll by viewModel.isExportingAll.collectAsState()
    val exportAllSuccess by viewModel.exportAllSuccess.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val showOverdueOnly by viewModel.showOverdueOnly.collectAsState()
    val dueDateFrom by viewModel.dueDateFrom.collectAsState()
    val dueDateTo by viewModel.dueDateTo.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(exportAllSuccess) {
        exportAllSuccess?.let {
            snackbarHostState.showSnackbar(
                message = "All issues exported to: ${it.split("/").lastOrNull() ?: "Documents"}",
                duration = SnackbarDuration.Long
            )
            viewModel.clearExportAllSuccess()
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
                    IconButton(
                        onClick = {
                            if (!isExportingAll) {
                                viewModel.exportAllIssuesToPdf(pdfExporter) { filePath ->
                                    // Export successful
                                }
                            }
                        },
                        enabled = !isExportingAll && issues.isNotEmpty()
                    ) {
                        if (isExportingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.weight(1f)
                )

                // Filter button with badge
                BadgedBox(
                    badge = {
                        if (viewModel.getActiveFilterCount() > 0) {
                            Badge {
                                Text(viewModel.getActiveFilterCount().toString())
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { showFilterSheet = true }
                    ) {
                        Text("🔍", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            // Filter chips showing active filters (keep this - it's useful!)
            FilterChips(
                searchQuery = searchQuery,
                selectedStatus = selectedStatus,
                selectedWorker = selectedWorker,
                selectedPriority = selectedPriority,
                showOverdueOnly = showOverdueOnly,
                dueDateFrom = dueDateFrom,
                dueDateTo = dueDateTo,
                onClearSearch = { viewModel.onSearchQueryChanged("") },
                onClearStatus = { viewModel.onStatusFilterChanged(null) },
                onClearWorker = { viewModel.onWorkerFilterChanged(null) },
                onClearPriority = { viewModel.onPriorityFilterChanged(null) },
                onClearOverdue = { viewModel.onOverdueFilterChanged(false) },
                onClearDateFrom = { viewModel.onDueDateFromChanged(null) },
                onClearDateTo = { viewModel.onDueDateToChanged(null) },
                onClearAll = viewModel::clearFilters,
                modifier = Modifier.padding(horizontal =16.dp)
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

        // Filter bottom sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                selectedStatus = selectedStatus,
                onStatusSelected = viewModel::onStatusFilterChanged,
                workers = workers,
                selectedWorker = selectedWorker,
                onWorkerSelected = viewModel::onWorkerFilterChanged,
                selectedPriority = selectedPriority,
                onPrioritySelected = viewModel::onPriorityFilterChanged,
                showOverdueOnly = showOverdueOnly,
                onOverdueChanged = viewModel::onOverdueFilterChanged,
                dueDateFrom = dueDateFrom,
                onDueDateFromChanged = viewModel::onDueDateFromChanged,
                dueDateTo = dueDateTo,
                onDueDateToChanged = viewModel::onDueDateToChanged,
                currentUserRole = currentUser.role,
                onDismiss = { showFilterSheet = false },
                onClearAll = {
                    viewModel.clearFilters()
                    showFilterSheet = false
                },
                onApply = {
                    showFilterSheet = false
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    searchQuery: String,
    selectedStatus: IssueStatus?,
    selectedWorker: User?,
    selectedPriority: IssuePriority?,
    showOverdueOnly: Boolean,
    dueDateFrom: Long?,
    dueDateTo: Long?,
    onClearSearch: () -> Unit,
    onClearStatus: () -> Unit,
    onClearWorker: () -> Unit,
    onClearPriority: () -> Unit,
    onClearOverdue: () -> Unit,
    onClearDateFrom: () -> Unit,
    onClearDateTo: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters = searchQuery.isNotEmpty() ||
            selectedStatus != null ||
            selectedWorker != null ||
            selectedPriority != null ||
            showOverdueOnly ||
            dueDateFrom != null ||
            dueDateTo != null

    if (hasFilters) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Filters",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    TextButton(onClick = onClearAll) {
                        Text("Clear All")
                    }
                }

                // Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        FilterChip(
                            selected = true,
                            onClick = onClearSearch,
                            label = { Text("Search: $searchQuery") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (selectedStatus != null) {
                        FilterChip(
                            selected = true,
                            onClick = onClearStatus,
                            label = { Text("Status: ${selectedStatus.name.replace("_", " ")}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (selectedWorker != null) {
                        FilterChip(
                            selected = true,
                            onClick = onClearWorker,
                            label = { Text("Worker: ${selectedWorker.name}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (selectedPriority != null) {
                        FilterChip(
                            selected = true,
                            onClick = onClearPriority,
                            label = { Text("Priority: ${selectedPriority.name}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (showOverdueOnly) {
                        FilterChip(
                            selected = true,
                            onClick = onClearOverdue,
                            label = { Text("Overdue Only") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (dueDateFrom != null) {
                        FilterChip(
                            selected = true,
                            onClick = onClearDateFrom,
                            label = { Text("From: ${formatDate(dueDateFrom)}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    if (dueDateTo != null) {
                        FilterChip(
                            selected = true,
                            onClick = onClearDateTo,
                            label = { Text("To: ${formatDate(dueDateTo)}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection(
    selectedStatus: IssueStatus?,
    onStatusSelected: (IssueStatus?) -> Unit,
    workers: List<User>,
    selectedWorker: User?,
    onWorkerSelected: (User?) -> Unit,
    selectedPriority: IssuePriority?,
    onPrioritySelected: (IssuePriority?) -> Unit,
    showOverdueOnly: Boolean,
    onOverdueChanged: (Boolean) -> Unit,
    dueDateFrom: Long?,
    onDueDateFromChanged: (Long?) -> Unit,
    dueDateTo: Long?,
    onDueDateToChanged: (Long?) -> Unit,
    currentUserRole: UserRole,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Status filter
            StatusFilterDropdown(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )

            // Worker filter (only for managers)
            if (currentUserRole == UserRole.MANAGER && workers.isNotEmpty()) {
                WorkerFilterDropdown(
                    workers = workers,
                    selectedWorker = selectedWorker,
                    onWorkerSelected = onWorkerSelected
                )
            }

            // Priority filter
            PriorityFilterDropdown(
                selectedPriority = selectedPriority,
                onPrioritySelected = onPrioritySelected
            )

            // Overdue toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Show overdue only")
                Switch(
                    checked = showOverdueOnly,
                    onCheckedChange = onOverdueChanged
                )
            }

            // Due date range
            DueDateRangeFilter(
                dueDateFrom = dueDateFrom,
                onDueDateFromChanged = onDueDateFromChanged,
                dueDateTo = dueDateTo,
                onDueDateToChanged = onDueDateToChanged
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterDropdown(
    selectedStatus: IssueStatus?,
    onStatusSelected: (IssueStatus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatus?.name?.replace("_", " ") ?: "All Statuses",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Status") }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Statuses") },
                onClick = {
                    onStatusSelected(null)
                    expanded = false
                }
            )
            IssueStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name.replace("_", " ")) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerFilterDropdown(
    workers: List<User>,
    selectedWorker: User?,
    onWorkerSelected: (User?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedWorker?.name ?: "All Workers",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Worker") }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Workers") },
                onClick = {
                    onWorkerSelected(null)
                    expanded = false
                }
            )
            workers.forEach { worker ->
                DropdownMenuItem(
                    text = { Text(worker.name) },
                    onClick = {
                        onWorkerSelected(worker)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityFilterDropdown(
    selectedPriority: IssuePriority?,
    onPrioritySelected: (IssuePriority?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPriority?.let { "${getPriorityIcon(it)} ${it.name}" } ?: "All Priorities",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Priority") }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Priorities") },
                onClick = {
                    onPrioritySelected(null)
                    expanded = false
                }
            )
            IssuePriority.entries.forEach { priority ->
                DropdownMenuItem(
                    text = { Text("${getPriorityIcon(priority)} ${priority.name}") },
                    onClick = {
                        onPrioritySelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateRangeFilter(
    dueDateFrom: Long?,
    onDueDateFromChanged: (Long?) -> Unit,
    dueDateTo: Long?,
    onDueDateToChanged: (Long?) -> Unit
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Due Date Range",
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // From date
            OutlinedButton(
                onClick = { showFromPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("From", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = dueDateFrom?.let { formatDate(it) } ?: "Any",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // To date
            OutlinedButton(
                onClick = { showToPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("To", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = dueDateTo?.let { formatDate(it) } ?: "Any",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Clear date range button
        if (dueDateFrom != null || dueDateTo != null) {
            TextButton(
                onClick = {
                    onDueDateFromChanged(null)
                    onDueDateToChanged(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Date Range")
            }
        }
    }

    // Date pickers
    if (showFromPicker) {
        SimpleDatePickerDialog(
            onDateSelected = { timestamp ->
                onDueDateFromChanged(timestamp)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        SimpleDatePickerDialog(
            onDateSelected = { timestamp ->
                onDueDateToChanged(timestamp)
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedStatus: IssueStatus?,
    onStatusSelected: (IssueStatus?) -> Unit,
    workers: List<User>,
    selectedWorker: User?,
    onWorkerSelected: (User?) -> Unit,
    selectedPriority: IssuePriority?,
    onPrioritySelected: (IssuePriority?) -> Unit,
    showOverdueOnly: Boolean,
    onOverdueChanged: (Boolean) -> Unit,
    dueDateFrom: Long?,
    onDueDateFromChanged: (Long?) -> Unit,
    dueDateTo: Long?,
    onDueDateToChanged: (Long?) -> Unit,
    currentUserRole: UserRole,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear All")
                }
            }

            Divider()

            // Status filter
            StatusFilterDropdown(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )

            // Worker filter (only for managers)
            if (currentUserRole == UserRole.MANAGER && workers.isNotEmpty()) {
                WorkerFilterDropdown(
                    workers = workers,
                    selectedWorker = selectedWorker,
                    onWorkerSelected = onWorkerSelected
                )
            }

            // Priority filter
            PriorityFilterDropdown(
                selectedPriority = selectedPriority,
                onPrioritySelected = onPrioritySelected
            )

            Divider()

            // Overdue toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overdue Only",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Show only overdue issues",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showOverdueOnly,
                    onCheckedChange = onOverdueChanged
                )
            }

            Divider()

            // Due date range
            Text(
                text = "Due Date Range",
                style = MaterialTheme.typography.titleMedium
            )

            DueDateRangeFilter(
                dueDateFrom = dueDateFrom,
                onDueDateFromChanged = onDueDateFromChanged,
                dueDateTo = dueDateTo,
                onDueDateToChanged = onDueDateToChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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