package com.hajducakmarek.fixit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.hajducakmarek.fixit.session.UserSession
import com.hajducakmarek.fixit.ui.*
import com.hajducakmarek.fixit.viewmodel.*
import com.hajducakmarek.fixit.platform.ImagePicker
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole

sealed class Screen {
    object Login : Screen()
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
    imagePicker: ImagePicker,
    userSession: UserSession
) {
    MaterialTheme {
        val repository = remember { IssueRepository(databaseDriverFactory) }

        // Check if user is logged in - with explicit types
        var isLoggedIn by remember { mutableStateOf<Boolean>(userSession.isLoggedIn()) }
        var currentUser by remember { mutableStateOf<User?>(userSession.getCurrentUser()) }

        if (!isLoggedIn || currentUser == null) {
            // Show login screen
            val loginViewModel = remember { LoginViewModel(repository, userSession) }

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    currentUser = userSession.getCurrentUser()
                    isLoggedIn = true
                }
            )
        } else {
            // Show main app
            MainApp(
                repository = repository,
                imagePicker = imagePicker,
                userSession = userSession,
                currentUser = currentUser!!,
                onLogout = {
                    userSession.clearUser()
                    isLoggedIn = false
                    currentUser = null
                }
            )
        }
    }
}

@Composable
private fun MainApp(
    repository: IssueRepository,
    imagePicker: ImagePicker,
    userSession: UserSession,
    currentUser: User,
    onLogout: () -> Unit
) {
    // ViewModels persist across navigation
    val listViewModel = remember { IssueListViewModel(repository, currentUser) }
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
                        // Hide Workers tab for workers (they can't manage workers)
                        if (item == BottomNavItem.WORKERS && currentUser.role == UserRole.WORKER) {
                            return@forEach
                        }

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
                is Screen.Login -> { /* Handled above */ }

                is Screen.Issues -> {
                    IssueListScreen(
                        viewModel = listViewModel,
                        currentUser = currentUser,
                        onAddClick = {
                            // Only managers can create issues
                            if (currentUser.role == UserRole.MANAGER) {
                                currentScreen = Screen.Create
                            }
                        },
                        onIssueClick = { issue ->
                            currentScreen = Screen.IssueDetail(issue.id)
                        },
                        onLogout = onLogout
                    )
                }

                is Screen.Workers -> {
                    WorkerListScreen(
                        viewModel = workerListViewModel,
                        onAddClick = {
                            // Only managers can add workers
                            if (currentUser.role == UserRole.MANAGER) {
                                currentScreen = Screen.AddWorker
                            }
                        },
                        onWorkerClick = { worker ->
                            // Future: worker detail screen
                        }
                    )
                }

                is Screen.Create -> {
                    val createViewModel = remember { CreateIssueViewModel(repository) }

                    CreateIssueScreen(
                        viewModel = createViewModel,
                        currentUser = currentUser,
                        onNavigateBack = {
                            currentScreen = Screen.Issues
                            selectedTab = BottomNavItem.ISSUES
                            listViewModel.loadIssues()
                        },
                        onTakePhoto = { callback ->
                            imagePicker.pickImage { path ->
                                path?.let { callback(it) }
                            }
                        }
                    )
                }

                is Screen.IssueDetail -> {
                    val detailViewModel = remember(screen.issueId) {
                        IssueDetailViewModel(repository, screen.issueId)
                    }

                    IssueDetailScreen(
                        viewModel = detailViewModel,
                        currentUser = currentUser,
                        onNavigateBack = {
                            currentScreen = Screen.Issues
                            selectedTab = BottomNavItem.ISSUES
                            listViewModel.loadIssues()
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
                            workerListViewModel.loadWorkers()
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

@Composable
expect fun CameraOverlay(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
)