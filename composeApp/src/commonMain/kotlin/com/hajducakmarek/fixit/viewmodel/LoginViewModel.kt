package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.repository.IssueRepository
import com.hajducakmarek.fixit.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: IssueRepository,
    private val userSession: UserSession
): ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.seedUsers()
                _users.value = repository.getAllUsers()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(user: User, onSuccess: () -> Unit) {
        userSession.saveUser(user)
        onSuccess()
    }
}