package com.example.ui.screens.caller

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppMode
import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.Wallet
import com.example.ui.components.Glass3DCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite
import java.text.SimpleDateFormat
import java.util.Locale

enum class CallHistoryTab(val label: String) {
    AUDIO("Audio Calls"),
    VIDEO("Video Calls"),
    MISSED("Missed"),
    COMPLETED("Completed")
}

@Composable
fun CallHistoryScreen(
    callsState: UiState<List<CallSession>>
) {
    var selectedTab by remember { mutableStateOf(CallHistoryTab.AUDIO) }
    val allCalls = (callsState as? UiState.Success)?.data ?: emptyList()

    val filtered = remember(allCalls, selectedTab) {
        when (selectedTab) {
            CallHistoryTab.AUDIO -> allCalls.filter { it.callTypeEnum == CallType.AUDIO }
            CallHistoryTab.VIDEO -> allCalls.filter { it.callTypeEnum == CallType.VIDEO }
            CallHistoryTab.MISSED -> allCalls.filter { it.callStatusEnum == CallStatus.MISSED || it.callStatusEnum == CallStatus.REJECTED }
            CallHistoryTab.COMPLETED -> allCalls.filter { it.callStatusEnum == CallStatus.COMPLETED }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Call History", style = MaterialTheme.typography.headlineLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CallHistoryTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricViolet, selectedLabelColor = SoftWhite)
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No ${selectedTab.label.lowercase()} recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedLavender,
                        modifier = Modifier.padding(22.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { call ->
                val dateStr = call.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Just now"
                Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (call.callTypeEnum == CallType.VIDEO) NeonPink.copy(0.25f) else ElectricViolet.copy(0.25f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (call.callTypeEnum == CallType.VIDEO) Icons.Rounded.Videocam else Icons.Rounded.Call,
                                    contentDescription = call.callType,
                                    tint = if (call.callTypeEnum == CallType.VIDEO) NeonPink else ElectricViolet
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(call.hostName, style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                                Text("$dateStr • ${call.durationSeconds}s", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("-${call.billedCoins} Coins", style = MaterialTheme.typography.titleSmall, color = GoldenGlow, fontWeight = FontWeight.Bold)
                            Text(call.status, style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    userProfileState: UiState<UserProfile?>,
    wallet: Wallet,
    currentUserId: String,
    isAdmin: Boolean,
    particlesEnabled: Boolean,
    onToggleParticles: (Boolean) -> Unit,
    onUpdateProfile: (name: String, phone: String?, countryCode: String?, language: String) -> Unit,
    onOpenWallet: () -> Unit,
    onSwitchAppMode: (AppMode) -> Unit,
    onSignOut: () -> Unit
) {
    val profile = (userProfileState as? UiState.Success)?.data
    val displayName = profile?.displayName ?: "Vibra Explorer"
    val language = profile?.language ?: "English"

    var showEditDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var editName by remember(displayName) { mutableStateOf(displayName) }
    var editPhone by remember(profile?.phoneNumber) { mutableStateOf(profile?.phoneNumber ?: "") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink)))
                            .border(2.dp, GoldenGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(displayName.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = SoftWhite)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(displayName, style = MaterialTheme.typography.headlineMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    Text("UID: ${currentUserId.take(14)}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = DarkNavy.copy(0.8f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable(onClick = onOpenWallet)
                    ) {
                        Text(
                            text = "Wallet Balance: ${wallet.balanceCoins} Coins",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldenGlow,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Workspace Role Switcher (Caller / Host Studio / Admin Control Center)
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Switch Vibra Workspace", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        NeonGradientButton(
                            text = "Host Studio",
                            colors = listOf(ElectricViolet, RoyalBlue),
                            onClick = { onSwitchAppMode(AppMode.HOST) },
                            modifier = Modifier.weight(1f).testTag("switch_to_host_btn"),
                            leadingIcon = { Icon(Icons.Rounded.Mic, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(16.dp)) }
                        )
                        if (isAdmin) {
                            NeonGradientButton(
                                text = "Admin Center",
                                colors = listOf(NeonPink, GoldenGlow),
                                onClick = { onSwitchAppMode(AppMode.ADMIN) },
                                modifier = Modifier.weight(1f).testTag("switch_to_admin_btn"),
                                leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileActionRow(Icons.Rounded.Edit, "Edit Profile", displayName) { showEditDialog = true }
                    ProfileActionRow(Icons.Rounded.Language, "Language", language) { showLanguageDialog = true }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, tint = ElectricViolet)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("3D Particle Effects", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                                Text("Disable on low-end devices", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                            }
                        }
                        Switch(checked = particlesEnabled, onCheckedChange = onToggleParticles)
                    }
                    ProfileActionRow(Icons.Rounded.Security, "Privacy & Screenshot Shield", "FLAG_SECURE & Consent Policy") { showPrivacyDialog = true }
                    ProfileActionRow(Icons.AutoMirrored.Rounded.HelpOutline, "Help & Support", "24x7 Safety Desk") { showHelpDialog = true }
                    ProfileActionRow(Icons.AutoMirrored.Rounded.ExitToApp, "Logout", "Sign out securely", tint = CrimsonAlert, onClick = onSignOut)
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Display Name") }, singleLine = true)
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Mobile Number") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(editName, editPhone, profile?.countryCode ?: "+91", language)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    listOf("English", "Hindi", "Tamil", "Telugu", "Spanish").forEach { lang ->
                        TextButton(onClick = {
                            onUpdateProfile(displayName, profile?.phoneNumber, profile?.countryCode, lang)
                            showLanguageDialog = false
                        }) { Text(lang) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security") },
            text = { Text(stringResource(R.string.privacy_screenshot_notice)) },
            confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("Got it") } }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Vibra Help & Support") },
            text = { Text("Need help with wallet recharges, call billing, or safety reports? Use the Report button on any host profile or chat thread—reports go directly to the Vibra Admin Control Center.") },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = ElectricViolet,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = tint)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedLavender)
        }
    }
}
