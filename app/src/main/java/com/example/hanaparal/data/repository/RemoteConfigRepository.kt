package com.example.hanaparal.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.example.hanaparal.data.model.RemoteConfigValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RemoteConfigRepository private constructor() {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    private val _configFlow = MutableStateFlow(
        RemoteConfigValues(
            groupCreationEnabled = true,
            announcementHeader = "Welcome!",
            maxMembersPerGroup = 20L
        )
    )

    val configFlow: StateFlow<RemoteConfigValues> = _configFlow

    companion object {
        @Volatile
        private var instance: RemoteConfigRepository? = null

        fun get(): RemoteConfigRepository = instance ?: synchronized(this) {
            instance ?: RemoteConfigRepository().also { instance = it }
        }
    }
}
