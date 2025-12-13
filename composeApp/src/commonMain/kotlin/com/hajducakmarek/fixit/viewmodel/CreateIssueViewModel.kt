package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.Issue
import com.hajducakmarek.fixit.models.IssueStatus
import com.hajducakmarek.fixit.models.IssuePriority
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.Photo
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.utils.Validation
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

    private val _photoPaths = MutableStateFlow<List<String>>(emptyList())
    val photoPaths: StateFlow<List<String>> = _photoPaths.asStateFlow()

    private val _priority = MutableStateFlow(IssuePriority.MEDIUM)
    val priority: StateFlow<IssuePriority> = _priority.asStateFlow()

    private val _dueDate = MutableStateFlow<Long?>(null)
    val dueDate: StateFlow<Long?> = _dueDate.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _flatNumberError = MutableStateFlow<String?>(null)
    val flatNumberError: StateFlow<String?> = _flatNumberError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

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
        _flatNumber.value = text.uppercase()
        // Clear error when user types
        if (_flatNumberError.value != null) {
            _flatNumberError.value = null
        }
    }

    fun onDescriptionChanged(text: String) {
        _description.value = text
        // Clear error when user types
        if (_descriptionError.value != null) {
            _descriptionError.value = null
        }
    }

    fun onPhotoAdded(path: String) {
        _photoPaths.value = _photoPaths.value + path
    }

    fun onPhotoRemoved(path: String) {
        _photoPaths.value = _photoPaths.value - path
    }

    fun onWorkerSelected(worker: User?) {
        _selectedWorker.value = worker
    }

    fun onPrioritySelected(priority: IssuePriority) {
        _priority.value = priority
    }

    fun onDueDateSelected(timestamp: Long?) {
        _dueDate.value = timestamp
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate flat number
        val flatError = Validation.getFlatNumberError(_flatNumber.value)
        _flatNumberError.value = flatError
        if (flatError != null) isValid = false

        // Validate description
        val descError = Validation.getDescriptionError(_description.value)
        _descriptionError.value = descError
        if (descError != null) isValid = false

        return isValid
    }

    fun saveIssue(createdBy: String, onSuccess: () -> Unit) {
        _saveError.value = null

        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val issueId = "issue-${uuid4()}"
                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

                val newIssue = Issue(
                    id = issueId,
                    description = _description.value.trim(),
                    flatNumber = _flatNumber.value.trim(),
                    status = IssueStatus.OPEN,
                    createdBy = createdBy,
                    assignedTo = _selectedWorker.value?.id,
                    createdAt = now,
                    completedAt = null,
                    priority = _priority.value,
                    dueDate = _dueDate.value
                )
                repository.insertIssue(newIssue)

                _photoPaths.value.forEach { photoPath ->
                    val photo = Photo(
                        id = "photo-${uuid4()}",
                        issueId = issueId,
                        photoPath = photoPath,
                        createdAt = now,
                        uploadedBy = createdBy
                    )
                    repository.insertPhoto(photo)
                }

                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _saveError.value = "Failed to create issue: ${e.message ?: "Unknown error"}"
            }
        }
    }
}