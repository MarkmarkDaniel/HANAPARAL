package com.example.hanaparal.data.repository

import android.util.Log
import kotlinx.coroutines.tasks.await
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.example.hanaparal.data.model.RemoteConfigValues
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
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


    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                "group_creation_enabled" to true,
                "announcement_header" to "Welcome!",
                "max_members_per_group" to 20L
            )
        )

        updateConfig()
    }

    fun updateConfig() {
        val newValues = RemoteConfigValues(
            groupCreationEnabled = remoteConfig.getBoolean("group_creation_enabled"),
            announcementHeader = remoteConfig.getString("announcement_header")
                .takeIf { it.isNotEmpty() } ?: "Welcome!",
            maxMembersPerGroup = remoteConfig.getLong("max_members_per_group")
                .takeIf { it > 0 } ?: 20L
        )

        _configFlow.value = newValues
    }

    suspend fun fetchAndActivate(): Boolean {
        val success = remoteConfig.fetchAndActivate().await()
        if (success) {
            updateConfig()
            Log.d("RemoteConfig", "Config updated successfully")
        }
        return success
    }
}
