package com.example.hanaparal.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HanapAralMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "HanapAral Update", it.body ?: "")
        }
    }
}