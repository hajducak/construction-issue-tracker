package com.hajducakmarek.fixit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hajducakmarek.fixit.models.User
import com.hajducakmarek.fixit.repository.IssueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerListViewModel(
    private val repository: IssueRepository
) : ViewModel() {

    private val _workers = MutableStateFlow<List<User>>(emptyList())
    val workers: StateFlow<List<User>> = _workers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWorkers()
    }

    fun loadWorkers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Seed users if needed
                repository.seedUsers()
                // Load all workers
                _workers.value = repository.getWorkers()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
