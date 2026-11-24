package com.example.ui.viewmodel

import android.content.Context
import com.example.data.repository.BetZoneRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.mock.MockSportsData
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.MarketOption
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatus
import com.example.data.model.PredictionMarket
import com.example.data.model.RewardClaim
import com.example.data.model.RewardType
import com.example.data.model.SportCategory
import com.example.data.model.UiState
import com.example.data.model.UserNotification
import com.example.data.model.UserPrediction
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyChallenge
import com.example.data.preferences.BetZonePreferences
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BetZoneViewModel(
    private val repository: BetZoneRepository,
    private val preferences: BetZonePreferences,
    displayNameHint: String,
    countryCodeHint: String,
    phoneHint: String,
    darkModeDefault: Boolean
) : ViewModel() {

    private val _matches = MutableStateFlow(MockSportsData.initialMatches)
    val matches: StateFlow<List<MatchItem>> = _matches.asStateFlow()

    private val _selectedSport = MutableStateFlow<SportCategory?>(null)
    val selectedSport: StateFlow<SportCategory?> = _selectedSport.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<MatchStatus?>(null)
    val selectedStatusFilter: StateFlow<MatchStatus?> = _selectedStatusFilter.asStateFlow()

    private val _selectedTournamentFilter = MutableStateFlow<String?>(null)
    val selectedTournamentFilter: StateFlow<String?> = _selectedTournamentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _leaderboardPeriod = MutableStateFlow(LeaderboardPeriod.DAILY)
    val leaderboardPeriod: StateFlow<LeaderboardPeriod> = _leaderboardPeriod.asStateFlow()

    private val _feedbackBanner = MutableStateFlow<String?>(null)
    val feedbackBanner: StateFlow<String?> = _feedbackBanner.asStateFlow()

    private val _isActionBusy = MutableStateFlow(false)
    val isActionBusy: StateFlow<Boolean> = _isActionBusy.asStateFlow()

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile()
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error -> emit(resolveErrorState(error, "Failed to load profile")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val predictionsState: StateFlow<UiState<List<UserPrediction>>> = repository.observePredictions()
        .map<List<UserPrediction>, UiState<List<UserPrediction>>> { UiState.Success(it) }
        .catch { error -> emit(resolveErrorState(error, "Failed to load predictions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val rewardClaimsState: StateFlow<UiState<List<RewardClaim>>> = repository.observeRewardClaims()
        .map<List<RewardClaim>, UiState<List<RewardClaim>>> { UiState.Success(it) }
        .catch { error -> emit(resolveErrorState(error, "Failed to load reward claims")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val notificationsState: StateFlow<UiState<List<UserNotification>>> = repository.observeNotifications()
        .map<List<UserNotification>, UiState<List<UserNotification>>> { UiState.Success(it) }
        .catch { error -> emit(resolveErrorState(error, "Failed to load notifications")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> = repository.observePublicLeaderboard()
        .map<List<LeaderboardEntry>, UiState<List<LeaderboardEntry>>> { remoteEntries ->
            val merged = (remoteEntries + MockSportsData.seededRivals())
                .distinctBy { it.userId }
                .sortedByDescending { it.virtualPoints }
            UiState.Success(merged)
        }
        .catch { error -> emit(resolveErrorState(error, "Failed to load leaderboard")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        viewModelScope.launch {
            repository.ensureUserInitialized(
                displayNameHint = displayNameHint,
                countryCode = countryCodeHint,
                phoneNumber = phoneHint,
                darkModeDefault = darkModeDefault
            ).onFailure {
                _feedbackBanner.value = it.localizedMessage ?: "Cloud sync unavailable."
            }
        }
        viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(1000L)
                tick += 1
                _matches.value = MockSportsData.advanceLiveTick(_matches.value, tick)
            }
        }
    }

    private fun <T> resolveErrorState(error: Throwable, fallback: String): UiState<T> {
        val firestoreError = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        return UiState.Error(rootError.message ?: fallback)
    }

    fun selectSport(sport: SportCategory?) {
        _selectedSport.value = sport
    }

    fun selectStatusFilter(status: MatchStatus?) {
        _selectedStatusFilter.value = status
    }

    fun selectTournamentFilter(tournament: String?) {
        _selectedTournamentFilter.value = tournament
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLeaderboardPeriod(period: LeaderboardPeriod) {
        _leaderboardPeriod.value = period
    }

    fun dismissFeedback() {
        _feedbackBanner.value = null
    }

    fun findMatchById(matchId: String): MatchItem =
        _matches.value.firstOrNull { it.id == matchId } ?: _matches.value.first()

    fun placePrediction(
        match: MatchItem,
        market: PredictionMarket,
        option: MarketOption,
        stakePoints: Long,
        onPlaced: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.placePrediction(match, market, option, stakePoints)
                .onSuccess {
                    _feedbackBanner.value = "Prediction locked! ${stakePoints} VP on ${option.label}."
                    onPlaced()
                }
                .onFailure {
                    _feedbackBanner.value = it.localizedMessage ?: "Could not submit prediction."
                }
            _isActionBusy.value = false
        }
    }

    fun resolvePrediction(prediction: UserPrediction, won: Boolean) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.settlePrediction(prediction, won)
                .onSuccess {
                    _feedbackBanner.value = if (won) {
                        "Prediction Won! +${prediction.potentialRewardPoints} VP added."
                    } else {
                        "Prediction settled as completed."
                    }
                }
                .onFailure {
                    _feedbackBanner.value = it.localizedMessage ?: "Settlement failed."
                }
            _isActionBusy.value = false
        }
    }

    fun todayKey(): String =
        SimpleDateFormat("yyyy_MM_dd", Locale.US).format(Date())

    fun claimDailyReward() {
        val key = "daily_${todayKey()}"
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.claimReward(
                rewardType = RewardType.DAILY_LOGIN,
                rewardKey = key,
                title = "Daily Login Bonus",
                pointsAwarded = 250L,
                incrementStreak = true
            ).onSuccess {
                preferences.setLastDailyClaimDate(key)
                _feedbackBanner.value = "Daily Reward Claimed: +250 VP!"
            }.onFailure {
                _feedbackBanner.value = it.localizedMessage ?: "Daily reward already claimed."
            }
            _isActionBusy.value = false
        }
    }

    fun claimChallenge(challenge: WeeklyChallenge) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.claimReward(
                rewardType = RewardType.WEEKLY_CHALLENGE,
                rewardKey = challenge.id,
                title = challenge.title,
                pointsAwarded = challenge.rewardPoints.toLong()
            ).onSuccess {
                _feedbackBanner.value = "Challenge Reward Claimed: +${challenge.rewardPoints} VP!"
            }.onFailure {
                _feedbackBanner.value = it.localizedMessage ?: "Unable to claim challenge."
            }
            _isActionBusy.value = false
        }
    }

    fun claimQuizSuccess(quizId: String, title: String, bonusPoints: Int) {
        viewModelScope.launch {
            _isActionBusy.value = true
            repository.claimReward(
                rewardType = RewardType.SPORTS_QUIZ,
                rewardKey = quizId,
                title = title,
                pointsAwarded = bonusPoints.toLong(),
                grantBadge = "QUIZ_MASTER"
            ).onSuccess {
                _feedbackBanner.value = "Correct Answer! +$bonusPoints VP & Sports Scholar Badge!"
            }.onFailure {
                _feedbackBanner.value = it.localizedMessage ?: "Quiz already completed."
            }
            _isActionBusy.value = false
        }
    }

    fun toggleFavoriteSport(sportLabel: String, currentList: List<String>) {
        viewModelScope.launch {
            repository.toggleFavoriteSport(sportLabel, currentList)
        }
    }

    fun toggleFavoriteTeam(teamName: String, currentList: List<String>) {
        viewModelScope.launch {
            repository.toggleFavoriteTeam(teamName, currentList)
        }
    }

    fun updateProfile(displayName: String, avatarKey: String) {
        viewModelScope.launch {
            repository.updateProfileAndSettings(displayName = displayName, avatarKey = avatarKey)
                .onSuccess { _feedbackBanner.value = "Profile updated." }
                .onFailure { _feedbackBanner.value = it.localizedMessage ?: "Update failed." }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
            repository.updateProfileAndSettings(darkMode = enabled)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
            repository.updateProfileAndSettings(language = language)
                .onSuccess { _feedbackBanner.value = "Language set to $language." }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateProfileAndSettings(notificationsEnabled = enabled)
        }
    }

    fun setPublicLeaderboard(visible: Boolean) {
        viewModelScope.launch {
            repository.updateProfileAndSettings(privacyPublicLeaderboard = visible)
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun markAllNotificationsRead(notifications: List<UserNotification>) {
        viewModelScope.launch {
            repository.markAllNotificationsRead(notifications)
        }
    }
}

class BetZoneViewModelFactory(
    private val context: Context,
    private val preferences: BetZonePreferences,
    private val displayNameHint: String,
    private val countryCodeHint: String,
    private val phoneHint: String,
    private val darkModeDefault: Boolean
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BetZoneViewModel(
            repository = BetZoneRepository(context),
            preferences = preferences,
            displayNameHint = displayNameHint,
            countryCodeHint = countryCodeHint,
            phoneHint = phoneHint,
            darkModeDefault = darkModeDefault
        ) as T
    }
}
