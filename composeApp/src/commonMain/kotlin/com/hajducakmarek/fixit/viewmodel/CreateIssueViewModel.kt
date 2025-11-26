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
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateIssueViewModel(
    private val repository: IssueRepository
) : ViewModel() {

    private val _photoPath = MutableStateFlow<String?>(null)
    val photoPath: StateFlow<String?> = _photoPath.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _flatNumber = MutableStateFlow("")
    val flatNumber: StateFlow<String> = _flatNumber.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun onPhotoSelected(path: String?) {
        _photoPath.value = path
    }

    fun onDescriptionChanged(text: String) {
        _description.value = text
    }

    fun onFlatNumberChanged(text: String) {
        _flatNumber.value = text
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveIssue(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_description.value.isBlank() || _flatNumber.value.isBlank()) {
                return@launch
            }

            _isSaving.value = true

            val issue = Issue(
                id = Uuid.random().toString(),
                photoPath = _photoPath.value ?: "",
                description = _description.value,
                flatNumber = _flatNumber.value,
                status = IssueStatus.OPEN,
                createdBy = "current-user", // TODO: Add auth
                assignedTo = null,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )

            repository.insertIssue(issue)
            _isSaving.value = false
            onSuccess()
        }
    }
}
