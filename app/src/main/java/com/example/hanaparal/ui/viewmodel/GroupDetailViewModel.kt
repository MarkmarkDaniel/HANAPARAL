package com.example.hanaparal.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.GroupMember
import com.example.hanaparal.data.model.StudentProfile
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: StudyGroup? = null,
    val pendingMembers: List<StudentProfile> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

class GroupDetailViewModel(
    application: Application,
    private val groupId: String,
    private val repository: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
    }

    private fun loadGroup() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.getStudyGroup(groupId).onSuccess { group ->
                val currentUserId = auth.currentUser?.uid
                val isAdmin = group?.adminId == currentUserId
                val isMember = group?.memberIds?.contains(currentUserId) == true

                if (isMember || isAdmin) {
                    FirebaseMessaging.getInstance().subscribeToTopic("group_$groupId")
                }

                _uiState.value = _uiState.value.copy(
                    group = group,
                    isAdmin = isAdmin,
                    isLoading = false
                )

                if (isAdmin) {
                    loadPendingMembers()
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, message = it.message)
            }
        }
    }

    private fun loadPendingMembers() {
        viewModelScope.launch {
            val pending = repository.getPendingMembers(groupId)
            _uiState.value = _uiState.value.copy(pendingMembers = pending)
        }
    }

    fun approveMember(student: StudentProfile) {
        viewModelScope.launch {
            val member = GroupMember(
                userId = student.id,
                userName = student.name,
                userEmail = student.email
            )
            repository.approveMember(groupId, member).onSuccess {
                // LOCAL NOTIFICATION PARA SA ADMIN (App side)
                showLocalNotification(
                    "New Member Joined!",
                    "${student.name} is now part of ${uiState.value.group?.name}"
                )

                _uiState.value = _uiState.value.copy(message = "${student.name} approved!")
                loadGroup()
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = "Failed to approve member")
            }
        }
    }

    private fun showLocalNotification(title: String, message: String) {
        val context = getApplication<Application>().applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "admin_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Admin Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun updateAnnouncement(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.updateAnnouncement(groupId, text).onSuccess {
                _uiState.value = _uiState.value.copy(message = "Announcement posted!")
                loadGroup()
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = "Failed to post announcement")
            }
        }
    }

    fun updateReminder(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.updateReminder(groupId, text).onSuccess {
                _uiState.value = _uiState.value.copy(message = "Reminder set!")
                loadGroup()
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = "Failed to set reminder")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

class GroupDetailViewModelFactory(private val application: Application, private val groupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailViewModel(application, groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
