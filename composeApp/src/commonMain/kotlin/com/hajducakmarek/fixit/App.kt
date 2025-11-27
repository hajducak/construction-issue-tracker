package com.hajducakmarek.fixit

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.ui.CreateIssueScreen
import com.hajducakmarek.fixit.ui.IssueDetailScreen
import com.hajducakmarek.fixit.ui.IssueListScreen
import com.hajducakmarek.fixit.viewmodel.CreateIssueViewModel
import com.hajducakmarek.fixit.viewmodel.IssueDetailViewModel
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import com.hajducakmarek.fixit.platform.ImagePicker

sealed class Screen {
    object List : Screen()
    object Create : Screen()
    data class Detail(val issueId: String) : Screen()
}

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    imagePicker: ImagePicker
) {
    MaterialTheme {
        val repository = remember { IssueRepository(databaseDriverFactory) }
        var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

        when (val screen = currentScreen) {
            is Screen.List -> {
                val listViewModel = remember { IssueListViewModel(repository) }

                IssueListScreen(
                    viewModel = listViewModel,
                    onAddClick = { currentScreen = Screen.Create },
                    onIssueClick = { issue ->
                        currentScreen = Screen.Detail(issue.id)
                    }
                )
            }

            is Screen.Create -> {
                val createViewModel = remember { CreateIssueViewModel(repository) }

                CreateIssueScreen(
                    viewModel = createViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.List
                    },
                    onTakePhoto = { callback ->
                        imagePicker.pickImage(callback)
                    }
                )
            }

            is Screen.Detail -> {
                val detailViewModel = remember(screen.issueId) {
                    IssueDetailViewModel(repository, screen.issueId)
                }

                IssueDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.List
                    }
                )
            }
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