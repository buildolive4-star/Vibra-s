package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallType
import com.example.data.model.HostFrame
import com.example.data.model.HostProfile
import com.example.data.model.RechargePackage
import com.example.data.model.UiState
import com.example.data.model.VirtualGift
import com.example.ui.components.Glass3DCard
import com.example.ui.components.SecureScreenEffect
import com.example.ui.components.VibraCosmicBackground
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.VibraAdminViewModel
import com.example.ui.viewmodel.VibraCallerViewModel
import kotlinx.coroutines.launch

enum class AdminSection(val title: String, val icon: ImageVector, val sensitive: Boolean = false) {
    OVERVIEW("Overview", Icons.Rounded.Dashboard),
    HOSTS("Hosts", Icons.Rounded.People),
    CALLERS("Callers", Icons.Rounded.Group),
    HOST_FRAMES("Host Frames", Icons.Rounded.AutoAwesome),
    AUDIO_CALLS("Audio Calls", Icons.Rounded.Call),
    VIDEO_CALLS("Video Calls", Icons.Rounded.Videocam),
    CALL_MONITORING("Call Monitoring", Icons.Rounded.GraphicEq),
    CONSENTED_RECORDINGS("Consented Recordings", Icons.Rounded.Lock, sensitive = true),
    RECHARGE_MGMT("Recharge Management", Icons.Rounded.CurrencyRupee),
    WALLET_MGMT("Wallet Management", Icons.Rounded.AccountBalanceWallet, sensitive = true),
    HOST_EARNINGS("Host Earnings", Icons.Rounded.MonetizationOn),
    SALARY_MGMT("Salary Management", Icons.Rounded.AccountBalance, sensitive = true),
    GIFTS_MGMT("Gifts Management", Icons.Rounded.CardGiftcard),
    CALL_PRICING("Call Pricing", Icons.Rounded.Settings),
    TIME_ADJUSTMENT("Time Adjustment", Icons.Rounded.Timer),
    ATTENDANCE("Attendance", Icons.Rounded.EventAvailable),
    NOTIFICATIONS("Notifications", Icons.Rounded.Notifications),
    COMPLAINTS("Complaints", Icons.Rounded.ReportProblem),
    FINANCIAL_REPORTS("Financial Reports", Icons.Rounded.Analytics, sensitive = true),
    SECURITY_AUDIT("Security & Audit Logs", Icons.Rounded.Security, sensitive = true),
    SETTINGS("Settings", Icons.Rounded.AdminPanelSettings)
}

@Composable
fun AdminControlCenterScreen(
    adminVm: VibraAdminViewModel,
    callerVm: VibraCallerViewModel,
    onExitAdmin: () -> Unit
) {
    BackHandler(onBack = onExitAdmin)

    var selectedSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
    SecureScreenEffect(enabled = selectedSection.sensitive)

    val hostsState by callerVm.hosts.collectAsStateWithLifecycle()
    val framesState by callerVm.hostFrames.collectAsStateWithLifecycle()
    val packagesState by callerVm.rechargePackages.collectAsStateWithLifecycle()
    val giftsState by callerVm.gifts.collectAsStateWithLifecycle()
    val pricing by callerVm.pricingSettings.collectAsStateWithLifecycle()

    val usersState by adminVm.allUsers.collectAsStateWithLifecycle()
    val walletsState by adminVm.allWallets.collectAsStateWithLifecycle()
    val callsState by adminVm.allCalls.collectAsStateWithLifecycle()
    val paymentsState by adminVm.allPayments.collectAsStateWithLifecycle()
    val earningsState by adminVm.allEarnings.collectAsStateWithLifecycle()
    val salariesState by adminVm.allSalaries.collectAsStateWithLifecycle()
    val attendanceState by adminVm.allAttendance.collectAsStateWithLifecycle()
    val recordingsState by adminVm.allRecordings.collectAsStateWithLifecycle()
    val complaintsState by adminVm.allComplaints.collectAsStateWithLifecycle()
    val auditLogsState by adminVm.auditLogs.collectAsStateWithLifecycle()

    val hosts = (hostsState as? UiState.Success)?.data ?: emptyList()
    val frames = (framesState as? UiState.Success)?.data ?: emptyList()
    val packages = (packagesState as? UiState.Success)?.data ?: emptyList()
    val gifts = (giftsState as? UiState.Success)?.data ?: emptyList()
    val users = (usersState as? UiState.Success)?.data ?: emptyList()
    val wallets = (walletsState as? UiState.Success)?.data ?: emptyList()
    val calls = (callsState as? UiState.Success)?.data ?: emptyList()
    val payments = (paymentsState as? UiState.Success)?.data ?: emptyList()
    val earnings = (earningsState as? UiState.Success)?.data ?: emptyList()
    val salaries = (salariesState as? UiState.Success)?.data ?: emptyList()
    val attendance = (attendanceState as? UiState.Success)?.data ?: emptyList()
    val recordings = (recordingsState as? UiState.Success)?.data ?: emptyList()
    val complaints = (complaintsState as? UiState.Success)?.data ?: emptyList()
    val auditLogs = (auditLogsState as? UiState.Success)?.data ?: emptyList()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    VibraCosmicBackground {
        if (!adminVm.isAdmin) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Glass3DCard(cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Admin Authorization Required", style = MaterialTheme.typography.headlineMedium, color = CrimsonAlert)
                        Text(
                            "Access to the Vibra Admin Control Center is restricted by zero-trust Firestore rules to verified platform administrators.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedLavender
                        )
                        Surface(
                            color = ElectricViolet,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable(onClick = onExitAdmin)
                        ) {
                            Text("Return to Caller App", color = SoftWhite, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                        }
                    }
                }
            }
            return@VibraCosmicBackground
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 840.dp

            val contentScaffold: @Composable (showMenuButton: Boolean) -> Unit = { showMenu ->
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showMenu) {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.size(48.dp).testTag("admin_drawer_btn")
                                    ) {
                                        Icon(Icons.Rounded.Menu, contentDescription = "Admin menu", tint = SoftWhite)
                                    }
                                }
                                IconButton(
                                    onClick = onExitAdmin,
                                    modifier = Modifier.size(48.dp).testTag("admin_exit_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Exit Admin", tint = SoftWhite)
                                }
                                Column {
                                    Text(selectedSection.title, style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                                    Text("VIBRA ADMIN CONTROL CENTER", style = MaterialTheme.typography.labelSmall, color = GoldenGlow)
                                }
                            }
                            if (selectedSection.sensitive) {
                                Surface(color = CrimsonAlert.copy(0.22f), shape = RoundedCornerShape(10.dp)) {
                                    Text("FLAG_SECURE", style = MaterialTheme.typography.labelSmall, color = SoftWhite, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .navigationBarsPadding()
                    ) {
                        AdminSectionRouter(
                            section = selectedSection,
                            adminVm = adminVm,
                            hosts = hosts,
                            frames = frames,
                            packages = packages,
                            gifts = gifts,
                            pricing = pricing,
                            users = users,
                            wallets = wallets,
                            calls = calls,
                            payments = payments,
                            earnings = earnings,
                            salaries = salaries,
                            attendance = attendance,
                            recordings = recordings,
                            complaints = complaints,
                            auditLogs = auditLogs,
                            onSelectSection = { selectedSection = it }
                        )
                    }
                }
            }

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AdminSidebarList(
                        selected = selectedSection,
                        onSelect = { selectedSection = it },
                        modifier = Modifier
                            .width(268.dp)
                            .fillMaxHeight()
                            .background(DeepSurface.copy(alpha = 0.92f))
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        contentScaffold(false)
                    }
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = DeepSurface) {
                            AdminSidebarList(
                                selected = selectedSection,
                                onSelect = {
                                    selectedSection = it
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.width(280.dp).fillMaxHeight()
                            )
                        }
                    }
                ) {
                    contentScaffold(true)
                }
            }
        }
    }
}

@Composable
private fun AdminSidebarList(
    selected: AdminSection,
    onSelect: (AdminSection) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                Text("VIBRA CORE", style = MaterialTheme.typography.titleMedium, color = GoldenGlow, fontWeight = FontWeight.Bold)
                Text("21 Control Modules", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
            }
        }
        items(AdminSection.entries) { section ->
            val active = selected == section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (active) Brush.horizontalGradient(listOf(ElectricViolet.copy(0.75f), NeonPink.copy(0.45f)))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(
                        width = if (active) 1.dp else 0.dp,
                        color = if (active) GoldenGlow.copy(0.5f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(section) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("admin_nav_${section.name.lowercase()}")
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.title,
                    tint = if (active) SoftWhite else MutedLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) SoftWhite else MutedLavender,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun AdminSectionRouter(
    section: AdminSection,
    adminVm: VibraAdminViewModel,
    hosts: List<HostProfile>,
    frames: List<HostFrame>,
    packages: List<RechargePackage>,
    gifts: List<VirtualGift>,
    pricing: AppPricingSettings,
    users: List<com.example.data.model.UserProfile>,
    wallets: List<com.example.data.model.Wallet>,
    calls: List<com.example.data.model.CallSession>,
    payments: List<com.example.data.model.PaymentRecord>,
    earnings: List<com.example.data.model.HostEarning>,
    salaries: List<com.example.data.model.HostSalary>,
    attendance: List<com.example.data.model.HostAttendance>,
    recordings: List<com.example.data.model.ConsentedRecording>,
    complaints: List<com.example.data.model.Complaint>,
    auditLogs: List<com.example.data.model.AdminAuditLog>,
    onSelectSection: (AdminSection) -> Unit
) {
    when (section) {
        AdminSection.OVERVIEW -> AdminOverviewSection(users, hosts, calls, payments, salaries, complaints, onSelectSection)
        AdminSection.HOSTS -> AdminHostsSection(hosts, adminVm)
        AdminSection.CALLERS -> AdminCallersSection(users, wallets, adminVm)
        AdminSection.HOST_FRAMES -> AdminHostFramesSection(frames, adminVm)
        AdminSection.AUDIO_CALLS -> AdminCallsSection("Audio Calls", calls.filter { it.callTypeEnum == CallType.AUDIO })
        AdminSection.VIDEO_CALLS -> AdminCallsSection("Video Calls", calls.filter { it.callTypeEnum == CallType.VIDEO })
        AdminSection.CALL_MONITORING -> AdminCallsSection("Live Call Monitoring", calls)
        AdminSection.CONSENTED_RECORDINGS -> AdminConsentedRecordingsSection(recordings, adminVm)
        AdminSection.RECHARGE_MGMT -> AdminRechargeManagementSection(payments, packages, adminVm)
        AdminSection.WALLET_MGMT -> AdminWalletManagementSection(wallets, users, adminVm)
        AdminSection.HOST_EARNINGS -> AdminHostEarningsSection(earnings)
        AdminSection.SALARY_MGMT -> AdminSalaryManagementSection(salaries, hosts, adminVm)
        AdminSection.GIFTS_MGMT -> AdminGiftsManagementSection(gifts, adminVm)
        AdminSection.CALL_PRICING -> AdminCallPricingSection(pricing, adminVm)
        AdminSection.TIME_ADJUSTMENT -> AdminTimeAdjustmentSection(calls, adminVm)
        AdminSection.ATTENDANCE -> AdminAttendanceSection(attendance)
        AdminSection.NOTIFICATIONS -> AdminNotificationsSection(users, adminVm)
        AdminSection.COMPLAINTS -> AdminComplaintsSection(complaints, adminVm)
        AdminSection.FINANCIAL_REPORTS -> AdminFinancialReportsSection(payments, earnings, salaries)
        AdminSection.SECURITY_AUDIT -> AdminSecurityAuditSection(auditLogs)
        AdminSection.SETTINGS -> AdminSettingsSection(pricing, adminVm)
    }
}
