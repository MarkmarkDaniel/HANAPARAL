package com.example.hanaparal.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudentProfile
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.viewmodel.GroupDetailViewModel
import com.example.hanaparal.ui.viewmodel.GroupDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: GroupDetailViewModel = viewModel(
        factory = GroupDetailViewModelFactory(application, groupId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Board", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.group?.let { group ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF8F9FA))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    GroupHeaderCard(group)

                    if (uiState.isAdmin && uiState.pendingMembers.isNotEmpty()) {
                        PendingRequestsSection(
                            members = uiState.pendingMembers,
                            onApprove = { viewModel.approveMember(it) }
                        )
                    }

                    if (uiState.isAdmin) {
                        AdminControlsSection(
                            onAnnouncement = { viewModel.updateAnnouncement(it) },
                            onReminder = { viewModel.updateReminder(it) }
                        )
                    }

                    BoardSection(
                        title = "ANNOUNCEMENTS",
                        icon = Icons.Default.Campaign,
                        content = group.announcement ?: "No announcements yet.",
                        color = Color(0xFF1A457B)
                    )

                    BoardSection(
                        title = "STUDY REMINDERS",
                        icon = Icons.Default.NotificationsActive,
                        content = group.reminder ?: "No study reminders yet.",
                        color = Color(0xFFF1B000)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingRequestsSection(members: List<StudentProfile>, onApprove: (StudentProfile) -> Unit) {
    Column {
        Text(
            text = "PENDING REQUESTS (${members.size})",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDC3545)
        )
        Spacer(modifier = Modifier.height(12.dp))
        members.forEach { student ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE9ECEF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = student.name, fontWeight = FontWeight.Bold)
                        Text(text = student.course, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Button(
                        onClick = { onApprove(student) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeaderCard(group: StudyGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = group.subject.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF1B000),
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A457B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFE9ECEF), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = "${group.memberCount} Members",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminControlsSection(onAnnouncement: (String) -> Unit, onReminder: (String) -> Unit) {
    var showAnnDialog by remember { mutableStateOf(false) }
    var showRemDialog by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ADMIN TOOLS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminActionButton(text = "Post Announce", color = Color(0xFF1A457B), modifier = Modifier.weight(1f), onClick = { showAnnDialog = true })
            AdminActionButton(text = "Set Reminder", color = Color(0xFFF1B000), modifier = Modifier.weight(1f), onClick = { showRemDialog = true })
        }
    }

    if (showAnnDialog || showRemDialog) {
        AlertDialog(
            onDismissRequest = { showAnnDialog = false; showRemDialog = false; text = "" },
            title = { Text(if (showAnnDialog) "New Announcement" else "New Reminder") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) },
            confirmButton = { Button(onClick = { if (showAnnDialog) onAnnouncement(text) else onReminder(text); showAnnDialog = false; showRemDialog = false; text = "" }) { Text("Send") } }
        )
    }
}

@Composable
fun AdminActionButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(16.dp)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun BoardSection(title: String, icon: ImageVector, content: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = color)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = content, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = Color(0xFF495057))
        }
    }
}
