package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.viewmodel.IssueDetailViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    viewModel: IssueDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val issue by viewModel.issue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf<IssueStatus?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Show success toast
    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
            showSuccessToast = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            issue == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Issue not found")
                }
            }
            else -> {
                IssueDetailContent(
                    issue = issue!!,
                    isSaving = isSaving,
                    onStatusChange = { newStatus ->
                        pendingStatus = newStatus
                        showConfirmDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }

    // Confirmation dialog
    if (showConfirmDialog && pendingStatus != null) {
        StatusChangeConfirmDialog(
            currentStatus = issue?.status ?: IssueStatus.OPEN,
            newStatus = pendingStatus!!,
            isSaving = isSaving,
            onConfirm = {
                val status = pendingStatus!!
                viewModel.updateStatus(status) {
                    // Called after successful save
                    showConfirmDialog = false
                    successMessage = "Status updated to ${status.name.replace("_", " ")}"
                    showSuccessToast = true
                }
            },
            onDismiss = {
                showConfirmDialog = false
                pendingStatus = null
            }
        )
    }
}

@Composable
private fun IssueDetailContent(
    issue: Issue,
    isSaving: Boolean,
    onStatusChange: (IssueStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo (if exists)
        if (issue.photoPath.isNotEmpty()) {
            IssueImage(
                photoPath = issue.photoPath,
                contentDescription = "Issue photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        // Flat Number
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Flat Number",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = issue.flatNumber,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // Description
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Status with dropdown
        StatusSelector(
            currentStatus = issue.status,
            isSaving = isSaving,
            onStatusChange = onStatusChange
        )

        // Created date
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Created",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(issue.createdAt),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Assigned to (if any)
        if (issue.assignedTo != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Assigned To",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.assignedTo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(
    currentStatus: IssueStatus,
    isSaving: Boolean,
    onStatusChange: (IssueStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded && !isSaving }
            ) {
                OutlinedTextField(
                    value = currentStatus.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    IssueStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.replace("_", " ")) },
                            onClick = {
                                onStatusChange(status)
                                expanded = false
                            },
                            leadingIcon = {
                                if (status == currentStatus) {
                                    Text("✓")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChangeConfirmDialog(
    currentStatus: IssueStatus,
    newStatus: IssueStatus,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Confirm Status Change") },
        text = {
            Column {
                Text(
                    "Change status from ${currentStatus.name.replace("_", " ")} to ${newStatus.name.replace("_", " ")}?"
                )

                if (isSaving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Updating...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}