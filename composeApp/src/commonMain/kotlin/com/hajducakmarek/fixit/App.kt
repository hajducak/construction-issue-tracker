package com.hajducakmarek.fixit

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.hajducakmarek.fixit.database.DatabaseDriverFactory
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.ui.IssueListScreen
import com.hajducakmarek.fixit.viewmodel.IssueListViewModel
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import kotlinx.datetime.Clock
import kotlinx.coroutines.launch

@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    MaterialTheme {
        val repository = remember { IssueRepository(databaseDriverFactory) }
        val viewModel = remember { IssueListViewModel(repository) }

        // Seed test data once
        LaunchedEffect(Unit) {
            try {
                seedTestData(repository)
                viewModel.loadIssues()  // Refresh after seeding
            } catch (e: Exception) {
                println("Error seeding data: ${e.message}")
            }
        }

        IssueListScreen(viewModel)
    }
}

private suspend fun seedTestData(repository: IssueRepository) {
    println("Seeding test data...")

    // Insert test issues
    val issues = listOf(
        Issue(
            id = "1",
            photoPath = "/photos/window.jpg",
            description = "Broken window in living room",
            flatNumber = "A-101",
            status = IssueStatus.OPEN,
            createdBy = "manager-1",
            assignedTo = null,
            createdAt = Clock.System.now().toEpochMilliseconds()
        ),
        Issue(
            id = "2",
            photoPath = "/photos/door.jpg",
            description = "Door handle loose",
            flatNumber = "A-102",
            status = IssueStatus.IN_PROGRESS,
            createdBy = "manager-1",
            assignedTo = "worker-1",
            createdAt = Clock.System.now().toEpochMilliseconds()
        ),
        Issue(
            id = "3",
            photoPath = "/photos/paint.jpg",
            description = "Paint chipping in bedroom",
            flatNumber = "B-201",
            status = IssueStatus.FIXED,
            createdBy = "manager-1",
            assignedTo = "worker-2",
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
    )

    issues.forEach { issue ->
        println("Inserting issue: ${issue.id}")
        repository.insertIssue(issue)
    }

    println("Test data seeded!")
}