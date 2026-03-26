package com.example.hanaparal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SuperuserUiState(
    val groupCreationEnabled: Boolean = true,
    val announcementHeader: String = "Welcome!",
    val maxMembersPerGroup: Long = 20L,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

class SuperuserViewModel(
    private val remoteConfigRepository: RemoteConfigRepository = RemoteConfigRepository.get()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuperuserUiState())
    val uiState: StateFlow<SuperuserUiState> = _uiState.asStateFlow()

    fun setAuthenticated(authenticated: Boolean) {
        _uiState.value = _uiState.value.copy(isAuthenticated = authenticated)
        if (authenticated) loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            remoteConfigRepository.fetchAndActivate()
            val config = remoteConfigRepository.getConfig()
            _uiState.value = _uiState.value.copy(
                groupCreationEnabled = config.groupCreationEnabled,
                announcementHeader = config.announcementHeader,
                maxMembersPerGroup = config.maxMembersPerGroup,
                isLoading = false
            )
        }
    }
}