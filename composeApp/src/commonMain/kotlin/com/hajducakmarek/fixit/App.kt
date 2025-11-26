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
    }
}