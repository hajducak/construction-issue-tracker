package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.models.IssuePriority
import com.hajducakmarek.fixit.utils.PdfExporter
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

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _isExportingAll = MutableStateFlow(false)
    val isExportingAll: StateFlow<Boolean> = _isExportingAll.asStateFlow()

    private val _exportAllSuccess = MutableStateFlow<String?>(null)
    val exportAllSuccess: StateFlow<String?> = _exportAllSuccess.asStateFlow()

    private val _selectedPriority = MutableStateFlow<IssuePriority?>(null)
    val selectedPriority: StateFlow<IssuePriority?> = _selectedPriority.asStateFlow()

    private val _showOverdueOnly = MutableStateFlow(false)
    val showOverdueOnly: StateFlow<Boolean> = _showOverdueOnly.asStateFlow()

    private val _dueDateFrom = MutableStateFlow<Long?>(null)
    val dueDateFrom: StateFlow<Long?> = _dueDateFrom.asStateFlow()

    private val _dueDateTo = MutableStateFlow<Long?>(null)
    val dueDateTo: StateFlow<Long?> = _dueDateTo.asStateFlow()

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
            _loadError.value = null
            try {
                _allIssues.value = repository.getAllIssues()
                applyFilters()
            } catch (e: Exception) {
                _loadError.value = "Failed to load issues: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _loadError.value = null
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

    fun getActiveFilterCount(): Int {
        var count = 0
        if (_searchQuery.value.isNotEmpty()) count++
        if (_selectedStatus.value != null) count++
        if (_selectedWorker.value != null) count++
        if (_selectedPriority.value != null) count++
        if (_showOverdueOnly.value) count++
        if (_dueDateFrom.value != null) count++
        if (_dueDateTo.value != null) count++
        return count
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatus.value = null
        _selectedWorker.value = null
        _selectedPriority.value = null
        _showOverdueOnly.value = false
        _dueDateFrom.value = null
        _dueDateTo.value = null
        applyFilters()
    }

    fun onPriorityFilterChanged(priority: IssuePriority?) {
        _selectedPriority.value = priority
        applyFilters()
    }

    fun onOverdueFilterChanged(showOverdue: Boolean) {
        _showOverdueOnly.value = showOverdue
        applyFilters()
    }

    fun onDueDateFromChanged(date: Long?) {
        _dueDateFrom.value = date
        applyFilters()
    }

    fun onDueDateToChanged(date: Long?) {
        _dueDateTo.value = date
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val allIssues = repository.getAllIssues()
            val searchTerm = _searchQuery.value.lowercase()
            val status = _selectedStatus.value
            val worker = _selectedWorker.value
            val priority = _selectedPriority.value
            val showOverdue = _showOverdueOnly.value
            val dateFrom = _dueDateFrom.value
            val dateTo = _dueDateTo.value

            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

            val filtered = allIssues.filter { issue ->
                // Text search filter
                val matchesSearch = searchTerm.isEmpty() ||
                        issue.description.lowercase().contains(searchTerm) ||
                        issue.flatNumber.lowercase().contains(searchTerm)

                // Status filter
                val matchesStatus = status == null || issue.status == status

                // Worker filter
                val matchesWorker = worker == null || issue.assignedTo == worker.id

                // Priority filter
                val matchesPriority = priority == null || issue.priority == priority

                // Overdue filter
                val matchesOverdue = if (showOverdue) {
                    issue.dueDate != null &&
                            issue.dueDate < now &&
                            issue.status != IssueStatus.VERIFIED
                } else {
                    true
                }

                // Due date range filter
                val matchesDateRange = if (dateFrom != null || dateTo != null) {
                    issue.dueDate?.let { dueDate ->
                        val afterFrom = dateFrom == null || dueDate >= dateFrom
                        val beforeTo = dateTo == null || dueDate <= dateTo
                        afterFrom && beforeTo
                    } ?: false
                } else {
                    true
                }

                matchesSearch && matchesStatus && matchesWorker &&
                        matchesPriority && matchesOverdue && matchesDateRange
            }

            _issues.value = filtered
        }
    }

    fun filterMyIssues(currentUserId: String) {
        clearFilters()
        _selectedWorker.value = _workers.value.find { it.id == currentUserId }
        applyFilters()
    }

    fun filterOverdueIssues() {
        clearFilters()
        _showOverdueOnly.value = true
        applyFilters()
    }

    fun filterHighPriorityIssues() {
        clearFilters()
        _selectedPriority.value = IssuePriority.URGENT
        applyFilters()
    }

    fun exportAllIssuesToPdf(pdfExporter: PdfExporter, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isExportingAll.value = true
            try {
                val issues = _issues.value

                if (issues.isEmpty()) {
                    throw Exception("No issues to export")
                }

                val filePath = pdfExporter.exportAllIssuesToPdf(issues)

                _exportAllSuccess.value = filePath
                onSuccess(filePath)
            } catch (e: Exception) {
                // Handle error if needed
                println("Export failed: ${e.message}")
            } finally {
                _isExportingAll.value = false
            }
        }
    }

    fun clearExportAllSuccess() {
        _exportAllSuccess.value = null
    }
}