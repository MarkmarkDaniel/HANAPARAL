package com.example.hanaparal.ui.viewmodel

data class SuperuserUiState(
    val groupCreationEnabled: Boolean = true,
    val announcementHeader: String = "Welcome!",
    val maxMembersPerGroup: Long = 20L,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)