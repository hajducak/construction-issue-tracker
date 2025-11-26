package com.hajducakmarek.fixit

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.ui.CreateIssueScreen
import com.hajducakmarek.fixit.ui.IssueListScreen
import com.hajducakmarek.fixit.viewmodel.CreateIssueViewModel
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import kotlinx.datetime.Clock
import kotlinx.coroutines.launch

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    imagePicker: ImagePicker
) {
    MaterialTheme {
        val repository = remember { IssueRepository(databaseDriverFactory) }
        val listViewModel = remember { IssueListViewModel(repository) }
        val createViewModel = remember { CreateIssueViewModel(repository) }

        var showCreateScreen by remember { mutableStateOf(false) }

        // Main content
        if (showCreateScreen) {
            CreateIssueScreen(
                viewModel = createViewModel,
                onNavigateBack = {
                    showCreateScreen = false
                    listViewModel.loadIssues()
                },
                onTakePhoto = { callback ->
                    imagePicker.pickImage(callback)
                }
            )
        } else {
            IssueListScreen(
                viewModel = listViewModel,
                onAddClick = { showCreateScreen = true }
            )
        }

        // Camera overlay (Android only)
        if (imagePicker.showCamera) {
            CameraOverlay(
                onPhotoCaptured = { path -> imagePicker.onPhotoCaptured(path) },
                onCancel = { imagePicker.onCameraCancel() }
            )
        }
    }
}

@Composable
expect fun CameraOverlay(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
)