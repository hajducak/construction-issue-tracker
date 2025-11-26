package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel

// @Composable = this function describes UI (like SwiftUI's View)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: IssueListViewModel,
    onAddClick: () -> Unit = {}
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // collectAsState() = observe Flow changes (like SwiftUI's @State)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issues") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        when { // when = Kotlin's switch statement
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            issues.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No issues yet. Tap + to create one.")
                }
            }
            else -> {
                // LazyColumn = scrollable list (like SwiftUI's List)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(issues) { issue ->
                        IssueCard(issue)
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(issue: Issue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Show photo if exists
            if (issue.photoPath.isNotEmpty()) {
                // Simple image display - in production you'd use Coil or Glide
                Text(
                    text = "📷 Photo attached",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = issue.flatNumber,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = issue.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when(issue.status) {
                        com.hajducakmarek.fixit.models.IssueStatus.OPEN ->
                            MaterialTheme.colorScheme.error
                        com.hajducakmarek.fixit.models.IssueStatus.IN_PROGRESS ->
                            MaterialTheme.colorScheme.tertiary
                        com.hajducakmarek.fixit.models.IssueStatus.FIXED ->
                            MaterialTheme.colorScheme.primary
                        com.hajducakmarek.fixit.models.IssueStatus.VERIFIED ->
                            MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}