package com.hajducakmarek.fixit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueListScreen(
    viewModel: IssueListViewModel,
    onAddClick: () -> Unit = {},
    onIssueClick: (Issue) -> Unit = {}
) {
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issues") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        when {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(issues) { issue ->
                        IssueCard(
                            issue = issue,
                            onClick = { onIssueClick(issue) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(
    issue: Issue,
    onClick: () -> Unit = {}
) {
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    val hasPhoto = issue.photoPath.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (hasPhoto) {
            // Layout WITH photo - Row with thumbnail on left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IssueImage(
                    photoPath = issue.photoPath,
                    contentDescription = "Issue photo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFullScreenPhoto = true }
                )
                Spacer(modifier = Modifier.width(16.dp))

                IssueDetails(issue = issue, modifier = Modifier.weight(1f))
            }

            // Only show dialog if photo exists AND user clicked
            if (showFullScreenPhoto) {
                FullScreenPhotoDialog(
                    photoPath = issue.photoPath,
                    onDismiss = { showFullScreenPhoto = false }
                )
            }
        } else {
            // Layout WITHOUT photo - Full width content
            IssueDetails(
                issue = issue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            // No dialog possible here - no photo to click!
        }
    }
}

@Composable
private fun IssueDetails(issue: Issue, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = issue.flatNumber,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = issue.description,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = issue.status.name.replace("_", " "),
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