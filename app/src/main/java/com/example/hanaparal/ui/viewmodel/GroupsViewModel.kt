package com.example.hanaparal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.model.StudentProfile
import com.example.hanaparal.data.repository.AuthRepository
import com.example.hanaparal.data.repository.FirestoreRepository
import com.example.hanaparal.data.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupsUiState(
    val allGroups: List<StudyGroup> = emptyList(),
    val myGroups: List<StudyGroup> = emptyList(),
    val profile: StudentProfile? = null,
    val groupCreationEnabled: Boolean = true,
    val announcementHeader: String = "Welcome!",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinRequestSent: Boolean = false
)

class GroupsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val remoteConfigRepository: RemoteConfigRepository = RemoteConfigRepository.get()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
        loadProfile()
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            firestoreRepository.getAllStudyGroups().collect { allGroups ->
                _uiState.update { it.copy(allGroups = allGroups) }
            }
        }
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            firestoreRepository.getMyStudyGroups(userId).collect { myGroups ->
                _uiState.update { it.copy(myGroups = myGroups) }
            }
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            remoteConfigRepository.fetchAndActivate()
            val config = remoteConfigRepository.getConfig()
            _uiState.update { it.copy(
                groupCreationEnabled = config.groupCreationEnabled,
                announcementHeader = config.announcementHeader
            ) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            firestoreRepository.getStudentProfile(user.uid).onSuccess { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
    }

    fun requestToJoin(groupId: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }

            firestoreRepository.requestToJoinGroup(groupId, userId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, joinRequestSent = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearJoinRequestSent() {
        _uiState.update { it.copy(joinRequestSent = false) }
    }
}
