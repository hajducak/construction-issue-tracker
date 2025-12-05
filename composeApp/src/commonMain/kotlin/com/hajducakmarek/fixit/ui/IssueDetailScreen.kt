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
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.models.CommentWithUser
import com.hajducakmarek.fixit.viewmodel.IssueDetailViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    viewModel: IssueDetailViewModel,
    currentUser: User,
    onNavigateBack: () -> Unit
) {
    val issue by viewModel.issue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val assignedWorker by viewModel.assignedWorker.collectAsState()
    val error by viewModel.error.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val isSendingComment by viewModel.isSendingComment.collectAsState()
    val isLoadingComments by viewModel.isLoadingComments.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf<IssueStatus?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showWorkerDialog by remember { mutableStateOf(false) }
    var pendingWorker by remember { mutableStateOf<User?>(null) }
    var showDeleteCommentDialog by remember { mutableStateOf(false) }
    var pendingDeleteCommentId by remember { mutableStateOf<String?>(null) }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

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
                        containerColor = if (error != null) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (error != null) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
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
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Issue not found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            else -> {
                IssueDetailContent(
                    issue = issue!!,
                    assignedWorker = assignedWorker,
                    isSaving = isSaving,
                    canEdit = currentUser.role == UserRole.MANAGER,
                    onStatusChange = { newStatus ->
                        pendingStatus = newStatus
                        showConfirmDialog = true
                    },
                    onAssignWorker = {
                        if (currentUser.role == UserRole.MANAGER) {
                            showWorkerDialog = true
                        }
                    },
                    comments = comments,
                    commentText = commentText,
                    isSendingComment = isSendingComment,
                    isLoadingComments = isLoadingComments,
                    currentUser = currentUser,
                    onCommentTextChanged = viewModel::onCommentTextChanged,
                    onSendComment = {
                        viewModel.sendComment(currentUser.id) {
                            // Comment sent successfully
                        }
                    },
                    onDeleteComment = { commentId ->
                        pendingDeleteCommentId = commentId
                        showDeleteCommentDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }

    // Status change confirmation dialog
    if (showConfirmDialog && pendingStatus != null) {
        StatusChangeConfirmDialog(
            currentStatus = issue?.status ?: IssueStatus.OPEN,
            newStatus = pendingStatus!!,
            isSaving = isSaving,
            onConfirm = {
                val status = pendingStatus!!
                viewModel.updateStatus(status) {
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

    // Worker assignment dialog
    if (showWorkerDialog) {
        WorkerAssignmentDialog(
            workers = workers,
            currentWorker = assignedWorker,
            isSaving = isSaving,
            onAssign = { worker ->
                pendingWorker = worker
                viewModel.assignWorker(worker) {
                    showWorkerDialog = false
                    successMessage = if (worker != null) {
                        "Assigned to ${worker.name}"
                    } else {
                        "Worker unassigned"
                    }
                    showSuccessToast = true
                }
            },
            onDismiss = {
                showWorkerDialog = false
            }
        )
    }

    if (showDeleteCommentDialog && pendingDeleteCommentId != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteCommentDialog = false
                pendingDeleteCommentId = null
            },
            title = { Text("Delete Comment") },
            text = { Text("Are you sure you want to delete this comment? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteComment(pendingDeleteCommentId!!) {
                            showDeleteCommentDialog = false
                            pendingDeleteCommentId = null
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteCommentDialog = false
                        pendingDeleteCommentId = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun IssueDetailContent(
    issue: Issue,
    assignedWorker: User?,
    isSaving: Boolean,
    canEdit: Boolean,
    onStatusChange: (IssueStatus) -> Unit,
    onAssignWorker: () -> Unit,
    modifier: Modifier = Modifier,
    comments: List<CommentWithUser>,
    commentText: String,
    isSendingComment: Boolean,
    isLoadingComments: Boolean,
    currentUser: User,
    onCommentTextChanged: (String) -> Unit,
    onSendComment: () -> Unit,
    onDeleteComment: (String) -> Unit
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
            enabled = canEdit || (issue.status == IssueStatus.OPEN || issue.status == IssueStatus.IN_PROGRESS),
            onStatusChange = onStatusChange,
            isManager = canEdit
        )

        // Assigned Worker
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAssignWorker,
            enabled = canEdit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Assigned To",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assignedWorker?.name ?: "Not assigned",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (assignedWorker != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (!canEdit && assignedWorker == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Only managers can assign workers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    text = if (assignedWorker != null) "👷" else if (canEdit) "➕" else "🔒",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

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

        CommentsSection(
            comments = comments,
            commentText = commentText,
            isSendingComment = isSendingComment,
            isLoadingComments = isLoadingComments,
            currentUser = currentUser,
            onCommentTextChanged = onCommentTextChanged,
            onSendComment = onSendComment,
            onDeleteComment = onDeleteComment
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(
    currentStatus: IssueStatus,
    isSaving: Boolean,
    enabled: Boolean = true,
    onStatusChange: (IssueStatus) -> Unit,
    isManager: Boolean = true
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
                onExpandedChange = { expanded = !expanded && !isSaving && enabled }
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
                    enabled = enabled && !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Filter statuses based on role
                    val availableStatuses = if (isManager) {
                        IssueStatus.entries  // Manager sees all statuses
                    } else {
                        // Worker: show VERIFIED only if it's current status
                        IssueStatus.entries.filter { status ->
                            status != IssueStatus.VERIFIED || status == currentStatus
                        }
                    }

                    availableStatuses.forEach { status ->
                        val isCurrentStatus = status == currentStatus

                        // Determine if worker can select this status
                        val canWorkerSelect = when (currentStatus) {
                            IssueStatus.OPEN -> status == IssueStatus.IN_PROGRESS
                            IssueStatus.IN_PROGRESS -> status == IssueStatus.FIXED
                            IssueStatus.FIXED -> false  // Worker can't change from FIXED
                            IssueStatus.VERIFIED -> false  // Can't change from VERIFIED
                        }

                        val canSelect = if (isManager) {
                            !isCurrentStatus  // Manager can select any except current
                        } else {
                            canWorkerSelect  // Worker has restrictions
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = status.name.replace("_", " "),
                                    color = if (canSelect) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            },
                            onClick = {
                                if (canSelect) {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            },
                            leadingIcon = {
                                if (isCurrentStatus) {
                                    Text("✓")
                                }
                            },
                            enabled = canSelect
                        )
                    }
                }
            }

            if (!enabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contact manager to change status",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!isManager) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (currentStatus) {
                        IssueStatus.OPEN -> "You can mark as IN PROGRESS"
                        IssueStatus.IN_PROGRESS -> "You can mark as FIXED"
                        IssueStatus.FIXED -> "Manager must verify issue"
                        IssueStatus.VERIFIED -> "Issue completed"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
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

@Composable
private fun WorkerAssignmentDialog(
    workers: List<User>,
    currentWorker: User?,
    isSaving: Boolean,
    onAssign: (User?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Assign Worker") },
        text = {
            Column {
                // Unassign option
                TextButton(
                    onClick = { onAssign(null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (currentWorker == null) "✓ Not assigned" else "Unassign",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // Worker list
                workers.forEach { worker ->
                    TextButton(
                        onClick = { onAssign(worker) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(worker.name)
                            if (worker.id == currentWorker?.id) {
                                Text("✓")
                            }
                        }
                    }
                }

                if (isSaving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        confirmButton = {},
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

@Composable
private fun CommentsSection(
    comments: List<CommentWithUser>,
    commentText: String,
    isSendingComment: Boolean,
    isLoadingComments: Boolean,
    currentUser: User,
    onCommentTextChanged: (String) -> Unit,
    onSendComment: () -> Unit,
    onDeleteComment: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Comments (${comments.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comment input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChanged,
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f),
                    enabled = !isSendingComment,
                    maxLines = 3
                )

                IconButton(
                    onClick = onSendComment,
                    enabled = commentText.isNotBlank() && !isSendingComment
                ) {
                    if (isSendingComment) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("➤", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comments list
            when {
                isLoadingComments -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                comments.isEmpty() -> {
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        comments.forEach { commentWithUser ->
                            CommentItem(
                                commentWithUser = commentWithUser,
                                currentUser = currentUser,
                                onDelete = { onDeleteComment(commentWithUser.comment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    commentWithUser: CommentWithUser,
    currentUser: User,
    onDelete: () -> Unit
) {
    val comment = commentWithUser.comment
    val user = commentWithUser.user
    val isOwnComment = comment.userId == currentUser.id
    val canDelete = isOwnComment || currentUser.role == UserRole.MANAGER

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwnComment) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (user.role == UserRole.MANAGER) "👔" else "👷",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = user.role.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("🗑️", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDate(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}