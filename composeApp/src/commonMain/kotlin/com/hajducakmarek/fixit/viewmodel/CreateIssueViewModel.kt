package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.benasher44.uuid.uuid4

class CreateIssueViewModel(
    private val repository: IssueRepository
) : ViewModel() {

    private val _flatNumber = MutableStateFlow("")
    val flatNumber: StateFlow<String> = _flatNumber.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _photoPath = MutableStateFlow("")
    val photoPath: StateFlow<String> = _photoPath.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _workers = MutableStateFlow<List<User>>(emptyList())
    val workers: StateFlow<List<User>> = _workers.asStateFlow()

    private val _selectedWorker = MutableStateFlow<User?>(null)
    val selectedWorker: StateFlow<User?> = _selectedWorker.asStateFlow()

    init {
        loadWorkers()
    }

    private fun loadWorkers() {
        viewModelScope.launch {
            _workers.value = repository.getWorkers()
        }
    }

    fun onFlatNumberChanged(text: String) {
        _flatNumber.value = text
    }

    fun onDescriptionChanged(text: String) {
        _description.value = text
    }

    fun onPhotoSelected(path: String) {
        _photoPath.value = path
    }

    fun onWorkerSelected(worker: User?) {
        _selectedWorker.value = worker
    }

    fun saveIssue(createdBy: String, onSuccess: () -> Unit) {
        if (_flatNumber.value.isBlank() || _description.value.isBlank()) {
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val newIssue = Issue(
                    id = "issue-${uuid4()}",
                    photoPath = _photoPath.value,
                    description = _description.value,
                    flatNumber = _flatNumber.value,
                    status = IssueStatus.OPEN,
                    createdBy = createdBy,
                    assignedTo = _selectedWorker.value?.id,  // Use selected worker
                    createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    completedAt = null
                )
                repository.insertIssue(newIssue)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
            }
        }
    }
}