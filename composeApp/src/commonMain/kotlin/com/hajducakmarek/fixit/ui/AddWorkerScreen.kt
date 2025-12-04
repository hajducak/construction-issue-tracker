package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.viewmodel.AddWorkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(
    viewModel: AddWorkerViewModel,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    // Show save error in snackbar
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
                title = { Text("Add Worker") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name input with validation
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name") },
                placeholder = { Text("e.g., John Smith") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    nameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            // Role selector
            RoleSelector(
                selectedRole = selectedRole,
                onRoleSelected = viewModel::onRoleChanged,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.saveWorker(onNavigateBack) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Worker")
                }
            }
        }
    }
}

@Composable
private fun RoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Role",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            UserRole.entries.forEach { role ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedRole == role,
                        onClick = { onRoleSelected(role) },
                        enabled = enabled
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = role.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}