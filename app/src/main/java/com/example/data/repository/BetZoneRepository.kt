package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MarketOption
import com.example.data.model.MatchItem
import com.example.data.model.NotificationCategory
import com.example.data.model.PredictionMarket
import com.example.data.model.PredictionStatus
import com.example.data.model.RewardClaim
import com.example.data.model.RewardType
import com.example.data.model.UserNotification
import com.example.data.model.UserPrediction
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyChallenge
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToLong

class BetZoneRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        require(!uid.isNullOrBlank()) { "User must be signed in with Google before accessing Firestore." }
        return uid
    }

    private fun sanitizeName(raw: String): String {
        val trimmed = raw.trim().take(40)
        return if (trimmed.length >= 2) trimmed else "777Bet Player"
    }

    private fun <T> Flow<T>.handleFirestoreStream(uid: String, operation: OperationType, path: String): Flow<T> =
        this.catch { error ->
            val firestoreError = generateSequence<Throwable>(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exToLog = firestoreError ?: (error as? Exception)
            if (exToLog != null && auth.currentUser?.uid == uid) {
                handleFirestoreError(exToLog, operation, path)
            }
            throw (firestoreError ?: error)
        }

    suspend fun ensureUserInitialized(
        displayNameHint: String,
        countryCode: String,
        phoneNumber: String,
        darkModeDefault: Boolean
    ): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val snap = userRef.get().await()
            if (!snap.exists()) {
                val safeName = sanitizeName(displayNameHint)
                val cleanPhone = phoneNumber.trim().take(20)
                val cleanCountry = countryCode.trim().take(8).ifBlank { "+91" }
                val profilePayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "avatarKey" to "cobalt_falcon",
                    "phoneNumber" to cleanPhone,
                    "countryCode" to cleanCountry,
                    "virtualPoints" to 2500L,
                    "totalPredictions" to 0L,
                    "correctPredictions" to 0L,
                    "streakDays" to 1L,
                    "favoriteSports" to listOf("Cricket", "Football"),
                    "favoriteTeams" to listOf("Mumbai Titans", "Real Madrid"),
                    "badges" to listOf("ROOKIE_PREDICTOR"),
                    "language" to "English",
                    "darkMode" to darkModeDefault,
                    "notificationsEnabled" to true,
                    "privacyPublicLeaderboard" to true,
                    "termsAccepted" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val leaderboardPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "avatarKey" to "cobalt_falcon",
                    "virtualPoints" to 2500L,
                    "totalPredictions" to 0L,
                    "correctPredictions" to 0L,
                    "accuracyPct" to 0L,
                    "badges" to listOf("ROOKIE_PREDICTOR"),
                    "periodBucket" to "ALL",
                    "publicVisible" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val welcomeNotif1 = mapOf(
                    "userId" to uid,
                    "title" to "Mumbai Titans vs Chennai Kings is LIVE!",
                    "body" to "Your favourite team is chasing 202 in Wankhede. Open Live Match Centre now.",
                    "category" to NotificationCategory.MATCH_LIVE.name,
                    "matchId" to "ipl_mi_csk_01",
                    "unread" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val welcomeNotif2 = mapOf(
                    "userId" to uid,
                    "title" to "Welcome Bonus Unlocked: 2,500 VP",
                    "body" to "Use your Virtual Points across IPL, UCL, NBA, and Sports Quizzes!",
                    "category" to NotificationCategory.ACHIEVEMENT.name,
                    "matchId" to "",
                    "unread" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val batch = db.batch()
                batch.set(userRef, profilePayload)
                batch.set(db.collection("leaderboard_entries").document(uid), leaderboardPayload)
                batch.set(userRef.collection("notifications").document("welcome_live"), welcomeNotif1)
                batch.set(userRef.collection("notifications").document("welcome_bonus"), welcomeNotif2)
                batch.commit().await()
            } else if (phoneNumber.isNotBlank()) {
                userRef.update(
                    mapOf(
                        "countryCode" to countryCode.trim().take(8).ifBlank { "+91" },
                        "phoneNumber" to phoneNumber.trim().take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap -> snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStream(uid, OperationType.GET, path)
        )
    }

    fun observePredictions(): Flow<List<UserPrediction>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/predictions"
        emitAll(
            db.collection("users").document(uid).collection("predictions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(UserPrediction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStream(uid, OperationType.LIST, path)
        )
    }

    fun observeRewardClaims(): Flow<List<RewardClaim>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/rewards_claims"
        emitAll(
            db.collection("users").document(uid).collection("rewards_claims")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(RewardClaim::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStream(uid, OperationType.LIST, path)
        )
    }

    fun observeNotifications(): Flow<List<UserNotification>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(UserNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStream(uid, OperationType.LIST, path)
        )
    }

    fun observePublicLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val uid = requireUserId()
        val path = "leaderboard_entries"
        emitAll(
            db.collection("leaderboard_entries")
                .whereEqualTo("publicVisible", true)
                .snapshots()
                .map { snap -> snap.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleFirestoreStream(uid, OperationType.LIST, path)
        )
    }

    suspend fun placePrediction(
        match: MatchItem,
        market: PredictionMarket,
        option: MarketOption,
        stakePoints: Long
    ): Result<UserPrediction> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/predictions"
        return try {
            val activeUid = requireUserId()
            require(stakePoints in 10L..50000L) { "Virtual points stake must be between 10 and 50,000 VP." }
            val potential = ((stakePoints * option.multiplierX100.toDouble()) / 100.0).roundToLong().coerceAtLeast(11L)
            val userRef = db.collection("users").document(activeUid)
            val predRef = userRef.collection("predictions").document()
            val lbRef = db.collection("leaderboard_entries").document(activeUid)
            val notifRef = userRef.collection("notifications").document()

            val created = db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getLong("virtualPoints") ?: 0L
                require(currentBalance >= stakePoints) { "Insufficient Virtual Points balance." }
                val totalPreds = (userSnap.getLong("totalPredictions") ?: 0L) + 1L
                val correctPreds = userSnap.getLong("correctPredictions") ?: 0L
                val newBalance = currentBalance - stakePoints
                val existingBadges = (userSnap.get("badges") as? List<*>)?.filterIsInstance<String>() ?: listOf("ROOKIE_PREDICTOR")
                val updatedBadges = if (totalPreds >= 3L && !existingBadges.contains("HAT_TRICK")) {
                    (existingBadges + "HAT_TRICK").take(12)
                } else {
                    existingBadges.take(12)
                }

                val predPayload = mapOf(
                    "userId" to activeUid,
                    "matchId" to match.id.take(64),
                    "matchTitle" to match.matchTitle.take(100),
                    "sport" to match.sport.label.take(30),
                    "tournament" to match.tournament.take(60),
                    "marketType" to market.title.take(40),
                    "selectedPick" to option.label.take(60),
                    "stakePoints" to stakePoints,
                    "multiplierX100" to option.multiplierX100.toLong(),
                    "potentialRewardPoints" to potential,
                    "status" to PredictionStatus.ACTIVE.name,
                    "resultSummary" to "Awaiting match outcome",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                tx.set(predRef, predPayload)
                tx.update(
                    userRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "totalPredictions" to totalPreds,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val accuracy = if (totalPreds <= 0L) 0L else ((correctPreds * 100L) / totalPreds).coerceIn(0L, 100L)
                tx.update(
                    lbRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "totalPredictions" to totalPreds,
                        "accuracyPct" to accuracy,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    notifRef,
                    mapOf(
                        "userId" to activeUid,
                        "title" to "Prediction Locked: ${option.label}",
                        "body" to "${stakePoints} VP placed on ${match.matchTitle} (${market.title}).",
                        "category" to NotificationCategory.CHALLENGE.name,
                        "matchId" to match.id.take(64),
                        "unread" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                UserPrediction(
                    id = predRef.id,
                    userId = activeUid,
                    matchId = match.id,
                    matchTitle = match.matchTitle,
                    sport = match.sport.label,
                    tournament = match.tournament,
                    marketType = market.title,
                    selectedPick = option.label,
                    stakePoints = stakePoints,
                    multiplierX100 = option.multiplierX100.toLong(),
                    potentialRewardPoints = potential,
                    status = PredictionStatus.ACTIVE.name
                )
            }.await()

            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun settlePrediction(prediction: UserPrediction, won: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/predictions/${prediction.id}"
        return try {
            val activeUid = requireUserId()
            require(prediction.id.isNotBlank()) { "Prediction ID cannot be blank." }
            val userRef = db.collection("users").document(activeUid)
            val predRef = userRef.collection("predictions").document(prediction.id)
            val lbRef = db.collection("leaderboard_entries").document(activeUid)
            val notifRef = userRef.collection("notifications").document()

            db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val predSnap = tx.get(predRef)
                val currentStatus = predSnap.getString("status") ?: PredictionStatus.ACTIVE.name
                require(currentStatus == PredictionStatus.ACTIVE.name) { "Prediction already settled." }

                val currentBalance = userSnap.getLong("virtualPoints") ?: 0L
                val totalPreds = userSnap.getLong("totalPredictions") ?: 1L
                val currentCorrect = userSnap.getLong("correctPredictions") ?: 0L
                val newCorrect = if (won) currentCorrect + 1L else currentCorrect
                val newBalance = if (won) currentBalance + prediction.potentialRewardPoints else currentBalance
                val existingBadges = (userSnap.get("badges") as? List<*>)?.filterIsInstance<String>() ?: listOf("ROOKIE_PREDICTOR")
                val updatedBadges = if (won && !existingBadges.contains("FIRST_WIN")) {
                    (existingBadges + "FIRST_WIN").take(12)
                } else {
                    existingBadges.take(12)
                }

                val summary = if (won) {
                    "Call matched! +${prediction.potentialRewardPoints} VP credited."
                } else {
                    "Result went the other way on ${prediction.selectedPick}."
                }
                tx.update(
                    predRef,
                    mapOf(
                        "status" to if (won) PredictionStatus.WON.name else PredictionStatus.LOST.name,
                        "resultSummary" to summary.take(140),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val accuracy = if (totalPreds <= 0L) 0L else ((newCorrect * 100L) / totalPreds).coerceIn(0L, 100L)
                tx.update(
                    userRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "correctPredictions" to newCorrect,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.update(
                    lbRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "correctPredictions" to newCorrect,
                        "accuracyPct" to accuracy,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    notifRef,
                    mapOf(
                        "userId" to activeUid,
                        "title" to if (won) "Prediction Won: +${prediction.potentialRewardPoints} VP!" else "Match Result Updated",
                        "body" to "${prediction.matchTitle}: $summary".take(220),
                        "category" to NotificationCategory.RESULT.name,
                        "matchId" to prediction.matchId.take(64),
                        "unread" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun claimReward(
        rewardType: RewardType,
        rewardKey: String,
        title: String,
        pointsAwarded: Long,
        incrementStreak: Boolean = false,
        grantBadge: String? = null
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/rewards_claims/$rewardKey"
        return try {
            val activeUid = requireUserId()
            require(rewardKey.matches(Regex("^[a-zA-Z0-9_\\-]{2,64}$"))) { "Invalid reward key." }
            require(pointsAwarded in 10L..5000L) { "Reward points out of bounds." }

            val userRef = db.collection("users").document(activeUid)
            val claimRef = userRef.collection("rewards_claims").document(rewardKey)
            val lbRef = db.collection("leaderboard_entries").document(activeUid)

            db.runTransaction { tx ->
                if (tx.get(claimRef).exists()) {
                    throw IllegalStateException("This reward has already been claimed.")
                }
                val userSnap = tx.get(userRef)
                val newBalance = (userSnap.getLong("virtualPoints") ?: 0L) + pointsAwarded
                val currentStreak = userSnap.getLong("streakDays") ?: 1L
                val newStreak = if (incrementStreak) currentStreak + 1L else currentStreak
                val existingBadges = (userSnap.get("badges") as? List<*>)?.filterIsInstance<String>() ?: listOf("ROOKIE_PREDICTOR")
                val updatedBadges = if (grantBadge != null && !existingBadges.contains(grantBadge)) {
                    (existingBadges + grantBadge).take(12)
                } else {
                    existingBadges.take(12)
                }

                tx.set(
                    claimRef,
                    mapOf(
                        "userId" to activeUid,
                        "rewardType" to rewardType.name,
                        "rewardKey" to rewardKey,
                        "title" to title.take(80),
                        "pointsAwarded" to pointsAwarded,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.update(
                    userRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "streakDays" to newStreak,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.update(
                    lbRef,
                    mapOf(
                        "virtualPoints" to newBalance,
                        "badges" to updatedBadges,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavoriteSport(sportLabel: String, currentList: List<String>): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}"
        return try {
            val activeUid = requireUserId()
            val updated = if (currentList.contains(sportLabel)) {
                (currentList - sportLabel).take(7)
            } else {
                (currentList + sportLabel).take(7)
            }
            db.collection("users").document(activeUid).update(
                mapOf("favoriteSports" to updated, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavoriteTeam(teamName: String, currentList: List<String>): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}"
        return try {
            val activeUid = requireUserId()
            val updated = if (currentList.contains(teamName)) {
                (currentList - teamName).take(20)
            } else {
                (currentList + teamName).take(20)
            }
            db.collection("users").document(activeUid).update(
                mapOf("favoriteTeams" to updated, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileAndSettings(
        displayName: String? = null,
        avatarKey: String? = null,
        language: String? = null,
        darkMode: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        privacyPublicLeaderboard: Boolean? = null
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}"
        return try {
            val activeUid = requireUserId()
            val userUpdates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            val lbUpdates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())

            displayName?.let {
                val clean = sanitizeName(it)
                userUpdates["displayName"] = clean
                lbUpdates["displayName"] = clean
            }
            avatarKey?.let {
                val clean = it.trim().take(40).ifBlank { "cobalt_falcon" }
                userUpdates["avatarKey"] = clean
                lbUpdates["avatarKey"] = clean
            }
            language?.let { userUpdates["language"] = it.trim().take(20).ifBlank { "English" } }
            darkMode?.let { userUpdates["darkMode"] = it }
            notificationsEnabled?.let { userUpdates["notificationsEnabled"] = it }
            privacyPublicLeaderboard?.let {
                userUpdates["privacyPublicLeaderboard"] = it
                lbUpdates["publicVisible"] = it
            }

            val batch = db.batch()
            batch.update(db.collection("users").document(activeUid), userUpdates)
            if (lbUpdates.size > 1) {
                batch.update(db.collection("leaderboard_entries").document(activeUid), lbUpdates)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/notifications/$notificationId"
        return try {
            val activeUid = requireUserId()
            db.collection("users").document(activeUid)
                .collection("notifications").document(notificationId)
                .update(mapOf("unread" to false, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(notifications: List<UserNotification>): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "users/${uid ?: "unauthenticated"}/notifications"
        return try {
            val activeUid = requireUserId()
            val unreadItems = notifications.filter { it.unread && it.id.isNotBlank() }
            if (unreadItems.isEmpty()) return Result.success(Unit)
            val batch = db.batch()
            unreadItems.forEach { notif ->
                val ref = db.collection("users").document(activeUid)
                    .collection("notifications").document(notif.id)
                batch.update(ref, mapOf("unread" to false, "updatedAt" to FieldValue.serverTimestamp()))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
