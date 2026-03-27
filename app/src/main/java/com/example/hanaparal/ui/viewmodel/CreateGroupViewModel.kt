package com.example.hanaparal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.AuthRepository
import com.example.hanaparal.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val createdGroupId: String? = null
)

class CreateGroupViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun createGroup(name: String, description: String, subject: String) {
        if (name.isBlank() || subject.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val user = authRepository.currentUser ?: return@launch
            val profileResult = firestoreRepository.getStudentProfile(user.uid)
            val adminName = profileResult.getOrNull()?.name ?: user.displayName ?: "Admin"

            val group = StudyGroup(
                name = name,
                description = description,
                subject = subject,
                adminId = user.uid,
                adminName = adminName,
                adminEmail = user.email ?: ""
            )

            firestoreRepository.createStudyGroup(group)
                .onSuccess { groupId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isCreated = true,
                        createdGroupId = groupId
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
