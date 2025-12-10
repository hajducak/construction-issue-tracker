package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.DashboardStatistics
import com.hajducakmarek.fixit.models.WorkerStatistics
import com.hajducakmarek.fixit.models.WorkerPersonalStatistics
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: IssueRepository,
    private val currentUser: User
) : ViewModel() {

    private val _statistics = MutableStateFlow<DashboardStatistics?>(null)
    val statistics: StateFlow<DashboardStatistics?> = _statistics.asStateFlow()

    private val _workerStats = MutableStateFlow<List<WorkerStatistics>>(emptyList())
    val workerStats: StateFlow<List<WorkerStatistics>> = _workerStats.asStateFlow()

    private val _workerPersonalStats = MutableStateFlow<WorkerPersonalStatistics?>(null)
    val workerPersonalStats: StateFlow<WorkerPersonalStatistics?> = _workerPersonalStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (currentUser.role == UserRole.MANAGER) {
                    // Load manager stats
                    _statistics.value = repository.getDashboardStatistics()
                    _workerStats.value = repository.getWorkerStatistics()
                } else {
                    // Load worker personal stats
                    _workerPersonalStats.value = repository.getWorkerPersonalStatistics(currentUser.id)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load statistics: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}