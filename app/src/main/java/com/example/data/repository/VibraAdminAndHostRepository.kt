package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AdminAuditLog
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallSession
import com.example.data.model.Complaint
import com.example.data.model.ConsentedRecording
import com.example.data.model.FrameTier
import com.example.data.model.HostAttendance
import com.example.data.model.HostEarning
import com.example.data.model.HostFrame
import com.example.data.model.HostProfile
import com.example.data.model.HostSalary
import com.example.data.model.HostStatus
import com.example.data.model.OnlineStatus
import com.example.data.model.PaymentRecord
import com.example.data.model.PayoutMethod
import com.example.data.model.RechargePackage
import com.example.data.model.SalaryStatus
import com.example.data.model.UserProfile
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VibraAdminAndHostRepository(
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

    private fun <T> safeCollectionFlow(
        path: String,
        query: com.google.firebase.firestore.Query,
        clazz: Class<T>
    ): Flow<List<T>> =
        query.snapshots()
            .map { snap -> snap.toObjects(clazz, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                }
                throw error
            }

    // --- HOST STUDIO OPERATIONS ---

    fun observeHostEarnings(hostOwnerUid: String): Flow<List<HostEarning>> =
        safeCollectionFlow(
            "host_earnings",
            db.collection("host_earnings").whereEqualTo("hostOwnerUid", hostOwnerUid),
            HostEarning::class.java
        )

    fun observeHostAttendance(hostOwnerUid: String): Flow<List<HostAttendance>> =
        safeCollectionFlow(
            "host_attendance",
            db.collection("host_attendance").whereEqualTo("hostOwnerUid", hostOwnerUid),
            HostAttendance::class.java
        )

    fun observeHostSalaries(hostOwnerUid: String): Flow<List<HostSalary>> =
        safeCollectionFlow(
            "host_salaries",
            db.collection("host_salaries").whereEqualTo("hostOwnerUid", hostOwnerUid),
            HostSalary::class.java
        )

    suspend fun applyOrRegisterAsHost(name: String, bio: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val hostId = "host_user_${uid.take(12)}"
        val hostRef = db.collection("hosts").document(hostId)
        return try {
            val cleanName = name.trim().ifBlank { "Vibra Host" }.take(50)
            val code = "VBR-${uid.take(5).uppercase()}"
            hostRef.set(
                mapOf(
                    "ownerUid" to uid,
                    "hostCode" to code,
                    "name" to cleanName,
                    "bio" to bio.trim().take(300),
                    "avatarKey" to "aurora",
                    "frameTier" to FrameTier.NEW_HOST.name,
                    "status" to HostStatus.PENDING.name,
                    "onlineStatus" to OnlineStatus.OFFLINE.name,
                    "audioAvailable" to true,
                    "videoAvailable" to true,
                    "featuredHost" to false,
                    "popularHost" to false,
                    "newHost" to true,
                    "premiumTier" to false,
                    "audioRatePerMin" to 20L,
                    "videoRatePerMin" to 60L,
                    "rating" to 5.0,
                    "totalCalls" to 0L,
                    "totalMinutes" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, hostRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateHostAvailabilityAndBio(
        host: HostProfile,
        onlineStatus: OnlineStatus,
        bio: String,
        audioAvailable: Boolean,
        videoAvailable: Boolean
    ): Result<Unit> {
        val path = "hosts/${host.id}"
        return try {
            val uid = requireUserId()
            check(uid == host.ownerUid) { "Only host owner can update availability" }
            db.document(path).update(
                mapOf(
                    "onlineStatus" to onlineStatus.name,
                    "bio" to bio.trim().take(300),
                    "audioAvailable" to audioAvailable,
                    "videoAvailable" to videoAvailable,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleHostAttendanceShift(host: HostProfile, checkIn: Boolean): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val timeLabel = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        val docId = "att_${host.id}_$dateKey"
        val docRef = db.collection("host_attendance").document(docId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "hostId" to host.id,
                        "hostOwnerUid" to uid,
                        "hostName" to host.name,
                        "status" to if (checkIn) "ONLINE" else "OFFLINE",
                        "loginTimeLabel" to timeLabel,
                        "logoutTimeLabel" to if (checkIn) null else timeLabel,
                        "workedMinutes" to if (checkIn) 0L else 60L,
                        "dateKey" to dateKey,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }
                ).await()
            } else {
                val currentWorked = snap.getLong("workedMinutes") ?: 0L
                docRef.update(
                    mapOf(
                        "status" to if (checkIn) "ONLINE" else "OFFLINE",
                        "logoutTimeLabel" to if (checkIn) "" else timeLabel,
                        "workedMinutes" to if (checkIn) currentWorked else currentWorked + 45L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- ADMIN CONTROL CENTER STREAMS ---

    fun observeAllUsersAdmin(): Flow<List<UserProfile>> =
        safeCollectionFlow("users", db.collection("users"), UserProfile::class.java)

    fun observeAllWalletsAdmin(): Flow<List<Wallet>> =
        safeCollectionFlow("wallets", db.collection("wallets"), Wallet::class.java)

    fun observeAllCallsAdmin(): Flow<List<CallSession>> =
        safeCollectionFlow("calls", db.collection("calls"), CallSession::class.java)

    fun observeAllPaymentsAdmin(): Flow<List<PaymentRecord>> =
        safeCollectionFlow("payments", db.collection("payments"), PaymentRecord::class.java)

    fun observeAllEarningsAdmin(): Flow<List<HostEarning>> =
        safeCollectionFlow("host_earnings", db.collection("host_earnings"), HostEarning::class.java)

    fun observeAllSalariesAdmin(): Flow<List<HostSalary>> =
        safeCollectionFlow("host_salaries", db.collection("host_salaries"), HostSalary::class.java)

    fun observeAllAttendanceAdmin(): Flow<List<HostAttendance>> =
        safeCollectionFlow("host_attendance", db.collection("host_attendance"), HostAttendance::class.java)

    fun observeAllRecordingsAdmin(): Flow<List<ConsentedRecording>> =
        safeCollectionFlow("consented_recordings", db.collection("consented_recordings"), ConsentedRecording::class.java)

    fun observeAllComplaintsAdmin(): Flow<List<Complaint>> =
        safeCollectionFlow("complaints", db.collection("complaints"), Complaint::class.java)

    fun observeAdminAuditLogs(): Flow<List<AdminAuditLog>> =
        safeCollectionFlow("admin_audit_logs", db.collection("admin_audit_logs"), AdminAuditLog::class.java)

    private suspend fun writeAuditLog(action: String, targetCollection: String, targetId: String, summary: String) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: "admin@vibra.app"
        val logId = "aud_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        db.collection("admin_audit_logs").document(logId).set(
            mapOf(
                "adminUid" to uid,
                "adminEmail" to email,
                "action" to action.take(80),
                "targetCollection" to targetCollection.take(60),
                "targetId" to targetId.take(128),
                "summary" to summary.take(300),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun adminCreateHost(
        name: String,
        bio: String,
        frameTier: FrameTier,
        audioRate: Long,
        videoRate: Long
    ): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val id = "host_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val code = "VBR-H${(100..999).random()}"
        val ref = db.collection("hosts").document(id)
        return try {
            ref.set(
                mapOf(
                    "ownerUid" to uid,
                    "hostCode" to code,
                    "name" to name.trim().ifBlank { "New Host" }.take(50),
                    "bio" to bio.trim().take(300),
                    "avatarKey" to "aurora",
                    "frameTier" to frameTier.name,
                    "status" to HostStatus.APPROVED.name,
                    "onlineStatus" to OnlineStatus.ONLINE.name,
                    "audioAvailable" to true,
                    "videoAvailable" to true,
                    "featuredHost" to true,
                    "popularHost" to false,
                    "newHost" to true,
                    "premiumTier" to (frameTier == FrameTier.DIAMOND || frameTier == FrameTier.PLATINUM || frameTier == FrameTier.STAR),
                    "audioRatePerMin" to audioRate,
                    "videoRatePerMin" to videoRate,
                    "rating" to 5.0,
                    "totalCalls" to 0L,
                    "totalMinutes" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("CREATE_HOST", "hosts", id, "Created host $name ($code) with ${frameTier.name} frame")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateHostStatusAndFrame(
        host: HostProfile,
        newStatus: HostStatus,
        newFrame: FrameTier
    ): Result<Unit> {
        val path = "hosts/${host.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "status" to newStatus.name,
                    "frameTier" to newFrame.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("UPDATE_HOST", "hosts", host.id, "Set ${host.name} status=${newStatus.name}, frame=${newFrame.name}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminToggleFrameActive(frame: HostFrame, active: Boolean): Result<Unit> {
        val path = "host_frames/${frame.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("TOGGLE_FRAME", "host_frames", frame.id, "Set frame ${frame.name} active=$active")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminCreateCustomFrame(
        name: String,
        tier: FrameTier,
        description: String,
        accentHex: String,
        secondaryHex: String
    ): Result<Unit> {
        val id = "frame_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val path = "host_frames/$id"
        return try {
            requireUserId()
            db.document(path).set(
                mapOf(
                    "name" to name.trim().take(40),
                    "tier" to tier.name,
                    "description" to description.trim().take(200),
                    "accentHex" to accentHex.take(16),
                    "secondaryHex" to secondaryHex.take(16),
                    "animationStyle" to "CUSTOM_3D",
                    "active" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("CREATE_FRAME", "host_frames", id, "Created custom frame $name (${tier.name})")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateCallPricing(settings: AppPricingSettings): Result<Unit> {
        val path = "app_settings/pricing"
        return try {
            requireUserId()
            db.document(path).set(
                mapOf(
                    "audioRatePerMin" to settings.audioRatePerMin,
                    "videoRatePerMin" to settings.videoRatePerMin,
                    "minBillingSeconds" to settings.minBillingSeconds,
                    "billingIntervalSeconds" to settings.billingIntervalSeconds,
                    "platformCommissionPct" to settings.platformCommissionPct,
                    "hostSharePct" to settings.hostSharePct,
                    "promoDiscountPct" to settings.promoDiscountPct,
                    "promoLabel" to settings.promoLabel,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()
            writeAuditLog(
                "UPDATE_PRICING",
                "app_settings",
                "pricing",
                "Audio ₹${settings.audioRatePerMin}/m, Video ₹${settings.videoRatePerMin}/m, Split ${settings.hostSharePct}/${settings.platformCommissionPct}"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun adminSaveRechargePackage(
        pkgId: String?,
        title: String,
        amountInr: Long,
        coins: Long,
        bonusCoins: Long,
        badge: String,
        active: Boolean
    ): Result<Unit> {
        val id = pkgId?.takeIf { it.isNotBlank() } ?: "pkg_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val path = "recharge_packages/$id"
        return try {
            requireUserId()
            val ref = db.document(path)
            val exists = ref.get().await().exists()
            if (!exists) {
                ref.set(
                    mapOf(
                        "title" to title.trim().take(40),
                        "amountInr" to amountInr,
                        "coins" to coins,
                        "bonusCoins" to bonusCoins,
                        "badge" to badge.trim().take(24),
                        "active" to active,
                        "sortOrder" to amountInr,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "title" to title.trim().take(40),
                        "amountInr" to amountInr,
                        "coins" to coins,
                        "bonusCoins" to bonusCoins,
                        "badge" to badge.trim().take(24),
                        "active" to active,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            writeAuditLog("SAVE_PACKAGE", "recharge_packages", id, "Saved ₹$amountInr -> ${coins + bonusCoins} coins")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateGiftPrice(gift: VirtualGift, newPrice: Long, active: Boolean): Result<Unit> {
        val path = "gifts/${gift.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "coinPrice" to newPrice,
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("UPDATE_GIFT", "gifts", gift.id, "Gift ${gift.name} price=$newPrice, active=$active")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminAdjustWallet(targetUserId: String, deltaCoins: Long, reason: String): Result<Unit> {
        val walletRef = db.collection("wallets").document(targetUserId)
        val txId = "adj_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val txRef = db.collection("wallet_transactions").document(txId)
        return try {
            requireUserId()
            var afterBal = 0L
            db.runTransaction { transaction ->
                val snap = transaction.get(walletRef)
                val current = snap.getLong("balanceCoins") ?: 0L
                val next = (current + deltaCoins).coerceAtLeast(0L)
                afterBal = next
                if (snap.exists()) {
                    transaction.update(
                        walletRef,
                        mapOf(
                            "balanceCoins" to next,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        walletRef,
                        mapOf(
                            "userId" to targetUserId,
                            "balanceCoins" to next,
                            "totalRechargedInr" to 0L,
                            "totalSpentCoins" to 0L,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to targetUserId,
                        "type" to WalletTxType.ADMIN_ADJUSTMENT.name,
                        "amountCoins" to deltaCoins,
                        "amountInr" to 0L,
                        "balanceAfter" to next,
                        "referenceId" to txId,
                        "status" to "CONFIRMED",
                        "description" to reason.trim().ifBlank { "Admin adjustment" }.take(160),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            writeAuditLog("WALLET_ADJUST", "wallets", targetUserId, "Adjusted $deltaCoins coins (new bal=$afterBal): $reason")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, walletRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminCreateOrUpdateSalary(
        existingSalary: HostSalary?,
        host: HostProfile,
        amountInr: Long,
        payoutMethod: PayoutMethod,
        paymentReference: String,
        newStatus: SalaryStatus
    ): Result<Unit> {
        val id = existingSalary?.id?.takeIf { it.isNotBlank() }
            ?: "sal_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "host_salaries/$id"
        val paidDate = if (newStatus == SalaryStatus.PAID) {
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        } else existingSalary?.paidDateLabel
        return try {
            requireUserId()
            val ref = db.document(path)
            if (existingSalary == null) {
                ref.set(
                    mapOf(
                        "hostId" to host.id,
                        "hostOwnerUid" to host.ownerUid,
                        "hostName" to host.name,
                        "periodLabel" to SimpleDateFormat("MMM yyyy", Locale.US).format(Date()),
                        "approvedAmountInr" to if (newStatus == SalaryStatus.APPROVED || newStatus == SalaryStatus.PAID) amountInr else 0L,
                        "pendingAmountInr" to if (newStatus == SalaryStatus.PENDING || newStatus == SalaryStatus.PROCESSING) amountInr else 0L,
                        "paidAmountInr" to if (newStatus == SalaryStatus.PAID) amountInr else 0L,
                        "payoutMethod" to payoutMethod.name,
                        "paymentReference" to paymentReference.trim().take(100).takeIf { it.isNotEmpty() },
                        "status" to newStatus.name,
                        "paidDateLabel" to paidDate,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "approvedAmountInr" to if (newStatus == SalaryStatus.APPROVED || newStatus == SalaryStatus.PAID) amountInr else existingSalary.approvedAmountInr,
                        "pendingAmountInr" to if (newStatus == SalaryStatus.PAID) 0L else amountInr,
                        "paidAmountInr" to if (newStatus == SalaryStatus.PAID) amountInr else existingSalary.paidAmountInr,
                        "payoutMethod" to payoutMethod.name,
                        "paymentReference" to paymentReference.trim().take(100),
                        "status" to newStatus.name,
                        "paidDateLabel" to (paidDate ?: ""),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            writeAuditLog("SALARY_${newStatus.name}", "host_salaries", id, "${host.name}: ₹$amountInr via ${payoutMethod.name} ($paymentReference)")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun adminDeleteConsentedRecording(rec: ConsentedRecording): Result<Unit> {
        val path = "consented_recordings/${rec.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "status" to "DELETED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("DELETE_RECORDING", "consented_recordings", rec.id, "Deleted recording for call ${rec.callId}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminResolveComplaint(complaint: Complaint, status: String): Result<Unit> {
        val path = "complaints/${complaint.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("RESOLVE_COMPLAINT", "complaints", complaint.id, "Marked ${complaint.targetName} complaint as $status")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminSendAnnouncement(targetUid: String, title: String, body: String): Result<Unit> {
        val id = "notif_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "notifications/$id"
        return try {
            requireUserId()
            db.document(path).set(
                mapOf(
                    "userId" to targetUid,
                    "title" to title.trim().take(80),
                    "body" to body.trim().take(300),
                    "category" to "ADMIN",
                    "readFlag" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("SEND_ANNOUNCEMENT", "notifications", id, "Sent announcement '$title' to $targetUid")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateUserStatus(user: UserProfile, status: String): Result<Unit> {
        val path = "users/${user.userId}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "accountStatus" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("USER_STATUS", "users", user.userId, "Set ${user.displayName} accountStatus=$status")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminAdjustCallTime(call: CallSession, newDurationSeconds: Long, newBilledCoins: Long): Result<Unit> {
        val path = "calls/${call.id}"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "durationSeconds" to newDurationSeconds,
                    "billedCoins" to newBilledCoins,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog("CALL_TIME_ADJUST", "calls", call.id, "Adjusted call ${call.id} duration=${newDurationSeconds}s, billed=$newBilledCoins")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
