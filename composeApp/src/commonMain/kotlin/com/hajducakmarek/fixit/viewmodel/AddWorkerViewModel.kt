package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.utils.Validation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.benasher44.uuid.uuid4

class AddWorkerViewModel(
    private val repository: IssueRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedRole = MutableStateFlow(UserRole.WORKER)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    fun onNameChanged(text: String) {
        _name.value = text
        if (_nameError.value != null) {
            _nameError.value = null
        }
    }

    fun onRoleChanged(role: UserRole) {
        _selectedRole.value = role
    }

    private fun validateForm(): Boolean {
        val nameErr = Validation.getWorkerNameError(_name.value)
        _nameError.value = nameErr
        return nameErr == null
    }

    fun saveWorker(onSuccess: () -> Unit) {
        _saveError.value = null

        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val newWorker = User(
                    id = "user-${uuid4()}",
                    name = _name.value.trim(),
                    role = _selectedRole.value
                )
                repository.insertUser(newWorker)
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _saveError.value = "Failed to add worker: ${e.message ?: "Unknown error"}"
            }
        }
    }
}