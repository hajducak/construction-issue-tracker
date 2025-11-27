package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.viewmodel.CreateIssueViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    viewModel: CreateIssueViewModel,
    onNavigateBack: () -> Unit,
    onTakePhoto: ((String?) -> Unit) -> Unit
) {
    val photoPath by viewModel.photoPath.collectAsState()
    val description by viewModel.description.collectAsState()
    val flatNumber by viewModel.flatNumber.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2190")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo button
            Button(
                onClick = { onTakePhoto { path -> viewModel.onPhotoSelected(path) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (photoPath != null) "Photo Taken \u2713" else "Take Photo")
            }

            photoPath?.let { path ->
                Spacer(modifier = Modifier.height(8.dp))
                IssueImage(
                    photoPath = path,
                    contentDescription = "Captured photo preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // Flat number input
            OutlinedTextField(
                value = flatNumber,
                onValueChange = viewModel::onFlatNumberChanged,
                label = { Text("Flat Number") },
                placeholder = { Text("e.g., A-101") },
                modifier = Modifier.fillMaxWidth()
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
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.saveIssue(onNavigateBack) },
                enabled = !isSaving && description.isNotBlank() && flatNumber.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Issue")
                }
            }
        }
    }
}
