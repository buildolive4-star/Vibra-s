package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UiState
import com.example.data.preferences.BetZonePreferences
import com.example.data.repository.BetZoneAuthRepository
import com.example.ui.components.BetZoneBackdrop
import com.example.ui.components.BetZoneFloatingBottomBar
import com.example.ui.components.BottomNavTab
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.predictions.PredictionsHubScreen
import com.example.ui.screens.profile.NotificationsScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.screens.profile.UserProfileScreen
import com.example.ui.screens.social.LeaderboardScreen
import com.example.ui.screens.social.RewardsScreen
import com.example.ui.screens.sports.LiveMatchCentreScreen
import com.example.ui.screens.sports.MatchDetailsScreen
import com.example.ui.screens.sports.SportsCategoryScreen
import com.example.ui.theme.BetZoneTheme
import com.example.ui.viewmodel.BetZoneViewModel
import com.example.ui.viewmodel.BetZoneViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

sealed interface BetZoneRoute {
    data object Home : BetZoneRoute
    data object Sports : BetZoneRoute
    data class Predictions(val matchId: String? = null) : BetZoneRoute
    data object Leaderboard : BetZoneRoute
    data object Profile : BetZoneRoute
    data class MatchDetails(val matchId: String) : BetZoneRoute
    data class LiveCentre(val matchId: String) : BetZoneRoute
    data object Rewards : BetZoneRoute
    data object Notifications : BetZoneRoute
    data object Settings : BetZoneRoute
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BetZoneRootApp()
        }
    }
}

@Composable
fun BetZoneRootApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { BetZonePreferences(context) }
    val authRepository = remember { BetZoneAuthRepository(context) }

    val darkModePref by preferences.darkMode.collectAsStateWithLifecycle(initialValue = true)
    val onboardingDone by preferences.onboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
    val languagePref by preferences.language.collectAsStateWithLifecycle(initialValue = "English")
    val pendingPhone by preferences.pendingPhoneInfo.collectAsStateWithLifecycle(initialValue = "+91" to "")

    var splashFinished by rememberSaveable { mutableStateOf(false) }

    val firebaseReady = remember { FirebaseApp.getApps(context).isNotEmpty() }
    var currentUser by remember {
        mutableStateOf<FirebaseUser?>(if (firebaseReady) Firebase.auth.currentUser else null)
    }

    if (firebaseReady) {
        DisposableEffect(Unit) {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                currentUser = auth.currentUser
            }
            Firebase.auth.addAuthStateListener(listener)
            onDispose { Firebase.auth.removeAuthStateListener(listener) }
        }
        LaunchedEffect(splashFinished, onboardingDone) {
            if (splashFinished && onboardingDone && currentUser == null) {
                authRepository.attemptAutoSignIn(context)
                currentUser = authRepository.currentUser()
            }
        }
    }

    BetZoneTheme(darkTheme = darkModePref) {
        when {
            !splashFinished -> {
                SplashScreen(onFinished = { splashFinished = true })
            }
            !onboardingDone -> {
                OnboardingScreen(
                    onComplete = {
                        scope.launch { preferences.setOnboardingCompleted(true) }
                    }
                )
            }
            currentUser == null -> {
                LoginScreen(
                    authRepository = authRepository,
                    preferences = preferences,
                    onAuthSuccess = {
                        currentUser = authRepository.currentUser()
                    }
                )
            }
            else -> {
                val activeUser = currentUser!!
                key(activeUser.uid) {
                    BetZoneAuthenticatedShell(
                        uid = activeUser.uid,
                        displayNameHint = activeUser.displayName ?: "Sports Strategist",
                        countryCodeHint = pendingPhone.first,
                        phoneHint = pendingPhone.second,
                        darkModePref = darkModePref,
                        languagePref = languagePref,
                        preferences = preferences,
                        onLogout = {
                            scope.launch {
                                authRepository.signOut()
                                currentUser = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BetZoneAuthenticatedShell(
    uid: String,
    displayNameHint: String,
    countryCodeHint: String,
    phoneHint: String,
    darkModePref: Boolean,
    languagePref: String,
    preferences: BetZonePreferences,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: BetZoneViewModel = viewModel(
        key = uid,
        factory = BetZoneViewModelFactory(
            context = context,
            preferences = preferences,
            displayNameHint = displayNameHint,
            countryCodeHint = countryCodeHint,
            phoneHint = phoneHint,
            darkModeDefault = darkModePref
        )
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val predictionsState by viewModel.predictionsState.collectAsStateWithLifecycle()
    val claimsState by viewModel.rewardClaimsState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val selectedSport by viewModel.selectedSport.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedTournament by viewModel.selectedTournamentFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val leaderboardPeriod by viewModel.leaderboardPeriod.collectAsStateWithLifecycle()
    val feedbackBanner by viewModel.feedbackBanner.collectAsStateWithLifecycle()
    val isBusy by viewModel.isActionBusy.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data
    val predictions = (predictionsState as? UiState.Success)?.data ?: emptyList()
    val claims = (claimsState as? UiState.Success)?.data ?: emptyList()
    val notifications = (notificationsState as? UiState.Success)?.data ?: emptyList()
    val leaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()
    val unreadCount = notifications.count { it.unread }

    var backStack by remember { mutableStateOf<List<BetZoneRoute>>(listOf(BetZoneRoute.Home)) }
    val currentRoute = backStack.last()

    fun navigateTo(route: BetZoneRoute) {
        backStack = if (route is BetZoneRoute.Home) listOf(BetZoneRoute.Home) else backStack + route
    }

    fun switchTab(tab: BottomNavTab) {
        val target = when (tab) {
            BottomNavTab.HOME -> BetZoneRoute.Home
            BottomNavTab.SPORTS -> BetZoneRoute.Sports
            BottomNavTab.PREDICTIONS -> BetZoneRoute.Predictions(null)
            BottomNavTab.LEADERBOARD -> BetZoneRoute.Leaderboard
            BottomNavTab.PROFILE -> BetZoneRoute.Profile
        }
        backStack = listOf(target)
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        } else if (currentRoute !is BetZoneRoute.Home) {
            backStack = listOf(BetZoneRoute.Home)
        }
    }

    BackHandler(enabled = backStack.size > 1 || currentRoute !is BetZoneRoute.Home) {
        popBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(feedbackBanner) {
        val msg = feedbackBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    val currentBottomTab = when (currentRoute) {
        BetZoneRoute.Home -> BottomNavTab.HOME
        BetZoneRoute.Sports -> BottomNavTab.SPORTS
        is BetZoneRoute.Predictions -> BottomNavTab.PREDICTIONS
        BetZoneRoute.Leaderboard -> BottomNavTab.LEADERBOARD
        BetZoneRoute.Profile -> BottomNavTab.PROFILE
        else -> null
    }

    BetZoneBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (currentBottomTab != null) 88.dp else 12.dp)
                )
            }
        ) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (val route = currentRoute) {
                    BetZoneRoute.Home -> HomeDashboardScreen(
                        profile = profile,
                        matches = matches,
                        unreadNotificationCount = unreadCount,
                        onSelectSport = { sport ->
                            viewModel.selectSport(sport)
                            viewModel.selectTournamentFilter(null)
                            switchTab(BottomNavTab.SPORTS)
                        },
                        onSelectTournament = { tourney ->
                            viewModel.selectSport(tourney.sport)
                            viewModel.selectTournamentFilter(tourney.name)
                            switchTab(BottomNavTab.SPORTS)
                        },
                        onOpenMatchDetails = { id -> navigateTo(BetZoneRoute.MatchDetails(id)) },
                        onOpenLiveCentre = { id -> navigateTo(BetZoneRoute.LiveCentre(id)) },
                        onOpenPredictionCentre = { id -> navigateTo(BetZoneRoute.Predictions(id)) },
                        onOpenNotifications = { navigateTo(BetZoneRoute.Notifications) },
                        onOpenProfile = { switchTab(BottomNavTab.PROFILE) },
                        onOpenRewards = { navigateTo(BetZoneRoute.Rewards) }
                    )

                    BetZoneRoute.Sports -> SportsCategoryScreen(
                        matches = matches,
                        selectedSport = selectedSport,
                        selectedStatus = selectedStatus,
                        selectedTournament = selectedTournament,
                        searchQuery = searchQuery,
                        favoriteTeams = profile?.favoriteTeams ?: emptyList(),
                        onSelectSport = viewModel::selectSport,
                        onSelectStatus = viewModel::selectStatusFilter,
                        onSelectTournament = viewModel::selectTournamentFilter,
                        onSearchChange = viewModel::updateSearchQuery,
                        onToggleFavoriteTeam = { team ->
                            viewModel.toggleFavoriteTeam(team, profile?.favoriteTeams ?: emptyList())
                        },
                        onOpenMatchDetails = { id -> navigateTo(BetZoneRoute.MatchDetails(id)) },
                        onOpenLiveCentre = { id -> navigateTo(BetZoneRoute.LiveCentre(id)) }
                    )

                    is BetZoneRoute.Predictions -> PredictionsHubScreen(
                        profile = profile,
                        matches = matches,
                        predictions = predictions,
                        initialMatchId = route.matchId,
                        isBusy = isBusy,
                        onPlacePrediction = { m, mk, opt, stake ->
                            viewModel.placePrediction(m, mk, opt, stake)
                        },
                        onSettlePrediction = viewModel::resolvePrediction
                    )

                    BetZoneRoute.Leaderboard -> LeaderboardScreen(
                        currentUid = uid,
                        entries = leaderboard,
                        selectedPeriod = leaderboardPeriod,
                        onSelectPeriod = viewModel::selectLeaderboardPeriod,
                        onOpenRewards = { navigateTo(BetZoneRoute.Rewards) }
                    )

                    BetZoneRoute.Profile -> UserProfileScreen(
                        uid = uid,
                        profile = profile,
                        onToggleFavoriteSport = { sport ->
                            viewModel.toggleFavoriteSport(sport, profile?.favoriteSports ?: emptyList())
                        },
                        onToggleFavoriteTeam = { team ->
                            viewModel.toggleFavoriteTeam(team, profile?.favoriteTeams ?: emptyList())
                        },
                        onOpenSettings = { navigateTo(BetZoneRoute.Settings) },
                        onOpenRewards = { navigateTo(BetZoneRoute.Rewards) }
                    )

                    is BetZoneRoute.MatchDetails -> MatchDetailsScreen(
                        match = viewModel.findMatchById(route.matchId),
                        onBack = { popBack() },
                        onOpenLiveCentre = { navigateTo(BetZoneRoute.LiveCentre(route.matchId)) },
                        onOpenPredict = { navigateTo(BetZoneRoute.Predictions(route.matchId)) }
                    )

                    is BetZoneRoute.LiveCentre -> LiveMatchCentreScreen(
                        match = viewModel.findMatchById(route.matchId),
                        onBack = { popBack() },
                        onOpenPredict = { navigateTo(BetZoneRoute.Predictions(route.matchId)) }
                    )

                    BetZoneRoute.Rewards -> RewardsScreen(
                        profile = profile,
                        claims = claims,
                        todayKey = viewModel.todayKey(),
                        isBusy = isBusy,
                        onBack = { popBack() },
                        onClaimDaily = viewModel::claimDailyReward,
                        onClaimChallenge = viewModel::claimChallenge,
                        onClaimQuiz = viewModel::claimQuizSuccess
                    )

                    BetZoneRoute.Notifications -> NotificationsScreen(
                        notifications = notifications,
                        onBack = { popBack() },
                        onMarkRead = viewModel::markNotificationRead,
                        onMarkAllRead = { viewModel.markAllNotificationsRead(notifications) },
                        onOpenMatch = { id -> navigateTo(BetZoneRoute.MatchDetails(id)) }
                    )

                    BetZoneRoute.Settings -> SettingsScreen(
                        profile = profile,
                        darkMode = darkModePref,
                        language = profile?.language ?: languagePref,
                        onBack = { popBack() },
                        onUpdateProfile = viewModel::updateProfile,
                        onChangeLanguage = viewModel::setLanguage,
                        onToggleDarkMode = viewModel::setDarkMode,
                        onToggleNotifications = viewModel::setNotificationsEnabled,
                        onTogglePublicLeaderboard = viewModel::setPublicLeaderboard,
                        onLogout = onLogout
                    )
                }

                if (currentBottomTab != null) {
                    BetZoneFloatingBottomBar(
                        currentTab = currentBottomTab,
                        onSelectTab = ::switchTab,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
