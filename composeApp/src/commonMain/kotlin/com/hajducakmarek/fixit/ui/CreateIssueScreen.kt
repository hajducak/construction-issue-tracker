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
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.viewmodel.CreateIssueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    viewModel: CreateIssueViewModel,
    currentUser: User,
    onNavigateBack: () -> Unit,
    onTakePhoto: (callback: (String) -> Unit) -> Unit
) {
    val flatNumber by viewModel.flatNumber.collectAsState()
    val description by viewModel.description.collectAsState()
    val photoPaths by viewModel.photoPaths.collectAsState()  // Changed to photoPaths
    val isSaving by viewModel.isSaving.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()

    val flatNumberError by viewModel.flatNumberError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveError) {
        saveError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Flat number input
            OutlinedTextField(
                value = flatNumber,
                onValueChange = viewModel::onFlatNumberChanged,
                label = { Text("Flat Number") },
                placeholder = { Text("e.g., A-101") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true,
                isError = flatNumberError != null,
                supportingText = {
                    flatNumberError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description") },
                placeholder = { Text("Describe the issue...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = !isSaving,
                maxLines = 6,
                isError = descriptionError != null,
                supportingText = {
                    if (descriptionError != null) {
                        Text(
                            text = descriptionError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "${description.length}/500 characters",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // Photo section - UPDATED for multiple photos
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Photos (${photoPaths.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = {
                                onTakePhoto { path ->
                                    viewModel.onPhotoAdded(path)
                                }
                            },
                            enabled = !isSaving
                        ) {
                            Text("📷 Add Photo")
                        }
                    }

                    if (photoPaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Photo grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            photoPaths.forEach { photoPath ->
                                PhotoItem(
                                    photoPath = photoPath,
                                    onRemove = { viewModel.onPhotoRemoved(photoPath) },
                                    enabled = !isSaving
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No photos added yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Worker assignment
            WorkerAssignmentSelector(
                workers = workers,
                selectedWorker = selectedWorker,
                onWorkerSelected = viewModel::onWorkerSelected,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    viewModel.saveIssue(
                        createdBy = currentUser.id,
                        onSuccess = onNavigateBack
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Issue")
                }
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photoPath: String,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail
                IssueImage(
                    photoPath = photoPath,
                    contentDescription = "Photo thumbnail",
                    modifier = Modifier.size(60.dp)
                )

                // Path preview
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Photo",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = photoPath.split("/").lastOrNull() ?: photoPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                enabled = enabled
            ) {
                Text("🗑️")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerAssignmentSelector(
    workers: List<User>,
    selectedWorker: User?,
    onWorkerSelected: (User?) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assign To (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded && enabled }
            ) {
                OutlinedTextField(
                    value = selectedWorker?.name ?: "Not assigned",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = enabled,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Not assigned option
                    DropdownMenuItem(
                        text = { Text("Not assigned") },
                        onClick = {
                            onWorkerSelected(null)
                            expanded = false
                        },
                        leadingIcon = {
                            if (selectedWorker == null) {
                                Text("✓")
                            }
                        }
                    )

                    HorizontalDivider()

                    // Workers
                    workers.forEach { worker ->
                        DropdownMenuItem(
                            text = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("👷")
                                    Text(worker.name)
                                }
                            },
                            onClick = {
                                onWorkerSelected(worker)
                                expanded = false
                            },
                            leadingIcon = {
                                if (worker.id == selectedWorker?.id) {
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