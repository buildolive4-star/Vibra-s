package com.example.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CallSession
import com.example.data.model.Complaint
import com.example.data.model.ConsentedRecording
import com.example.data.model.FrameTier
import com.example.data.model.HostFrame
import com.example.data.model.HostProfile
import com.example.data.model.HostSalary
import com.example.data.model.HostStatus
import com.example.data.model.PaymentRecord
import com.example.data.model.UserProfile
import com.example.data.model.Wallet
import com.example.ui.components.AnimatedStatLineChart
import com.example.ui.components.Glass3DCard
import com.example.ui.components.Host3DFrameAvatar
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
import com.example.ui.viewmodel.VibraAdminViewModel

@Composable
fun AdminOverviewSection(
    users: List<UserProfile>,
    hosts: List<HostProfile>,
    calls: List<CallSession>,
    payments: List<PaymentRecord>,
    salaries: List<HostSalary>,
    complaints: List<Complaint>,
    onNavigate: (AdminSection) -> Unit
) {
    val totalRevenueInr = payments.sumOf { it.amountInr }
    val activeHosts = hosts.count { it.hostStatusEnum == HostStatus.APPROVED }
    val openComplaints = complaints.count { it.status == "OPEN" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminKpiTile("Total Users", "${users.size}", ElectricViolet, Modifier.weight(1f)) { onNavigate(AdminSection.CALLERS) }
                AdminKpiTile("Approved Hosts", "$activeHosts / ${hosts.size}", EmeraldOnline, Modifier.weight(1f)) { onNavigate(AdminSection.HOSTS) }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminKpiTile("Revenue", "₹$totalRevenueInr", GoldenGlow, Modifier.weight(1f)) { onNavigate(AdminSection.FINANCIAL_REPORTS) }
                AdminKpiTile("Calls / Alerts", "${calls.size} / $openComplaints", NeonPink, Modifier.weight(1f)) { onNavigate(AdminSection.CALL_MONITORING) }
            }
        }

        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform Revenue & Call Velocity", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    Spacer(Modifier.height(8.dp))
                    val chartVals = payments.takeLast(6).map { it.amountInr.toFloat() }
                        .ifEmpty { listOf(120f, 240f, 500f, 380f, 750f, 1000f) }
                    AnimatedStatLineChart(
                        values = chartVals,
                        labels = listOf("T-5", "T-4", "T-3", "T-2", "T-1", "Live"),
                        primaryColor = NeonPink,
                        secondaryColor = GoldenGlow
                    )
                }
            }
        }

        item {
            Glass3DCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pending Salaries & Compliance", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    Text("Pending Host Salaries: ${salaries.count { it.status != "PAID" }} • Open Complaints: $openComplaints", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onNavigate(AdminSection.SALARY_MGMT) }) { Text("Process Salaries", color = GoldenGlow) }
                        OutlinedButton(onClick = { onNavigate(AdminSection.CALL_PRICING) }) { Text("Call Pricing", color = SoftWhite) }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiTile(title: String, value: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Glass3DCard(cornerRadius = 18.dp, modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MutedLavender)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminHostsSection(hosts: List<HostProfile>, adminVm: VibraAdminViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingHost by remember { mutableStateOf<HostProfile?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NeonGradientButton(
                text = "+ Create Approved Host",
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("admin_create_host_btn")
            )
        }
        items(hosts, key = { it.id }) { host ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Host3DFrameAvatar(host.name, host.avatarKey, host.frameTierEnum, host.onlineStatusEnum, size = 58.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(host.name, style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                        Text("${host.hostCode} • ${host.frameTierEnum.label} • ${host.status}", style = MaterialTheme.typography.labelSmall, color = GoldenGlow)
                        Text("Audio ₹${host.audioRatePerMin}/m • Video ₹${host.videoRatePerMin}/m", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                    OutlinedButton(onClick = { editingHost = host }) {
                        Text("Edit", color = SoftWhite)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf("") }
        var tier by remember { mutableStateOf(FrameTier.GOLD) }
        var audioRate by remember { mutableStateOf("20") }
        var videoRate by remember { mutableStateOf("60") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Host") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Host Name") }, singleLine = true)
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = audioRate, onValueChange = { audioRate = it.filter(Char::isDigit) }, label = { Text("Audio ₹/m") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = videoRate, onValueChange = { videoRate = it.filter(Char::isDigit) }, label = { Text("Video ₹/m") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FrameTier.entries) { f ->
                            FilterChip(selected = tier == f, onClick = { tier = f }, label = { Text(f.label) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.createHost(name, bio, tier, audioRate.toLongOrNull() ?: 20L, videoRate.toLongOrNull() ?: 60L)
                    showCreateDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }

    val target = editingHost
    if (target != null) {
        var selStatus by remember(target) { mutableStateOf(target.hostStatusEnum) }
        var selFrame by remember(target) { mutableStateOf(target.frameTierEnum) }
        AlertDialog(
            onDismissRequest = { editingHost = null },
            title = { Text("Manage ${target.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Host Approval Status")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(HostStatus.entries) { st ->
                            FilterChip(selected = selStatus == st, onClick = { selStatus = st }, label = { Text(st.name) })
                        }
                    }
                    Text("Assign 3D Frame (Admin-Only)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FrameTier.entries) { ft ->
                            FilterChip(selected = selFrame == ft, onClick = { selFrame = ft }, label = { Text(ft.label) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.updateHostStatusAndFrame(target, selStatus, selFrame)
                    editingHost = null
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { editingHost = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminCallersSection(users: List<UserProfile>, wallets: List<Wallet>, adminVm: VibraAdminViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users, key = { it.userId }) { user ->
            val bal = wallets.firstOrNull { it.userId == user.userId }?.balanceCoins ?: 0L
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(user.displayName, style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                            Text("ID: ${user.userId.take(12)} • Bal: $bal c • ${user.role}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                        }
                        Surface(color = DarkNavy, shape = RoundedCornerShape(8.dp)) {
                            Text(user.accountStatus, style = MaterialTheme.typography.labelSmall, color = if (user.accountStatus == "ACTIVE") EmeraldOnline else CrimsonAlert, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { adminVm.updateUserStatus(user, "ACTIVE") }) { Text("Activate", color = EmeraldOnline) }
                        OutlinedButton(onClick = { adminVm.updateUserStatus(user, "SUSPENDED") }) { Text("Suspend", color = GoldenGlow) }
                        OutlinedButton(onClick = { adminVm.updateUserStatus(user, "BLOCKED") }) { Text("Block", color = CrimsonAlert) }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHostFramesSection(frames: List<HostFrame>, adminVm: VibraAdminViewModel) {
    var showCreate by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NeonGradientButton(
                text = "+ Create Custom 3D Frame",
                onClick = { showCreate = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(frames, key = { it.id }) { frame ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Host3DFrameAvatar(frame.name, "prism", frame.tierEnum, null, size = 54.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(frame.name, style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                            Text("${frame.tier} • ${frame.description ?: ""}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                        }
                    }
                    Switch(checked = frame.active, onCheckedChange = { adminVm.toggleFrameActive(frame, it) })
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var tier by remember { mutableStateOf(FrameTier.PLATINUM) }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create Custom 3D Frame") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Frame Name") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FrameTier.entries) { f -> FilterChip(selected = tier == f, onClick = { tier = f }, label = { Text(f.label) }) }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.createCustomFrame(name.ifBlank { "Celestial Aura" }, tier, desc, "#8B5CFF", "#FFD36E")
                    showCreate = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminCallsSection(heading: String, calls: List<CallSession>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("$heading (${calls.size})", style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
        }
        if (calls.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No sessions logged in this view yet.", color = MutedLavender, modifier = Modifier.padding(18.dp))
                }
            }
        } else {
            items(calls, key = { it.id }) { call ->
                Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${call.callerName} → ${call.hostName}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                            Text("${call.callType} • ${call.status}", style = MaterialTheme.typography.labelSmall, color = GoldenGlow)
                        }
                        Text("Duration: ${call.durationSeconds}s • Billed: ${call.billedCoins}c • Quality: ${call.networkQuality}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                        Text("Consent Recording Active: ${call.recordingActive} (Caller=${call.callerConsentRecording}, Host=${call.hostConsentRecording})", style = MaterialTheme.typography.labelSmall, color = if (call.recordingActive) EmeraldOnline else MutedLavender)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminConsentedRecordingsSection(recordings: List<ConsentedRecording>, adminVm: VibraAdminViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dual-consent enforcement: Only sessions where both Caller and Host explicitly consented are stored. All admin views and deletions are logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedLavender,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        items(recordings, key = { it.id }) { rec ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Call ${rec.callId.take(12)} • ${rec.status}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("Dual Consent: ${rec.callerConsented && rec.hostConsented} • Retention: ${rec.retentionDays}d", style = MaterialTheme.typography.bodySmall, color = EmeraldOnline)
                    }
                    if (rec.status != "DELETED") {
                        OutlinedButton(onClick = { adminVm.deleteRecording(rec) }) {
                            Text("Delete", color = CrimsonAlert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTimeAdjustmentSection(calls: List<CallSession>, adminVm: VibraAdminViewModel) {
    var selectedCall by remember { mutableStateOf<CallSession?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(calls, key = { it.id }) { call ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${call.callerName} ↔ ${call.hostName}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("${call.durationSeconds}s • ${call.billedCoins} coins billed", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                    OutlinedButton(onClick = { selectedCall = call }) {
                        Text("Adjust", color = GoldenGlow)
                    }
                }
            }
        }
    }

    val editing = selectedCall
    if (editing != null) {
        var secs by remember(editing) { mutableStateOf(editing.durationSeconds.toString()) }
        var coins by remember(editing) { mutableStateOf(editing.billedCoins.toString()) }
        AlertDialog(
            onDismissRequest = { selectedCall = null },
            title = { Text("Adjust Call Time & Billing") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = secs, onValueChange = { secs = it.filter(Char::isDigit) }, label = { Text("Duration (seconds)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = coins, onValueChange = { coins = it.filter(Char::isDigit) }, label = { Text("Billed Coins") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.adjustCallTime(editing, secs.toLongOrNull() ?: editing.durationSeconds, coins.toLongOrNull() ?: editing.billedCoins)
                    selectedCall = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { selectedCall = null }) { Text("Cancel") } }
        )
    }
}
