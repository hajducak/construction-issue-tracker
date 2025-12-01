package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.models.UserRole
import com.hajducakmarek.fixit.repository.IssueRepository
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

    fun onNameChanged(text: String) {
        _name.value = text
    }

    fun onRoleChanged(role: UserRole) {
        _selectedRole.value = role
    }

    fun saveWorker(onSuccess: () -> Unit) {
        if (_name.value.isBlank()) return

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
            }
        }
    }
}
