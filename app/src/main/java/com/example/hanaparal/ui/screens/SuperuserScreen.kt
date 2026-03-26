package com.example.hanaparal.ui.screens

import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.ui.viewmodel.SuperuserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperuserScreen(
    onBack: () -> Unit,
    viewModel: SuperuserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Helper to find FragmentActivity
    val activity = remember(context) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) break
            currentContext = (currentContext as ContextWrapper).baseContext
        }
        currentContext as? FragmentActivity
    }

    // Function to trigger biometric prompt
    val promptBiometric = {
        activity?.let { act ->
            val executor = ContextCompat.getMainExecutor(act)
            val biometricPrompt = BiometricPrompt(
                act,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        viewModel.setAuthenticated(true)
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        viewModel.showMessage("Security error: $errString")
                    }
                    override fun onAuthenticationFailed() {
                        viewModel.showMessage("Authentication failed. Try again.")
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Superuser Authentication")
                .setSubtitle("Authenticate using Fingerprint, Face, or PIN.")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Automatically show biometric prompt
    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            promptBiometric()
        } else {
            viewModel.showMessage("Biometric or Screen Lock not set up.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Superuser Dashboard") },
                navigationIcon = {
                    Button(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!uiState.isAuthenticated) {
                Text("Admin Access Locked 🔒", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please authenticate to view global app configurations.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { promptBiometric() }) {
                    Text("Unlock with Biometrics")
                }
            } else {
                Text(
                    text = "Current Remote Config (Global Settings)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ConfigItem("Group Creation", if (uiState.groupCreationEnabled) "ENABLED" else "DISABLED")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ConfigItem("Global Announcement", uiState.announcementHeader)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ConfigItem("Max Group Members", uiState.maxMembersPerGroup.toString())
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadConfig() }) {
                    Text("Sync with Cloud")
                }
            }
        }
    }
}

@Composable
fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = MaterialTheme.colorScheme.primary)
    }
}