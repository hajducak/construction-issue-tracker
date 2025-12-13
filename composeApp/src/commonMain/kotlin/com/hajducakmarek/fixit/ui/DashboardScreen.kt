package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    currentUser: User,
    onLogout: () -> Unit
) {
    val statistics by viewModel.statistics.collectAsState()
    val workerStats by viewModel.workerStats.collectAsState()
    val workerPersonalStats by viewModel.workerPersonalStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    Text(
                        text = "${currentUser.name} (${currentUser.role.name})",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onLogout) {
                        Text("🚪")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadStatistics() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                currentUser.role == UserRole.MANAGER && statistics == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available")
                    }
                }

                currentUser.role == UserRole.WORKER && workerPersonalStats == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available")
                    }
                }

                else -> {
                    if (currentUser.role == UserRole.MANAGER) {
                        // Manager dashboard
                        DashboardContent(
                            statistics = statistics!!,
                            workerStats = workerStats,
                            currentUser = currentUser,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        )
                    } else {
                        // Worker dashboard
                        WorkerDashboardContent(
                            personalStats = workerPersonalStats!!,
                            currentUser = currentUser,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    statistics: com.hajducakmarek.fixit.models.DashboardStatistics,
    workerStats: List<com.hajducakmarek.fixit.models.WorkerStatistics>,
    currentUser: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Text(
            text = "Welcome, ${currentUser.name}!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Overview stats
        OverviewSection(statistics = statistics)

        // Status breakdown
        StatusBreakdownSection(statistics = statistics)

        // Worker performance (managers only)
        if (currentUser.role == UserRole.MANAGER) {
            WorkerPerformanceSection(workerStats = workerStats)
        }
    }
}

@Composable
private fun OverviewSection(
    statistics: com.hajducakmarek.fixit.models.DashboardStatistics
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Issues",
                    value = statistics.totalIssues.toString(),
                    icon = "📋",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completion",
                    value = "${statistics.completionRate.toInt()}%",
                    icon = "✅",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Workers",
                    value = statistics.totalWorkers.toString(),
                    icon = "👷",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Overdue",
                    value = statistics.overdueIssues.toString(),
                    icon = "⚠️",
                    modifier = Modifier.weight(1f),
                    containerColor = if (statistics.overdueIssues > 0) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StatusBreakdownSection(
    statistics: com.hajducakmarek.fixit.models.DashboardStatistics
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatusBar(
                label = "Open",
                count = statistics.openIssues,
                total = statistics.totalIssues,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusBar(
                label = "In Progress",
                count = statistics.inProgressIssues,
                total = statistics.totalIssues,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusBar(
                label = "Fixed",
                count = statistics.fixedIssues,
                total = statistics.totalIssues,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusBar(
                label = "Verified",
                count = statistics.verifiedIssues,
                total = statistics.totalIssues,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = if (total > 0) count.toFloat() / total.toFloat() else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color
        )
    }
}

@Composable
private fun WorkerPerformanceSection(
    workerStats: List<com.hajducakmarek.fixit.models.WorkerStatistics>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Worker Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (workerStats.isEmpty()) {
                Text(
                    text = "No workers yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                workerStats.forEach { stat ->
                    WorkerStatItem(stat)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkerStatItem(
    stat: com.hajducakmarek.fixit.models.WorkerStatistics
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("👷")
                Text(
                    text = stat.worker.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${stat.completedIssues} / ${stat.assignedIssues}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = if (stat.assignedIssues > 0) {
                stat.completedIssues.toFloat() / stat.assignedIssues.toFloat()
            } else {
                0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun WorkerDashboardContent(
    personalStats: com.hajducakmarek.fixit.models.WorkerPersonalStatistics,
    currentUser: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome message
        Text(
            text = "Welcome, ${currentUser.name}!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // My Work Overview
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Work",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Assigned to Me",
                        value = personalStats.assignedToMe.toString(),
                        icon = "📋",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = personalStats.completedByMe.toString(),
                        icon = "✅",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (personalStats.overdueIssues > 0) {
                    StatCard(
                        title = "Overdue Issues",
                        value = personalStats.overdueIssues.toString(),
                        icon = "⚠️",
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Completion rate card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${personalStats.completionRate.toInt()}%",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completion Rate",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // My Issues Breakdown
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Issues Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (personalStats.assignedToMe == 0) {
                    Text(
                        text = "No issues assigned to you yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    StatusBar(
                        label = "Open",
                        count = personalStats.openIssues,
                        total = personalStats.assignedToMe,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusBar(
                        label = "In Progress",
                        count = personalStats.inProgressIssues,
                        total = personalStats.assignedToMe,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusBar(
                        label = "Fixed (Awaiting Verification)",
                        count = personalStats.fixedIssues,
                        total = personalStats.assignedToMe,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}