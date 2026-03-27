package com.example.hanaparal.data.model

data class RemoteConfigValues(
    val groupCreationEnabled: Boolean = true,
    val announcementHeader: String = "Welcome!",
    val maxMembersPerGroup: Long = 20L
)