package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SportCategory(val label: String, val iconKey: String) {
    CRICKET("Cricket", "cricket"),
    FOOTBALL("Football", "football"),
    TENNIS("Tennis", "tennis"),
    BASKETBALL("Basketball", "basketball"),
    VOLLEYBALL("Volleyball", "volleyball"),
    HOCKEY("Hockey", "hockey"),
    ESPORTS("Esports", "esports")
}

enum class MatchStatus(val label: String) {
    LIVE("LIVE"),
    UPCOMING("UPCOMING"),
    COMPLETED("COMPLETED")
}

enum class PredictionStatus {
    ACTIVE,
    WON,
    LOST;

    companion object {
        fun fromRaw(raw: String?): PredictionStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

enum class RewardType {
    DAILY_LOGIN,
    WEEKLY_CHALLENGE,
    SPORTS_QUIZ,
    BADGE_BONUS
}

enum class NotificationCategory {
    MATCH_LIVE,
    TEAM_ALERT,
    RESULT,
    CHALLENGE,
    ACHIEVEMENT;

    companion object {
        fun fromRaw(raw: String?): NotificationCategory =
            entries.firstOrNull { it.name == raw } ?: MATCH_LIVE
    }
}

enum class LeaderboardPeriod(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class TeamSummary(
    val name: String,
    val shortCode: String,
    val score: String,
    val subScore: String,
    val primaryHex: Long,
    val secondaryHex: Long
)

data class MatchStatRow(
    val label: String,
    val homeValue: String,
    val awayValue: String,
    val homeRatio: Float
)

data class CommentaryItem(
    val stamp: String,
    val headline: String,
    val detail: String,
    val highlight: Boolean = false
)

data class LineupPlayer(
    val name: String,
    val role: String,
    val teamSide: String,
    val rating: String,
    val statHighlight: String
)

data class HeadToHeadItem(
    val dateLabel: String,
    val scoreLine: String,
    val winnerName: String
)

data class MarketOption(
    val id: String,
    val label: String,
    val multiplierX100: Int
)

data class PredictionMarket(
    val id: String,
    val title: String,
    val options: List<MarketOption>
)

data class MatchItem(
    val id: String,
    val sport: SportCategory,
    val tournament: String,
    val status: MatchStatus,
    val venue: String,
    val timerLabel: String,
    val countdownSeconds: Long,
    val momentumHome: Float,
    val summaryNote: String,
    val homeTeam: TeamSummary,
    val awayTeam: TeamSummary,
    val stats: List<MatchStatRow>,
    val commentary: List<CommentaryItem>,
    val lineups: List<LineupPlayer>,
    val headToHead: List<HeadToHeadItem>,
    val markets: List<PredictionMarket>
) {
    val matchTitle: String
        get() = "${homeTeam.name} vs ${awayTeam.name}"
}

data class TournamentInfo(
    val id: String,
    val name: String,
    val sport: SportCategory,
    val badgeCode: String,
    val activeMatches: Int,
    val accentHex: Long
)

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val rewardPoints: Int
)

data class BadgeInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val requiredPredictions: Int
)

data class QuizQuestion(
    val id: String,
    val sport: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val bonusPoints: Int
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Sports Strategist",
    val avatarKey: String = "cobalt_falcon",
    val phoneNumber: String = "",
    val countryCode: String = "+91",
    val virtualPoints: Long = 2500L,
    val totalPredictions: Long = 0L,
    val correctPredictions: Long = 0L,
    val streakDays: Long = 1L,
    val favoriteSports: List<String> = listOf("Cricket", "Football"),
    val favoriteTeams: List<String> = listOf("Mumbai Titans", "Real Madrid"),
    val badges: List<String> = listOf("ROOKIE_PREDICTOR"),
    val language: String = "English",
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val privacyPublicLeaderboard: Boolean = true,
    val termsAccepted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val accuracyPct: Int
        get() = if (totalPredictions <= 0L) 0 else ((correctPredictions * 100L) / totalPredictions).toInt().coerceIn(0, 100)
}

@Keep
@IgnoreExtraProperties
data class UserPrediction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val matchTitle: String = "",
    val sport: String = "",
    val tournament: String = "",
    val marketType: String = "",
    val selectedPick: String = "",
    val stakePoints: Long = 0L,
    val multiplierX100: Long = 180L,
    val potentialRewardPoints: Long = 0L,
    val status: String = PredictionStatus.ACTIVE.name,
    val resultSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedStatus: PredictionStatus
        get() = PredictionStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class RewardClaim(
    @DocumentId val id: String = "",
    val userId: String = "",
    val rewardType: String = RewardType.DAILY_LOGIN.name,
    val rewardKey: String = "",
    val title: String = "",
    val pointsAwarded: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = NotificationCategory.MATCH_LIVE.name,
    val matchId: String = "",
    val unread: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedCategory: NotificationCategory
        get() = NotificationCategory.fromRaw(category)
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val avatarKey: String = "cobalt_falcon",
    val virtualPoints: Long = 2500L,
    val totalPredictions: Long = 0L,
    val correctPredictions: Long = 0L,
    val accuracyPct: Long = 0L,
    val badges: List<String> = emptyList(),
    val periodBucket: String = "ALL",
    val publicVisible: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
