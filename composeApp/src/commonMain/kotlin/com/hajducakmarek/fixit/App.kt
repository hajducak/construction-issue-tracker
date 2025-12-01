package com.hajducakmarek.fixit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.ui.CreateIssueScreen
import com.hajducakmarek.fixit.ui.IssueDetailScreen
import com.hajducakmarek.fixit.ui.IssueListScreen
import com.hajducakmarek.fixit.ui.WorkerListScreen
import com.hajducakmarek.fixit.ui.AddWorkerScreen
import com.hajducakmarek.fixit.viewmodel.AddWorkerViewModel
import com.hajducakmarek.fixit.viewmodel.CreateIssueViewModel
import com.hajducakmarek.fixit.viewmodel.IssueDetailViewModel
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import com.hajducakmarek.fixit.viewmodel.WorkerListViewModel
import com.hajducakmarek.fixit.platform.ImagePicker

sealed class Screen {
    object Issues : Screen()
    object Workers : Screen()
    object Create : Screen()
    data class IssueDetail(val issueId: String) : Screen()
    object AddWorker : Screen()
}

enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
) {
    ISSUES("Issues", Icons.Filled.List, Icons.Outlined.List, Screen.Issues),
    WORKERS("Workers", Icons.Filled.Person, Icons.Outlined.Person, Screen.Workers)
}

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    imagePicker: ImagePicker
) {
    MaterialTheme {
        val repository = remember { IssueRepository(databaseDriverFactory) }

        // Create ViewModels OUTSIDE the when statement so they persist
        val listViewModel = remember { IssueListViewModel(repository) }
        val workerListViewModel = remember { WorkerListViewModel(repository) }

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Issues) }
        var selectedTab by remember { mutableStateOf(BottomNavItem.ISSUES) }

        // Show bottom nav only on main screens
        val showBottomNav = when (currentScreen) {
            is Screen.Issues, is Screen.Workers -> true
            else -> false
        }

        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar {
                        BottomNavItem.entries.forEach { item ->
                            NavigationBarItem(
                                selected = selectedTab == item,
                                onClick = {
                                    selectedTab = item
                                    currentScreen = item.screen
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == item) {
                                            item.selectedIcon
                                        } else {
                                            item.unselectedIcon
                                        },
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (val screen = currentScreen) {
                    is Screen.Issues -> {
                        IssueListScreen(
                            viewModel = listViewModel,  // Reuse same instance
                            onAddClick = { currentScreen = Screen.Create },
                            onIssueClick = { issue ->
                                currentScreen = Screen.IssueDetail(issue.id)
                            }
                        )
                    }

                    is Screen.Workers -> {
                        WorkerListScreen(
                            viewModel = workerListViewModel,  // Reuse same instance
                            onAddClick = { currentScreen = Screen.AddWorker },
                            onWorkerClick = { worker ->
                                // Future: worker detail screen
                            }
                        )
                    }

                    is Screen.Create -> {
                        val createViewModel = remember { CreateIssueViewModel(repository) }

                        CreateIssueScreen(
                            viewModel = createViewModel,
                            onNavigateBack = {
                                currentScreen = Screen.Issues
                                selectedTab = BottomNavItem.ISSUES
                                listViewModel.loadIssues()  // Refresh list
                            },
                            onTakePhoto = { callback ->
                                imagePicker.pickImage(callback)
                            }
                        )
                    }

                    is Screen.IssueDetail -> {
                        val detailViewModel = remember(screen.issueId) {
                            IssueDetailViewModel(repository, screen.issueId)
                        }

                        IssueDetailScreen(
                            viewModel = detailViewModel,
                            onNavigateBack = {
                                currentScreen = Screen.Issues
                                selectedTab = BottomNavItem.ISSUES
                                listViewModel.loadIssues()  // Refresh list but keep filters
                            }
                        )
                    }

                    is Screen.AddWorker -> {
                        val addWorkerViewModel = remember { AddWorkerViewModel(repository) }

                        AddWorkerScreen(
                            viewModel = addWorkerViewModel,
                            onNavigateBack = {
                                currentScreen = Screen.Workers
                                selectedTab = BottomNavItem.WORKERS
                                workerListViewModel.loadWorkers()  // Refresh workers
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerPlaceholder(onNavigateBack: () -> Unit) {
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Add Worker screen coming soon...")
        }
    }
}

@Composable
expect fun CameraOverlay(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
)