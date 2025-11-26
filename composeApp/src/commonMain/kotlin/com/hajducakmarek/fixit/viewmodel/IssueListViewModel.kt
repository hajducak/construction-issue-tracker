package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IssueListViewModel(
    private val repository: IssueRepository
) : ViewModel() {
    // UI State - what the screen displays
    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // MutableStateFlow = private, changeable state (like Swift's @Published)
    // StateFlow = public, read-only state (like Swift's var vs let)

    init {
        loadIssues()
    }

    fun loadIssues() {
        // viewModelScope.launch = start async work (like Swift's Task)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val issueList = repository.getAllIssues()
                _issues.value = issueList
            } catch (e: Exception) {
                // Handle error (we'll add proper error handling later)
                _issues.value = emptyList()
            } finally { // finally = always runs, even if error
                _isLoading.value = false
            }
        }
    }
}
