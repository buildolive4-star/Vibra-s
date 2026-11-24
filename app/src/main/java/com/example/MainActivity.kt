package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.AppMode
import com.example.data.model.HostProfile
import com.example.data.model.UiState
import com.example.data.preferences.VibraPreferences
import com.example.data.repository.VibraAdminAndHostRepository
import com.example.data.repository.VibraAuthRepository
import com.example.data.repository.VibraCallAndChatRepository
import com.example.data.repository.VibraPlatformRepository
import com.example.ui.components.Glass3DCard
import com.example.ui.components.VibraCosmicBackground
import com.example.ui.screens.admin.AdminControlCenterScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.VibraOnboardingScreen
import com.example.ui.screens.auth.VibraSplashScreen
import com.example.ui.screens.caller.ActiveCallScreen
import com.example.ui.screens.caller.CallHistoryScreen
import com.example.ui.screens.caller.CallerHomeScreen
import com.example.ui.screens.caller.ChatHubScreen
import com.example.ui.screens.caller.GiftShopSheet
import com.example.ui.screens.caller.HostDiscoveryScreen
import com.example.ui.screens.caller.HostProfileScreen
import com.example.ui.screens.caller.NotificationsScreen
import com.example.ui.screens.caller.RechargeScreen
import com.example.ui.screens.caller.UserProfileScreen
import com.example.ui.screens.caller.WalletScreen
import com.example.ui.screens.host.HostStudioScreen
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.VibraTheme
import com.example.ui.viewmodel.VibraAdminViewModel
import com.example.ui.viewmodel.VibraCallerViewModel
import com.example.ui.viewmodel.VibraHostViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibraTheme {
                VibraAppRoot()
            }
        }
    }
}

private enum class CallerTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    DISCOVER("Discover", Icons.Rounded.Explore),
    CHATS("Chats", Icons.AutoMirrored.Rounded.Chat),
    HISTORY("History", Icons.Rounded.History),
    PROFILE("Profile", Icons.Rounded.Person)
}

private sealed interface CallerOverlay {
    data class HostDetail(val host: HostProfile) : CallerOverlay
    data object WalletView : CallerOverlay
    data object RechargeView : CallerOverlay
    data object NotificationsView : CallerOverlay
}

@Composable
fun VibraAppRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val prefs = remember(app) { VibraPreferences(app) }
    val scope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(true) }
    val onboardingDone by prefs.onboardingCompleted.collectAsStateWithLifecycle(initialValue = true)
    val preferredMode by prefs.preferredMode.collectAsStateWithLifecycle(initialValue = AppMode.CALLER)
    val particlesEnabled by prefs.particlesEnabled.collectAsStateWithLifecycle(initialValue = true)

    val firebaseReady = remember(context) {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    }
    if (!firebaseReady) {
        VibraCosmicBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Glass3DCard(
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Firebase Config Required",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SoftWhite
                        )
                        Text(
                            text = "This APK was built without app/google-services.json. Export the APK directly from AI Studio or add GOOGLE_SERVICES_JSON in GitHub Secrets and re-run the workflow.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedLavender
                        )
                    }
                }
            }
        }
        return
    }

    val currentUser by VibraAuthRepository.authStateFlow().collectAsStateWithLifecycle(
        initialValue = Firebase.auth.currentUser
    )
    var pendingPhone by remember { mutableStateOf<String?>(null) }
    var pendingCountryCode by remember { mutableStateOf("+91") }

    if (showSplash) {
        VibraSplashScreen(onFinished = { showSplash = false })
        return
    }

    if (!onboardingDone) {
        VibraOnboardingScreen(
            onComplete = {
                scope.launch { prefs.setOnboardingCompleted(true) }
            }
        )
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { phone, cc ->
                pendingPhone = phone
                pendingCountryCode = cc
            }
        )
        return
    }

    androidx.compose.runtime.key(user.uid) {
        AuthenticatedVibraShell(
            app = app,
            user = user,
            initialPhone = pendingPhone,
            initialCountryCode = pendingCountryCode,
            preferredMode = preferredMode,
            particlesEnabled = particlesEnabled,
            onUpdatePreferredMode = { mode -> scope.launch { prefs.setPreferredMode(mode) } },
            onToggleParticles = { enabled -> scope.launch { prefs.setParticlesEnabled(enabled) } }
        )
    }
}

@Composable
private fun AuthenticatedVibraShell(
    app: Application,
    user: FirebaseUser,
    initialPhone: String?,
    initialCountryCode: String,
    preferredMode: AppMode,
    particlesEnabled: Boolean,
    onUpdatePreferredMode: (AppMode) -> Unit,
    onToggleParticles: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val callerVm: VibraCallerViewModel = viewModel(
        key = "caller_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                VibraCallerViewModel(
                    platformRepo = VibraPlatformRepository(db),
                    callChatRepo = VibraCallAndChatRepository(db),
                    currentUserId = user.uid,
                    initialDisplayName = user.displayName ?: "Vibra Explorer",
                    initialPhone = initialPhone,
                    initialCountryCode = initialCountryCode,
                    initialPhotoUrl = user.photoUrl?.toString()
                )
            }
        }
    )

    val hostVm: VibraHostViewModel = viewModel(
        key = "host_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                VibraHostViewModel(
                    adminHostRepo = VibraAdminAndHostRepository(db),
                    currentUserId = user.uid
                )
            }
        }
    )

    val adminVm: VibraAdminViewModel = viewModel(
        key = "admin_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                VibraAdminViewModel(
                    adminRepo = VibraAdminAndHostRepository(db),
                    isAdmin = callerVm.isAdmin
                )
            }
        }
    )

    var activeMode by remember(preferredMode, callerVm.isAdmin) {
        mutableStateOf(
            if (preferredMode == AppMode.ADMIN && !callerVm.isAdmin) AppMode.CALLER else preferredMode
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val callerBanner by callerVm.actionBanner.collectAsStateWithLifecycle()
    val hostBanner by hostVm.hostBanner.collectAsStateWithLifecycle()
    val adminBanner by adminVm.adminBanner.collectAsStateWithLifecycle()

    LaunchedEffect(callerBanner) {
        callerBanner?.let { (_, msg) ->
            snackbarHostState.showSnackbar(msg)
            callerVm.clearBanner()
        }
    }
    LaunchedEffect(hostBanner) {
        hostBanner?.let { (_, msg) ->
            snackbarHostState.showSnackbar(msg)
            hostVm.clearBanner()
        }
    }
    LaunchedEffect(adminBanner) {
        adminBanner?.let { (_, msg) ->
            snackbarHostState.showSnackbar(msg)
            adminVm.clearBanner()
        }
    }

    val userProfileState by callerVm.userProfile.collectAsStateWithLifecycle()
    val hostsState by callerVm.hosts.collectAsStateWithLifecycle()
    val pricing by callerVm.pricingSettings.collectAsStateWithLifecycle()
    val wallet by callerVm.wallet.collectAsStateWithLifecycle()
    val notificationsState by callerVm.notifications.collectAsStateWithLifecycle()
    val giftsState by callerVm.gifts.collectAsStateWithLifecycle()
    val activeCall by callerVm.activeCall.collectAsStateWithLifecycle()
    val callElapsed by callerVm.callElapsedSeconds.collectAsStateWithLifecycle()
    val isMuted by callerVm.isMuted.collectAsStateWithLifecycle()
    val isSpeakerOn by callerVm.isSpeakerOn.collectAsStateWithLifecycle()
    val isCameraOn by callerVm.isCameraOn.collectAsStateWithLifecycle()
    val isFrontCamera by callerVm.isFrontCamera.collectAsStateWithLifecycle()

    var giftShopHost by remember { mutableStateOf<HostProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentCall = activeCall
        if (currentCall != null) {
            val allHosts = (hostsState as? UiState.Success)?.data ?: emptyList()
            val callHost = allHosts.firstOrNull { it.id == currentCall.hostId }
            ActiveCallScreen(
                call = currentCall,
                host = callHost,
                wallet = wallet,
                elapsedSeconds = callElapsed,
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                isCameraOn = isCameraOn,
                isFrontCamera = isFrontCamera,
                onToggleMute = callerVm::toggleMute,
                onToggleSpeaker = callerVm::toggleSpeaker,
                onToggleCamera = callerVm::toggleCamera,
                onSwitchCamera = callerVm::switchCamera,
                onUpdateConsent = callerVm::toggleRecordingConsent,
                onOpenGiftShop = { if (callHost != null) giftShopHost = callHost },
                onEndCall = { callerVm.endActiveCall() }
            )
        } else {
            when (activeMode) {
                AppMode.CALLER -> {
                    CallerMainWorkspace(
                        callerVm = callerVm,
                        particlesEnabled = particlesEnabled,
                        onToggleParticles = onToggleParticles,
                        onOpenGiftShop = { giftShopHost = it },
                        onSwitchMode = { next ->
                            activeMode = next
                            onUpdatePreferredMode(next)
                        },
                        onSignOut = {
                            VibraAuthRepository.signOut(credentialManager, scope)
                        }
                    )
                }

                AppMode.HOST -> {
                    val hostEarnings by hostVm.hostEarnings.collectAsStateWithLifecycle()
                    val hostAttendance by hostVm.hostAttendance.collectAsStateWithLifecycle()
                    val hostSalaries by hostVm.hostSalaries.collectAsStateWithLifecycle()
                    val calls by callerVm.callHistory.collectAsStateWithLifecycle()
                    HostStudioScreen(
                        currentUserId = user.uid,
                        hostsState = hostsState,
                        earningsState = hostEarnings,
                        attendanceState = hostAttendance,
                        salariesState = hostSalaries,
                        callsState = calls,
                        notificationsState = notificationsState,
                        pricing = pricing,
                        onApplyAsHost = hostVm::applyAsHost,
                        onUpdateAvailability = hostVm::updateAvailability,
                        onToggleShift = hostVm::toggleShift,
                        onAcceptCallPreview = { host, type ->
                            callerVm.startCall(host, type) {}
                        },
                        onExitHostMode = {
                            activeMode = AppMode.CALLER
                            onUpdatePreferredMode(AppMode.CALLER)
                        }
                    )
                }

                AppMode.ADMIN -> {
                    AdminControlCenterScreen(
                        adminVm = adminVm,
                        callerVm = callerVm,
                        onExitAdmin = {
                            activeMode = AppMode.CALLER
                            onUpdatePreferredMode(AppMode.CALLER)
                        }
                    )
                }
            }
        }

        val targetGiftHost = giftShopHost
        if (targetGiftHost != null) {
            GiftShopSheet(
                host = targetGiftHost,
                giftsState = giftsState,
                wallet = wallet,
                onDismiss = { giftShopHost = null },
                onSendGift = { gift ->
                    callerVm.sendGift(targetGiftHost, gift, activeCall?.id)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(bottom = 94.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            val isSuccess = (callerBanner?.first ?: hostBanner?.first ?: adminBanner?.first) != false
            Snackbar(
                containerColor = if (isSuccess) DeepSurface else CrimsonAlert,
                contentColor = SoftWhite,
                actionColor = EmeraldOnline,
                snackbarData = data
            )
        }
    }
}

@Composable
private fun CallerMainWorkspace(
    callerVm: VibraCallerViewModel,
    particlesEnabled: Boolean,
    onToggleParticles: (Boolean) -> Unit,
    onOpenGiftShop: (HostProfile) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(CallerTab.HOME) }
    var overlay by remember { mutableStateOf<CallerOverlay?>(null) }

    val userProfileState by callerVm.userProfile.collectAsStateWithLifecycle()
    val hostsState by callerVm.hosts.collectAsStateWithLifecycle()
    val pricing by callerVm.pricingSettings.collectAsStateWithLifecycle()
    val wallet by callerVm.wallet.collectAsStateWithLifecycle()
    val walletTxs by callerVm.walletTransactions.collectAsStateWithLifecycle()
    val payments by callerVm.payments.collectAsStateWithLifecycle()
    val packages by callerVm.rechargePackages.collectAsStateWithLifecycle()
    val notifications by callerVm.notifications.collectAsStateWithLifecycle()
    val calls by callerVm.callHistory.collectAsStateWithLifecycle()
    val threads by callerVm.chatThreads.collectAsStateWithLifecycle()
    val activeThread by callerVm.activeChatThread.collectAsStateWithLifecycle()
    val messages by callerVm.activeMessages.collectAsStateWithLifecycle()

    val approvedHosts = ((hostsState as? UiState.Success)?.data ?: emptyList())

    if (overlay != null) {
        BackHandler { overlay = null }
    } else if (currentTab != CallerTab.HOME) {
        BackHandler { currentTab = CallerTab.HOME }
    }

    VibraCosmicBackground(particlesEnabled = particlesEnabled) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 700.dp

            val renderMainContent: @Composable (Modifier) -> Unit = { contentMod ->
                Box(modifier = contentMod.statusBarsPadding()) {
                    when (val activeOverlay = overlay) {
                        is CallerOverlay.HostDetail -> HostProfileScreen(
                            host = activeOverlay.host,
                            pricing = pricing,
                            onBack = { overlay = null },
                            onStartCall = { host, type -> callerVm.startCall(host, type) {} },
                            onOpenChat = { host ->
                                callerVm.openChatWithHost(host) {
                                    overlay = null
                                    currentTab = CallerTab.CHATS
                                }
                            },
                            onOpenGiftShop = onOpenGiftShop,
                            onReportOrBlock = { reason, details, actionType ->
                                callerVm.submitReportOrBlock(activeOverlay.host.id, activeOverlay.host.name, reason, details, actionType)
                            }
                        )
                        CallerOverlay.WalletView -> WalletScreen(
                            wallet = wallet,
                            pricing = pricing,
                            transactionsState = walletTxs,
                            paymentsState = payments,
                            onOpenRecharge = { overlay = CallerOverlay.RechargeView },
                            onBack = { overlay = null }
                        )
                        CallerOverlay.RechargeView -> RechargeScreen(
                            packagesState = packages,
                            pricing = pricing,
                            wallet = wallet,
                            onBack = { overlay = null },
                            onConfirmPayment = { pkg, gw, ref ->
                                callerVm.rechargeWallet(pkg, gw, ref)
                            }
                        )
                        CallerOverlay.NotificationsView -> NotificationsScreen(
                            notificationsState = notifications,
                            onMarkRead = callerVm::markNotificationRead,
                            onBack = { overlay = null }
                        )
                        null -> when (currentTab) {
                            CallerTab.HOME -> CallerHomeScreen(
                                userProfileState = userProfileState,
                                hostsState = hostsState,
                                wallet = wallet,
                                pricing = pricing,
                                notificationsState = notifications,
                                onOpenProfile = { currentTab = CallerTab.PROFILE },
                                onOpenWallet = { overlay = CallerOverlay.WalletView },
                                onOpenRecharge = { overlay = CallerOverlay.RechargeView },
                                onOpenNotifications = { overlay = CallerOverlay.NotificationsView },
                                onOpenDiscovery = { currentTab = CallerTab.DISCOVER },
                                onOpenHostProfile = { overlay = CallerOverlay.HostDetail(it) },
                                onStartCall = { host, type -> callerVm.startCall(host, type) {} },
                                onOpenGiftShop = onOpenGiftShop
                            )
                            CallerTab.DISCOVER -> HostDiscoveryScreen(
                                hostsState = hostsState,
                                pricing = pricing,
                                onOpenHostProfile = { overlay = CallerOverlay.HostDetail(it) },
                                onStartCall = { host, type -> callerVm.startCall(host, type) {} },
                                onOpenGiftShop = onOpenGiftShop
                            )
                            CallerTab.CHATS -> ChatHubScreen(
                                threadsState = threads,
                                activeThread = activeThread,
                                messagesState = messages,
                                hosts = approvedHosts,
                                currentUserId = callerVm.currentUserId,
                                onSelectThread = callerVm::selectChatThread,
                                onStartChatWithHost = { host -> callerVm.openChatWithHost(host) {} },
                                onClearThread = callerVm::clearActiveChatThread,
                                onSendMessage = { text, type, label -> callerVm.sendMessage(text, type, label) },
                                onOpenGiftShop = onOpenGiftShop,
                                onReportOrBlock = { id, name, act ->
                                    callerVm.submitReportOrBlock(id, name, "Chat safety report", null, act)
                                }
                            )
                            CallerTab.HISTORY -> CallHistoryScreen(callsState = calls)
                            CallerTab.PROFILE -> UserProfileScreen(
                                userProfileState = userProfileState,
                                wallet = wallet,
                                currentUserId = callerVm.currentUserId,
                                isAdmin = callerVm.isAdmin,
                                particlesEnabled = particlesEnabled,
                                onToggleParticles = onToggleParticles,
                                onUpdateProfile = callerVm::updateProfile,
                                onOpenWallet = { overlay = CallerOverlay.WalletView },
                                onSwitchAppMode = onSwitchMode,
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }

            if (useRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (overlay == null) {
                        NavigationRail(
                            containerColor = DeepSurface.copy(alpha = 0.94f),
                            modifier = Modifier.fillMaxHeight().statusBarsPadding()
                        ) {
                            CallerTab.entries.forEach { tab ->
                                NavigationRailItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, maxLines = 1) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = NeonPink,
                                        selectedTextColor = SoftWhite,
                                        unselectedIconColor = MutedLavender,
                                        unselectedTextColor = MutedLavender,
                                        indicatorColor = ElectricViolet.copy(0.28f)
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                    renderMainContent(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.navigationBars,
                    bottomBar = {
                        if (overlay == null) {
                            NavigationBar(
                                containerColor = DeepSurface.copy(alpha = 0.94f)
                            ) {
                                CallerTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NeonPink,
                                            selectedTextColor = SoftWhite,
                                            unselectedIconColor = MutedLavender,
                                            unselectedTextColor = MutedLavender,
                                            indicatorColor = ElectricViolet.copy(0.28f)
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    renderMainContent(Modifier.fillMaxSize().padding(innerPadding))
                }
            }
        }
    }
}
