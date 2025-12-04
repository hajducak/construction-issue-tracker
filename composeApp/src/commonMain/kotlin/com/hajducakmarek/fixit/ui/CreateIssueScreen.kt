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
    val photoPath by viewModel.photoPath.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val selectedWorker by viewModel.selectedWorker.collectAsState()

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
            // Flat number input
            OutlinedTextField(
                value = flatNumber,
                onValueChange = viewModel::onFlatNumberChanged,
                label = { Text("Flat Number") },
                placeholder = { Text("e.g., A-101") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true
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
                maxLines = 6
            )

            // Photo section
            Button(
                onClick = {
                    onTakePhoto { path ->
                        viewModel.onPhotoSelected(path)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (photoPath.isEmpty()) "📷 Take Photo" else "📷 Retake Photo")
            }

            if (photoPath.isNotEmpty()) {
                IssueImage(
                    photoPath = photoPath,
                    contentDescription = "Selected photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Worker assignment (Optional)
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
                        createdBy = currentUser.id,  // Use actual logged-in user
                        onSuccess = onNavigateBack
                    )
                },
                enabled = !isSaving && flatNumber.isNotBlank() && description.isNotBlank(),
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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