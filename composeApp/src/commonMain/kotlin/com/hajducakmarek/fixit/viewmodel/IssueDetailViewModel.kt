package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IssueDetailViewModel(
    private val repository: IssueRepository,
    private val issueId: String
) : ViewModel() {

    private val _issue = MutableStateFlow<Issue?>(null)
    val issue: StateFlow<Issue?> = _issue.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadIssue()
    }

    private fun loadIssue() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _issue.value = repository.getIssueById(issueId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(newStatus: IssueStatus, onSuccess: () -> Unit) {
        val currentIssue = _issue.value ?: return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                // Add small delay to show spinner
                kotlinx.coroutines.delay(300)
                repository.updateIssueStatus(currentIssue.id, newStatus)
                _issue.value = currentIssue.copy(status = newStatus)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                // Handle error
            }
        }
    }

    fun refreshIssue() {
        loadIssue()
    }
}
