package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.delay
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

    private val _workers = MutableStateFlow<List<User>>(emptyList())
    val workers: StateFlow<List<User>> = _workers.asStateFlow()

    private val _assignedWorker = MutableStateFlow<User?>(null)
    val assignedWorker: StateFlow<User?> = _assignedWorker.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadIssue()
        loadWorkers()
    }

    private fun loadIssue() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val loadedIssue = repository.getIssueById(issueId)
                _issue.value = loadedIssue

                // Load assigned worker if exists
                loadedIssue?.assignedTo?.let { workerId ->
                    _assignedWorker.value = repository.getUserById(workerId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load issue: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            try {
                _workers.value = repository.getWorkers()
            } catch (e: Exception) {
                // Silent failure for workers list - non-critical
            }
        }
    }

    fun updateStatus(newStatus: IssueStatus, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                repository.updateIssueStatus(issueId, newStatus)
                _issue.value = _issue.value?.copy(status = newStatus)
                delay(300)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _error.value = "Failed to update status: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun assignWorker(worker: User?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                repository.updateIssueAssignment(issueId, worker?.id)
                _assignedWorker.value = worker
                _issue.value = _issue.value?.copy(assignedTo = worker?.id)
                delay(300)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _error.value = "Failed to assign worker: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}