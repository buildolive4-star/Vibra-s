package com.example.ui.screens.host

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.FrameTier
import com.example.data.model.HostAttendance
import com.example.data.model.HostEarning
import com.example.data.model.HostProfile
import com.example.data.model.HostSalary
import com.example.data.model.HostStatus
import com.example.data.model.OnlineStatus
import com.example.data.model.SalaryStatus
import com.example.data.model.UiState
import com.example.ui.components.AnimatedStatLineChart
import com.example.ui.components.Glass3DCard
import com.example.ui.components.Host3DFrameAvatar
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.VibraCosmicBackground
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite

enum class HostTab(val label: String) {
    DASHBOARD("Dashboard"),
    EARNINGS("Earnings"),
    ATTENDANCE("Attendance"),
    SALARY("Salary"),
    PROFILE("Profile")
}

@Composable
fun HostStudioScreen(
    currentUserId: String,
    hostsState: UiState<List<HostProfile>>,
    earningsState: UiState<List<HostEarning>>,
    attendanceState: UiState<List<HostAttendance>>,
    salariesState: UiState<List<HostSalary>>,
    callsState: UiState<List<CallSession>>,
    notificationsState: UiState<List<AppNotification>>,
    pricing: AppPricingSettings,
    onApplyAsHost: (name: String, bio: String) -> Unit,
    onUpdateAvailability: (HostProfile, OnlineStatus, String, Boolean, Boolean) -> Unit,
    onToggleShift: (HostProfile, Boolean) -> Unit,
    onAcceptCallPreview: (HostProfile, CallType) -> Unit,
    onExitHostMode: () -> Unit
) {
    BackHandler(onBack = onExitHostMode)

    val allHosts = (hostsState as? UiState.Success)?.data ?: emptyList()
    val myHost = remember(allHosts, currentUserId) {
        allHosts.firstOrNull { it.ownerUid == currentUserId }
    }

    var currentTab by remember { mutableStateOf(HostTab.DASHBOARD) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    VibraCosmicBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onExitHostMode,
                            modifier = Modifier.size(48.dp).testTag("host_exit_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Caller", tint = SoftWhite)
                        }
                        Column {
                            Text("VIBRA HOST", style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                            Text(
                                text = myHost?.let { "${it.hostCode} • ${it.hostStatusEnum.name}" } ?: "Creator Studio",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldenGlow
                            )
                        }
                    }
                    IconButton(
                        onClick = { showNotificationsSheet = !showNotificationsSheet },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Host notifications", tint = SoftWhite)
                    }
                }
            },
            bottomBar = {
                if (myHost != null) {
                    NavigationBar(
                        containerColor = DeepSurface.copy(alpha = 0.94f),
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        HostTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                HostTab.DASHBOARD -> Icons.Rounded.Dashboard
                                HostTab.EARNINGS -> Icons.Rounded.MonetizationOn
                                HostTab.ATTENDANCE -> Icons.Rounded.AccessTime
                                HostTab.SALARY -> Icons.Rounded.AccountBalance
                                HostTab.PROFILE -> Icons.Rounded.Person
                            }
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab; showNotificationsSheet = false },
                                icon = { Icon(icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (myHost == null) {
                    HostApplicationGate(onApply = onApplyAsHost)
                } else if (showNotificationsSheet) {
                    HostNotificationsView(
                        notifications = (notificationsState as? UiState.Success)?.data ?: emptyList(),
                        onClose = { showNotificationsSheet = false }
                    )
                } else {
                    val earnings = (earningsState as? UiState.Success)?.data ?: emptyList()
                    val attendance = (attendanceState as? UiState.Success)?.data ?: emptyList()
                    val salaries = (salariesState as? UiState.Success)?.data ?: emptyList()
                    val calls = (callsState as? UiState.Success)?.data ?: emptyList()

                    when (currentTab) {
                        HostTab.DASHBOARD -> HostDashboardView(
                            host = myHost,
                            earnings = earnings,
                            attendance = attendance,
                            calls = calls,
                            pricing = pricing,
                            onAcceptCall = { type -> onAcceptCallPreview(myHost, type) },
                            onQuickToggleStatus = { status ->
                                onUpdateAvailability(myHost, status, myHost.bio ?: "", myHost.audioAvailable, myHost.videoAvailable)
                            }
                        )
                        HostTab.EARNINGS -> HostEarningsView(earnings = earnings)
                        HostTab.ATTENDANCE -> HostAttendanceView(
                            host = myHost,
                            attendance = attendance,
                            onToggleShift = { checkIn -> onToggleShift(myHost, checkIn) }
                        )
                        HostTab.SALARY -> HostSalaryView(salaries = salaries, earnings = earnings)
                        HostTab.PROFILE -> HostProfileAndFramesView(
                            host = myHost,
                            onUpdateAvailability = onUpdateAvailability
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostApplicationGate(onApply: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Glass3DCard(cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Host Studio Verification", style = MaterialTheme.typography.headlineMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                Text(
                    text = "Submit your creator profile to receive a unique Host ID, admin-assigned 3D Frame, and audio/video call routing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedLavender
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Stage Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("host_apply_name")
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Host Bio & Languages") },
                    modifier = Modifier.fillMaxWidth().testTag("host_apply_bio")
                )
                NeonGradientButton(
                    text = "Apply for Host Approval",
                    enabled = name.trim().length >= 2,
                    onClick = { onApply(name, bio) },
                    modifier = Modifier.fillMaxWidth().testTag("host_apply_submit")
                )
            }
        }
    }
}

@Composable
private fun HostDashboardView(
    host: HostProfile,
    earnings: List<HostEarning>,
    attendance: List<HostAttendance>,
    calls: List<CallSession>,
    pricing: AppPricingSettings,
    onAcceptCall: (CallType) -> Unit,
    onQuickToggleStatus: (OnlineStatus) -> Unit
) {
    val totalEarnings = earnings.sumOf { it.hostShareCoins + it.adjustmentCoins }
    val audioEarnings = earnings.filter { it.category == "AUDIO_CALL" }.sumOf { it.hostShareCoins }
    val videoEarnings = earnings.filter { it.category == "VIDEO_CALL" }.sumOf { it.hostShareCoins }
    val giftEarnings = earnings.filter { it.category == "GIFT" }.sumOf { it.hostShareCoins }
    val totalOnlineMins = attendance.sumOf { it.workedMinutes }
    val incomingCalls = calls.filter { it.callStatusEnum == CallStatus.RINGING }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Host3DFrameAvatar(
                        name = host.name,
                        avatarKey = host.avatarKey,
                        frameTier = host.frameTierEnum,
                        onlineStatus = host.onlineStatusEnum,
                        size = 76.dp
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(host.name, style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                        Text("${host.hostCode} • ${host.frameTierEnum.label} FRAME", style = MaterialTheme.typography.labelSmall, color = GoldenGlow)
                        Text("Approval: ${host.hostStatusEnum.name}", style = MaterialTheme.typography.bodySmall, color = if (host.hostStatusEnum == HostStatus.APPROVED) EmeraldOnline else GoldenGlow)
                    }
                    Switch(
                        checked = host.onlineStatusEnum == OnlineStatus.ONLINE,
                        onCheckedChange = { online ->
                            onQuickToggleStatus(if (online) OnlineStatus.ONLINE else OnlineStatus.OFFLINE)
                        }
                    )
                }
            }
        }

        // Incoming Call Queue Card
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Incoming Call Console", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    if (incomingCalls.isEmpty()) {
                        Text("Status: Listening for incoming audio & video calls (${host.onlineStatusEnum.name}).", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            NeonGradientButton(
                                text = "Join Audio Room",
                                colors = listOf(ElectricViolet, RoyalBlue),
                                onClick = { onAcceptCall(CallType.AUDIO) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Rounded.Call, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(16.dp)) }
                            )
                            NeonGradientButton(
                                text = "Join Video Room",
                                colors = listOf(NeonPink, ElectricViolet),
                                onClick = { onAcceptCall(CallType.VIDEO) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Rounded.Videocam, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    } else {
                        incomingCalls.forEach { inc ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(inc.callerName, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                                    Text("Caller ID: ${inc.callerId.take(10)} • ${inc.callType}", style = MaterialTheme.typography.labelSmall, color = GoldenGlow)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { onAcceptCall(inc.callTypeEnum) }, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.Rounded.Call, contentDescription = "Accept", tint = EmeraldOnline)
                                    }
                                    IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.Rounded.CallEnd, contentDescription = "Reject", tint = CrimsonAlert)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // KPI Grid
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                HostMetricCard("Total Earnings", "$totalEarnings c", GoldenGlow, Modifier.weight(1f))
                HostMetricCard("Audio / Video", "$audioEarnings / $videoEarnings c", ElectricViolet, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                HostMetricCard("Gift Earnings", "$giftEarnings c", NeonPink, Modifier.weight(1f))
                HostMetricCard("Calls / Online", "${host.totalCalls} / ${totalOnlineMins}m", EmeraldOnline, Modifier.weight(1f))
            }
        }

        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Earning Trend (Host Share ${pricing.hostSharePct}%)", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    Spacer(Modifier.height(8.dp))
                    val chartVals = earnings.takeLast(6).map { it.hostShareCoins.toFloat() }
                        .ifEmpty { listOf(0f, 0f, 0f, 0f, 0f, 0f) }
                    AnimatedStatLineChart(
                        values = chartVals,
                        labels = listOf("W1", "W2", "W3", "W4", "W5", "Now")
                    )
                }
            }
        }
    }
}

@Composable
private fun HostMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Glass3DCard(cornerRadius = 18.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedLavender)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HostEarningsView(earnings: List<HostEarning>) {
    var filter by remember { mutableStateOf("Monthly") }
    val audio = earnings.filter { it.category == "AUDIO_CALL" }
    val video = earnings.filter { it.category == "VIDEO_CALL" }
    val gift = earnings.filter { it.category == "GIFT" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Host Earnings", style = MaterialTheme.typography.headlineMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Today", "Yesterday", "Last 7 Days", "Monthly", "Custom")) { label ->
                    FilterChip(
                        selected = filter == label,
                        onClick = { filter = label },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricViolet, selectedLabelColor = SoftWhite)
                    )
                }
            }
        }

        item {
            EarningCategoryChartCard("AUDIO CALL EARNINGS", audio, ElectricViolet)
        }
        item {
            EarningCategoryChartCard("VIDEO CALL EARNINGS", video, NeonPink)
        }
        item {
            EarningCategoryChartCard("GIFT EARNINGS", gift, GoldenGlow)
        }
    }
}

@Composable
private fun EarningCategoryChartCard(title: String, items: List<HostEarning>, accent: Color) {
    val total = items.sumOf { it.hostShareCoins }
    Glass3DCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = accent)
                Text("$total Coins", style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            AnimatedStatLineChart(
                values = items.takeLast(6).map { it.hostShareCoins.toFloat() }.ifEmpty { listOf(0f, 0f, 0f, 0f) },
                labels = emptyList(),
                primaryColor = accent,
                secondaryColor = RoyalBlue
            )
        }
    }
}

@Composable
private fun HostAttendanceView(
    host: HostProfile,
    attendance: List<HostAttendance>,
    onToggleShift: (Boolean) -> Unit
) {
    val isCurrentlyOnline = attendance.firstOrNull()?.status == "ONLINE"
    val totalMins = attendance.sumOf { it.workedMinutes }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Attendance & Shift Tracker", style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                    Text("Total Logged Time: ${totalMins / 60}h ${totalMins % 60}m • Host: ${host.hostCode}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    NeonGradientButton(
                        text = if (isCurrentlyOnline) "Check Out (End Shift)" else "Check In (Start Shift)",
                        colors = if (isCurrentlyOnline) listOf(CrimsonAlert, NeonPink) else listOf(EmeraldOnline, RoyalBlue),
                        onClick = { onToggleShift(!isCurrentlyOnline) },
                        modifier = Modifier.fillMaxWidth().testTag("host_shift_toggle_btn")
                    )
                }
            }
        }

        items(attendance, key = { it.id }) { row ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(row.dateKey, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("In: ${row.loginTimeLabel} • Out: ${row.logoutTimeLabel ?: "Active"}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                    Text("${row.workedMinutes} min", style = MaterialTheme.typography.labelLarge, color = GoldenGlow)
                }
            }
        }
    }
}

@Composable
private fun HostSalaryView(salaries: List<HostSalary>, earnings: List<HostEarning>) {
    val approved = salaries.sumOf { it.approvedAmountInr }
    val pending = salaries.sumOf { it.pendingAmountInr }
    val paid = salaries.sumOf { it.paidAmountInr }
    val pendingCoins = earnings.filter { it.status == "PENDING" }.sumOf { it.hostShareCoins }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = GoldenGlow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Admin-Verified Salary Ledger", style = MaterialTheme.typography.titleMedium, color = GoldenGlow)
                    }
                    Text("Hosts cannot modify earnings or payout statuses.", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("APPROVED", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Text("₹$approved", style = MaterialTheme.typography.titleMedium, color = ElectricViolet)
                        }
                        Column {
                            Text("PENDING", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Text("₹$pending ($pendingCoins c)", style = MaterialTheme.typography.titleMedium, color = GoldenGlow)
                        }
                        Column {
                            Text("PAID", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Text("₹$paid", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline)
                        }
                    }
                }
            }
        }

        items(salaries, key = { it.id }) { sal ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sal.periodLabel, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Surface(
                            color = if (sal.salaryStatusEnum == SalaryStatus.PAID) EmeraldOnline.copy(0.2f) else GoldenGlow.copy(0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(sal.status, style = MaterialTheme.typography.labelSmall, color = SoftWhite, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Text("${sal.payoutMethodEnum.label} • Ref: ${sal.paymentReference ?: "Awaiting release"}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    Text("Paid Date: ${sal.paidDateLabel ?: "-"}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                }
            }
        }
    }
}

@Composable
private fun HostProfileAndFramesView(
    host: HostProfile,
    onUpdateAvailability: (HostProfile, OnlineStatus, String, Boolean, Boolean) -> Unit
) {
    var bio by remember(host.bio) { mutableStateOf(host.bio ?: "") }
    var audioAvail by remember(host.audioAvailable) { mutableStateOf(host.audioAvailable) }
    var videoAvail by remember(host.videoAvailable) { mutableStateOf(host.videoAvailable) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Host Profile & Availability", style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Audio Calls Enabled", color = SoftWhite)
                        Switch(checked = audioAvail, onCheckedChange = { audioAvail = it })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Video Calls Enabled", color = SoftWhite)
                        Switch(checked = videoAvail, onCheckedChange = { videoAvail = it })
                    }
                    NeonGradientButton(
                        text = "Save Availability",
                        onClick = { onUpdateAvailability(host, host.onlineStatusEnum, bio, audioAvail, videoAvail) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Admin-Controlled 3D Frame Showcase
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = GoldenGlow)
                        Spacer(Modifier.width(8.dp))
                        Text("Admin-Controlled 3D Frame System", style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Your assigned frame: ${host.frameTierEnum.label}. Frames are exclusively assigned by Vibra Admins and cannot be purchased or edited.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(FrameTier.entries) { tier ->
                            val isAssigned = host.frameTierEnum == tier
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isAssigned) ElectricViolet.copy(0.22f) else DarkNavy.copy(0.6f))
                                    .border(1.dp, if (isAssigned) GoldenGlow else Color.Transparent, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Host3DFrameAvatar(
                                    name = host.name,
                                    avatarKey = host.avatarKey,
                                    frameTier = tier,
                                    onlineStatus = null,
                                    size = 64.dp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(tier.label, style = MaterialTheme.typography.labelSmall, color = if (isAssigned) GoldenGlow else SoftWhite)
                                if (isAssigned) {
                                    Text("ASSIGNED", style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HostNotificationsView(notifications: List<AppNotification>, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Host Alerts", style = MaterialTheme.typography.titleLarge, color = SoftWhite)
            OutlinedButton(onClick = onClose) { Text("Done", color = SoftWhite) }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications, key = { it.id }) { n ->
                Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(n.title, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text(n.body, style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                }
            }
        }
    }
}
