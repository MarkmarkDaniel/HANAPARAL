package com.example.hanaparal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.StudentProfile
import com.example.hanaparal.data.repository.AuthRepository
import com.example.hanaparal.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: StudentProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            firestoreRepository.getStudentProfile(user.uid)
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile ?: StudentProfile(
                            id = user.uid,
                            email = user.email ?: ""
                        ),
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        profile = StudentProfile(id = user.uid, email = user.email ?: ""),
                        isLoading = false
                    )
                }
        }
    }

    fun saveProfile(name: String, course: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser ?: return@launch
            val profile = StudentProfile(
                id = user.uid,
                name = name,
                course = course,
                email = user.email ?: ""
            )
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            firestoreRepository.createOrUpdateStudentProfile(profile)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        isSaved = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
