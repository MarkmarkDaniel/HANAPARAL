package com.example.hanaparal.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigRepository private constructor() {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        @Volatile
        private var instance: RemoteConfigRepository? = null

        fun get(): RemoteConfigRepository = instance ?: synchronized(this) {
            instance ?: RemoteConfigRepository().also { instance = it }
        }
    }
}
