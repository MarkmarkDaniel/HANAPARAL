package com.example.hanaparal.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GroupMember(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val isAdmin: Boolean = false,

)
