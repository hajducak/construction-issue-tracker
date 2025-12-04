package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class IssueListViewModel(
    private val repository: IssueRepository,
    private val currentUser: User
) : ViewModel() {

    // All issues from database
    private val _allIssues = MutableStateFlow<List<Issue>>(emptyList())

    // Filtered issues (displayed in UI)
    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter states
    private val _selectedStatus = MutableStateFlow<IssueStatus?>(null)
    val selectedStatus: StateFlow<IssueStatus?> = _selectedStatus.asStateFlow()

    private val _selectedWorker = MutableStateFlow<User?>(null)
    val selectedWorker: StateFlow<User?> = _selectedWorker.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Workers list for filter
    private val _workers = MutableStateFlow<List<User>>(emptyList())
    val workers: StateFlow<List<User>> = _workers.asStateFlow()

    // Active filter count
    val activeFilterCount: StateFlow<Int> = combine(
        _selectedStatus,
        _selectedWorker,
        _searchQuery
    ) { status, worker, search ->
        var count = 0
        if (status != null) count++
        if (worker != null) count++
        if (search.isNotBlank()) count++
        count
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        loadIssues()
        loadWorkers()
    }

    fun loadIssues() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allIssues.value = repository.getAllIssues()
                applyFilters()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            _workers.value = repository.getWorkers()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onStatusFilterChanged(status: IssueStatus?) {
        _selectedStatus.value = if (_selectedStatus.value == status) null else status
        applyFilters()
    }

    fun onWorkerFilterChanged(worker: User?) {
        _selectedWorker.value = if (_selectedWorker.value?.id == worker?.id) null else worker
        applyFilters()
    }

    fun clearFilters() {
        _selectedStatus.value = null
        _selectedWorker.value = null
        _searchQuery.value = ""
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _allIssues.value

        // ROLE-BASED FILTERING - Workers only see their assigned issues
        if (currentUser.role == UserRole.WORKER) {
            filtered = filtered.filter { it.assignedTo == currentUser.id }
        }
        // Managers see all issues (no filtering needed)

        // Filter by status
        _selectedStatus.value?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // Filter by assigned worker
        _selectedWorker.value?.let { worker ->
            filtered = filtered.filter { it.assignedTo == worker.id }
        }

        // Filter by search query
        if (_searchQuery.value.isNotBlank()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter { issue ->
                issue.description.lowercase().contains(query) ||
                        issue.flatNumber.lowercase().contains(query)
            }
        }

        _issues.value = filtered
    }
}