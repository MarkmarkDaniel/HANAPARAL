package com.example.hanaparal.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class StudentProfile(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val course: String = "",
    val email: String = "",
    val fcmToken: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
