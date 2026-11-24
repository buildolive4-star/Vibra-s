package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.ConsentedRecording
import com.example.data.model.GiftTransaction
import com.example.data.model.HostProfile
import com.example.data.model.NetworkQuality
import com.example.data.model.VirtualGift
import com.example.data.model.WalletTxType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max

class VibraCallAndChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google.")

    fun observeCallsForUser(userId: String): Flow<List<CallSession>> =
        db.collection("calls")
            .whereArrayContains("participantUids", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(CallSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "calls")
                }
                throw error
            }

    fun observeCall(callId: String): Flow<CallSession?> =
        db.collection("calls").document(callId)
            .snapshots()
            .map { snap -> snap.toObject(CallSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.GET, "calls/$callId")
                throw error
            }

    suspend fun startCallSession(
        callerName: String,
        host: HostProfile,
        callType: CallType,
        pricing: AppPricingSettings
    ): Result<CallSession> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val baseRate = if (callType == CallType.AUDIO) {
            if (host.audioRatePerMin > 0) host.audioRatePerMin else pricing.audioRatePerMin
        } else {
            if (host.videoRatePerMin > 0) host.videoRatePerMin else pricing.videoRatePerMin
        }
        val effectiveRate = max(1L, baseRate - (baseRate * pricing.promoDiscountPct / 100L))
        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val callRef = db.collection("calls").document(callId)
        val walletRef = db.collection("wallets").document(uid)

        return try {
            val walletSnap = walletRef.get().await()
            val balance = walletSnap.getLong("balanceCoins") ?: 0L
            if (balance < effectiveRate) {
                return Result.failure(
                    IllegalStateException("Insufficient balance ($balance coins). Need at least $effectiveRate coins/min to start ${callType.name.lowercase()} call.")
                )
            }
            val participants = listOf(uid, host.ownerUid).distinct()
            callRef.set(
                mapOf(
                    "callerId" to uid,
                    "callerName" to callerName.trim().ifBlank { "Caller" }.take(60),
                    "hostId" to host.id,
                    "hostOwnerUid" to host.ownerUid,
                    "hostName" to host.name,
                    "callType" to callType.name,
                    "status" to CallStatus.ACTIVE.name,
                    "ratePerMin" to effectiveRate,
                    "durationSeconds" to 0L,
                    "billedCoins" to 0L,
                    "hostEarnedCoins" to 0L,
                    "platformCommissionCoins" to 0L,
                    "networkQuality" to NetworkQuality.EXCELLENT.name,
                    "callerConsentRecording" to false,
                    "hostConsentRecording" to false,
                    "recordingActive" to false,
                    "participantUids" to participants,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(
                CallSession(
                    id = callId,
                    callerId = uid,
                    callerName = callerName,
                    hostId = host.id,
                    hostOwnerUid = host.ownerUid,
                    hostName = host.name,
                    callType = callType.name,
                    status = CallStatus.ACTIVE.name,
                    ratePerMin = effectiveRate,
                    participantUids = participants
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, callRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateRecordingConsent(
        call: CallSession,
        callerConsented: Boolean,
        hostConsented: Boolean
    ): Result<Unit> {
        val path = "calls/${call.id}"
        return try {
            requireUserId()
            val bothConsented = callerConsented && hostConsented
            db.document(path).update(
                mapOf(
                    "callerConsentRecording" to callerConsented,
                    "hostConsentRecording" to hostConsented,
                    "recordingActive" to bothConsented,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (bothConsented) {
                val recId = "rec_${call.id}"
                db.collection("consented_recordings").document(recId).set(
                    mapOf(
                        "callId" to call.id,
                        "callerId" to call.callerId,
                        "hostId" to call.hostId,
                        "hostOwnerUid" to call.hostOwnerUid,
                        "callerConsented" to true,
                        "hostConsented" to true,
                        "durationSeconds" to call.durationSeconds,
                        "retentionDays" to 30L,
                        "accessLogCount" to 0L,
                        "status" to "ACTIVE",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun settleAndEndCall(
        call: CallSession,
        elapsedSeconds: Long,
        pricing: AppPricingSettings,
        finalStatus: CallStatus = CallStatus.COMPLETED
    ): Result<CallSession> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val callRef = db.collection("calls").document(call.id)
        val walletRef = db.collection("wallets").document(call.callerId)
        val hostRef = db.collection("hosts").document(call.hostId)
        val txRef = db.collection("wallet_transactions").document("tx_${call.id}")
        val earningRef = db.collection("host_earnings").document("earn_${call.id}")

        return try {
            val billableSeconds = if (finalStatus == CallStatus.COMPLETED && elapsedSeconds > 0L) {
                max(elapsedSeconds, pricing.minBillingSeconds)
            } else {
                0L
            }
            val interval = max(1L, pricing.billingIntervalSeconds)
            val intervals = if (billableSeconds == 0L) 0L else ceil(billableSeconds.toDouble() / interval.toDouble()).toLong()
            val coinsPerInterval = max(1L, (call.ratePerMin * interval) / 60L)
            val calculatedBilled = intervals * coinsPerInterval

            var actualBilled = 0L
            var hostEarned = 0L
            var commission = 0L

            db.runTransaction { transaction ->
                val callSnap = transaction.get(callRef)
                val existingStatus = callSnap.getString("status")
                if (existingStatus == CallStatus.COMPLETED.name || existingStatus == CallStatus.MISSED.name || existingStatus == CallStatus.REJECTED.name) {
                    return@runTransaction
                }

                val walletSnap = transaction.get(walletRef)
                val currentBal = walletSnap.getLong("balanceCoins") ?: 0L
                val currentSpent = walletSnap.getLong("totalSpentCoins") ?: 0L
                val hostSnap = transaction.get(hostRef)
                val hostCalls = hostSnap.getLong("totalCalls") ?: 0L
                val hostMins = hostSnap.getLong("totalMinutes") ?: 0L

                actualBilled = minOf(currentBal, calculatedBilled)
                hostEarned = (actualBilled * pricing.hostSharePct) / 100L
                commission = actualBilled - hostEarned
                val newBalance = currentBal - actualBilled

                transaction.update(
                    callRef,
                    mapOf(
                        "status" to finalStatus.name,
                        "durationSeconds" to elapsedSeconds,
                        "billedCoins" to actualBilled,
                        "hostEarnedCoins" to hostEarned,
                        "platformCommissionCoins" to commission,
                        "recordingActive" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (actualBilled > 0L && call.callerId == uid) {
                    transaction.update(
                        walletRef,
                        mapOf(
                            "balanceCoins" to newBalance,
                            "totalSpentCoins" to (currentSpent + actualBilled),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    transaction.set(
                        txRef,
                        mapOf(
                            "userId" to call.callerId,
                            "type" to if (call.callTypeEnum == CallType.AUDIO) WalletTxType.AUDIO_CALL.name else WalletTxType.VIDEO_CALL.name,
                            "amountCoins" to -actualBilled,
                            "amountInr" to 0L,
                            "balanceAfter" to newBalance,
                            "referenceId" to call.id,
                            "status" to "CONFIRMED",
                            "description" to "${call.callTypeEnum.name} call with ${call.hostName} (${elapsedSeconds}s)",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    transaction.set(
                        earningRef,
                        mapOf(
                            "hostId" to call.hostId,
                            "hostOwnerUid" to call.hostOwnerUid,
                            "category" to if (call.callTypeEnum == CallType.AUDIO) "AUDIO_CALL" else "VIDEO_CALL",
                            "grossCoins" to actualBilled,
                            "hostShareCoins" to hostEarned,
                            "platformCommissionCoins" to commission,
                            "adjustmentCoins" to 0L,
                            "status" to "PENDING",
                            "referenceId" to call.id,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                if (hostSnap.exists() && finalStatus == CallStatus.COMPLETED) {
                    transaction.update(
                        hostRef,
                        mapOf(
                            "totalCalls" to (hostCalls + 1L),
                            "totalMinutes" to (hostMins + max(1L, elapsedSeconds / 60L)),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()

            Result.success(
                call.copy(
                    status = finalStatus.name,
                    durationSeconds = elapsedSeconds,
                    billedCoins = actualBilled,
                    hostEarnedCoins = hostEarned,
                    platformCommissionCoins = commission,
                    recordingActive = false
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, callRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendVirtualGift(
        senderName: String,
        host: HostProfile,
        gift: VirtualGift,
        pricing: AppPricingSettings,
        callId: String? = null
    ): Result<GiftTransaction> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val txId = "gft_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val walletRef = db.collection("wallets").document(uid)
        val giftTxRef = db.collection("gift_transactions").document(txId)
        val walletTxRef = db.collection("wallet_transactions").document("wtx_$txId")
        val earningRef = db.collection("host_earnings").document("gearn_$txId")

        return try {
            val hostShare = (gift.coinPrice * pricing.hostSharePct) / 100L
            val commission = gift.coinPrice - hostShare

            db.runTransaction { transaction ->
                val walletSnap = transaction.get(walletRef)
                val balance = walletSnap.getLong("balanceCoins") ?: 0L
                val spent = walletSnap.getLong("totalSpentCoins") ?: 0L
                if (balance < gift.coinPrice) {
                    throw IllegalStateException("Not enough coins ($balance) for ${gift.name} (${gift.coinPrice} coins).")
                }
                val newBalance = balance - gift.coinPrice

                transaction.update(
                    walletRef,
                    mapOf(
                        "balanceCoins" to newBalance,
                        "totalSpentCoins" to (spent + gift.coinPrice),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                val giftPayload = mapOf(
                    "senderId" to uid,
                    "senderName" to senderName.trim().ifBlank { "Caller" }.take(60),
                    "hostId" to host.id,
                    "hostOwnerUid" to host.ownerUid,
                    "hostName" to host.name,
                    "giftId" to gift.id.ifBlank { gift.artKey },
                    "giftName" to gift.name,
                    "coinPrice" to gift.coinPrice,
                    "hostShareCoins" to hostShare,
                    "platformCommissionCoins" to commission,
                    "callId" to callId,
                    "createdAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                transaction.set(giftTxRef, giftPayload)

                transaction.set(
                    walletTxRef,
                    mapOf(
                        "userId" to uid,
                        "type" to WalletTxType.GIFT.name,
                        "amountCoins" to -gift.coinPrice,
                        "amountInr" to 0L,
                        "balanceAfter" to newBalance,
                        "referenceId" to txId,
                        "status" to "CONFIRMED",
                        "description" to "Sent ${gift.name} to ${host.name}",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )

                transaction.set(
                    earningRef,
                    mapOf(
                        "hostId" to host.id,
                        "hostOwnerUid" to host.ownerUid,
                        "category" to "GIFT",
                        "grossCoins" to gift.coinPrice,
                        "hostShareCoins" to hostShare,
                        "platformCommissionCoins" to commission,
                        "adjustmentCoins" to 0L,
                        "status" to "PENDING",
                        "referenceId" to txId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(
                GiftTransaction(
                    id = txId,
                    senderId = uid,
                    senderName = senderName,
                    hostId = host.id,
                    hostOwnerUid = host.ownerUid,
                    hostName = host.name,
                    giftId = gift.id,
                    giftName = gift.name,
                    coinPrice = gift.coinPrice,
                    hostShareCoins = hostShare,
                    platformCommissionCoins = commission,
                    callId = callId
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, giftTxRef.path)
            Result.failure(e)
        }
    }

    fun observeChatsForUser(userId: String): Flow<List<ChatThread>> =
        db.collection("chats")
            .whereArrayContains("participantUids", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(ChatThread::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "chats")
                }
                throw error
            }

    fun observeMessages(chatId: String, userId: String): Flow<List<ChatMessage>> =
        db.collection("chats").document(chatId).collection("messages")
            .whereArrayContains("participantUids", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "chats/$chatId/messages")
                }
                throw error
            }

    suspend fun getOrCreateChatThread(
        callerName: String,
        host: HostProfile
    ): Result<ChatThread> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val chatId = "chat_${uid.take(12)}_${host.id}"
        val chatRef = db.collection("chats").document(chatId)
        return try {
            val snap = chatRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(ChatThread::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (existing != null) return Result.success(existing)
            }
            val participants = if (uid == host.ownerUid) listOf(uid, host.id) else listOf(uid, host.ownerUid)
            chatRef.set(
                mapOf(
                    "participantUids" to participants,
                    "callerId" to uid,
                    "callerName" to callerName.trim().ifBlank { "Caller" }.take(60),
                    "hostId" to host.id,
                    "hostOwnerUid" to host.ownerUid,
                    "hostName" to host.name,
                    "lastMessage" to "Connected on Vibra",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(
                ChatThread(
                    id = chatId,
                    participantUids = participants,
                    callerId = uid,
                    callerName = callerName,
                    hostId = host.id,
                    hostOwnerUid = host.ownerUid,
                    hostName = host.name,
                    lastMessage = "Connected on Vibra"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, chatRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(
        thread: ChatThread,
        senderName: String,
        text: String,
        messageType: String = "TEXT",
        mediaLabel: String? = null,
        giftId: String? = null
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Message cannot be empty"))
        val chatRef = db.collection("chats").document(thread.id)
        val msgRef = chatRef.collection("messages").document("msg_${UUID.randomUUID().toString().replace("-", "").take(14)}")
        return try {
            val msgPayload = mapOf(
                "participantUids" to thread.participantUids,
                "senderId" to uid,
                "senderName" to senderName.trim().ifBlank { "Member" }.take(60),
                "text" to cleanText,
                "messageType" to messageType,
                "mediaLabel" to mediaLabel?.take(120),
                "giftId" to giftId?.take(128),
                "deliveryStatus" to "DELIVERED",
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            msgRef.set(msgPayload).await()
            chatRef.update(
                mapOf(
                    "lastMessage" to cleanText.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitComplaint(
        targetId: String,
        targetName: String,
        reason: String,
        details: String?,
        actionType: String
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val compId = "cmp_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val compRef = db.collection("complaints").document(compId)
        return try {
            val payload = mapOf(
                "reporterId" to uid,
                "targetId" to targetId.take(128),
                "targetName" to targetName.trim().ifBlank { "User" }.take(60),
                "reason" to reason.trim().ifBlank { "Policy Violation" }.take(80),
                "details" to details?.trim()?.take(400)?.takeIf { it.isNotEmpty() },
                "actionType" to actionType,
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            compRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, compRef.path)
            Result.failure(e)
        }
    }

    fun observeGiftTransactionsForUser(userId: String): Flow<List<GiftTransaction>> =
        db.collection("gift_transactions")
            .whereEqualTo("senderId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(GiftTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "gift_transactions")
                }
                throw error
            }
}
