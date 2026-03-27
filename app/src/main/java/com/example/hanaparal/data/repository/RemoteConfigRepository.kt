package com.example.hanaparal.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigRepository private constructor() {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
}