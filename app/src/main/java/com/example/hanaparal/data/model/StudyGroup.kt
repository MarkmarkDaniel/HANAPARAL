package com.example.hanaparal.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class StudyGroup(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val adminEmail: String = "",
    val memberIds: List<String> = emptyList(),
    val pendingMemberIds: List<String> = emptyList(),
    val memberCount: Int = 0,
    val subject: String = "",
    val announcement: String? = null,
    val reminder: String? = null,

)
