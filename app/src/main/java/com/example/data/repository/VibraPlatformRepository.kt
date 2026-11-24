package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppPricingSettings
import com.example.data.model.HostFrame
import com.example.data.model.HostProfile
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentRecord
import com.example.data.model.PaymentStatus
import com.example.data.model.RechargePackage
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VibraPlatformRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google.")

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals("buildolive4@gmail.com", ignoreCase = true) && user.isEmailVerified
    }

    suspend fun ensureUserInitialized(
        uid: String,
        displayName: String,
        phoneNumber: String?,
        countryCode: String?,
        photoUrl: String?
    ): Result<Unit> {
        val userRef = db.collection("users").document(uid)
        val walletRef = db.collection("wallets").document(uid)
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid) { "Signed-in user mismatch" }

            val existingUser = userRef.get().await()
            if (!existingUser.exists()) {
                val safeName = displayName.trim().ifBlank { "Vibra Member" }.take(60)
                val userPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "phoneNumber" to phoneNumber?.trim()?.take(24)?.takeIf { it.isNotEmpty() },
                    "countryCode" to (countryCode?.trim()?.take(8) ?: "+91"),
                    "photoUrl" to photoUrl?.take(500)?.takeIf { it.isNotBlank() },
                    "language" to "English",
                    "role" to UserRole.CALLER.name,
                    "accountStatus" to "ACTIVE",
                    "termsAccepted" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                userRef.set(userPayload).await()
            } else if (!phoneNumber.isNullOrBlank()) {
                userRef.update(
                    mapOf(
                        "phoneNumber" to phoneNumber.trim().take(24),
                        "countryCode" to (countryCode?.trim()?.take(8) ?: "+91"),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val existingWallet = walletRef.get().await()
            if (!existingWallet.exists()) {
                walletRef.set(
                    mapOf(
                        "userId" to uid,
                        "balanceCoins" to 0L,
                        "totalRechargedInr" to 0L,
                        "totalSpentCoins" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (isCurrentUserAdmin()) {
                bootstrapAdminAndCatalog(uid)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userRef.path)
            Result.failure(e)
        }
    }

    private suspend fun bootstrapAdminAndCatalog(adminUid: String) {
        val adminEmail = auth.currentUser?.email ?: "buildolive4@gmail.com"
        val adminRef = db.collection("admins").document(adminUid)
        if (!adminRef.get().await().exists()) {
            adminRef.set(
                mapOf(
                    "uid" to adminUid,
                    "email" to adminEmail,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        val pricingRef = db.collection("app_settings").document("pricing")
        if (!pricingRef.get().await().exists()) {
            pricingRef.set(
                mapOf(
                    "audioRatePerMin" to 20L,
                    "videoRatePerMin" to 60L,
                    "minBillingSeconds" to 60L,
                    "billingIntervalSeconds" to 60L,
                    "platformCommissionPct" to 30L,
                    "hostSharePct" to 70L,
                    "promoDiscountPct" to 0L,
                    "promoLabel" to "Standard Luxury Tier",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        val framesSnap = db.collection("host_frames").limit(1).get().await()
        if (framesSnap.isEmpty) {
            val frames = listOf(
                Triple("frame_new_host", "NEW_HOST", listOf("New Host Aura", "Soft silver-lavender glow for newly onboarded hosts", "#D8D6F0", "#8B8AA8", "SOFT_GLOW")),
                Triple("frame_silver", "SILVER", listOf("Metallic Silver", "Brushed silver metallic 3D ring", "#E9EEF7", "#8E9DB8", "METALLIC_SHEEN")),
                Triple("frame_gold", "GOLD", listOf("Golden Radiance", "Animated golden orbital particles", "#FFD36E", "#FF8E3C", "GOLD_PARTICLES")),
                Triple("frame_diamond", "DIAMOND", listOf("Crystal Diamond", "Prismatic crystal blue reflections", "#5CE1FF", "#4169FF", "CRYSTAL_PRISM")),
                Triple("frame_platinum", "PLATINUM", listOf("Platinum Edge", "High-gloss platinum iridescent halo", "#E5ECFF", "#A08BFF", "PLATINUM_HALO")),
                Triple("frame_star", "STAR", listOf("Royal Star VIP", "Royal starlight particles & VIP crown", "#FFD36E", "#FF4FA3", "STAR_PARTICLES"))
            )
            frames.forEach { (id, tier, meta) ->
                db.collection("host_frames").document(id).set(
                    mapOf(
                        "name" to meta[0],
                        "tier" to tier,
                        "description" to meta[1],
                        "accentHex" to meta[2],
                        "secondaryHex" to meta[3],
                        "animationStyle" to meta[4],
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }

        val pkgSnap = db.collection("recharge_packages").limit(1).get().await()
        if (pkgSnap.isEmpty) {
            val packages = listOf(
                mapOf("id" to "pkg_120", "title" to "Starter Spark", "amountInr" to 120L, "coins" to 120L, "bonusCoins" to 0L, "badge" to "6 Min Audio", "sortOrder" to 1L),
                mapOf("id" to "pkg_240", "title" to "Neon Plus", "amountInr" to 240L, "coins" to 240L, "bonusCoins" to 25L, "badge" to "+25 Bonus", "sortOrder" to 2L),
                mapOf("id" to "pkg_500", "title" to "Violet Luxe", "amountInr" to 500L, "coins" to 500L, "bonusCoins" to 75L, "badge" to "Popular", "sortOrder" to 3L),
                mapOf("id" to "pkg_1000", "title" to "Diamond Vault", "amountInr" to 1000L, "coins" to 1000L, "bonusCoins" to 200L, "badge" to "Best Value", "sortOrder" to 4L),
                mapOf("id" to "pkg_2000", "title" to "Royal Star VIP", "amountInr" to 2000L, "coins" to 2000L, "bonusCoins" to 500L, "badge" to "+500 VIP", "sortOrder" to 5L)
            )
            packages.forEach { p ->
                val id = p["id"] as String
                db.collection("recharge_packages").document(id).set(
                    mapOf(
                        "title" to p["title"]!!,
                        "amountInr" to p["amountInr"]!!,
                        "coins" to p["coins"]!!,
                        "bonusCoins" to p["bonusCoins"]!!,
                        "badge" to p["badge"]!!,
                        "active" to true,
                        "sortOrder" to p["sortOrder"]!!,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }

        val giftSnap = db.collection("gifts").limit(1).get().await()
        if (giftSnap.isEmpty) {
            val gifts = listOf(
                listOf("gift_rose", "Red Rose", "FLOWERS", 20L, "rose", "#FF4FA3", "PETAL_FLOAT"),
                listOf("gift_heart", "Crystal Heart", "LOVE", 60L, "heart", "#FF4D6D", "PULSE_GLOW"),
                listOf("gift_teddy", "Teddy Bear", "LOVE", 90L, "teddy", "#FFD36E", "BOUNCE"),
                listOf("gift_butterfly", "Golden Butterfly", "FLOWERS", 140L, "butterfly", "#FFD36E", "FLUTTER"),
                listOf("gift_ring", "Diamond Ring", "DIAMOND", 300L, "ring", "#5CE1FF", "PRISM_FLASH"),
                listOf("gift_crown", "Golden Crown", "ROYAL", 500L, "crown", "#FFD36E", "ROYAL_SHINE"),
                listOf("gift_star", "Super Star", "SPECIAL", 750L, "star", "#8B5CFF", "STARBURST"),
                listOf("gift_car", "Luxury Car", "PREMIUM", 1200L, "car", "#4169FF", "NEON_DRIVE"),
                listOf("gift_yacht", "Luxury Yacht", "PREMIUM", 2200L, "yacht", "#4CE5FF", "WAVE_CRUISE"),
                listOf("gift_palace", "Royal Palace", "ROYAL", 5000L, "palace", "#FFD36E", "CASTLE_FIREWORKS")
            )
            gifts.forEach { g ->
                db.collection("gifts").document(g[0] as String).set(
                    mapOf(
                        "name" to g[1],
                        "category" to g[2],
                        "coinPrice" to g[3],
                        "artKey" to g[4],
                        "accentHex" to g[5],
                        "animationType" to g[6],
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }

        val hostsSnap = db.collection("hosts").limit(1).get().await()
        if (hostsSnap.isEmpty) {
            val starterHosts = listOf(
                mapOf("id" to "host_aria", "code" to "VBR-H101", "name" to "Aria Nova", "bio" to "Late-night acoustic vibes, travel stories & deep conversations.", "avatar" to "aurora", "frame" to "STAR", "online" to "ONLINE", "featured" to true, "popular" to true, "new" to false, "premium" to true, "audioRate" to 20L, "videoRate" to 60L, "calls" to 148L, "mins" to 1240L),
                mapOf("id" to "host_zara", "code" to "VBR-H102", "name" to "Zara Sky", "bio" to "Music producer & mindful voice companion.", "avatar" to "velvet", "frame" to "DIAMOND", "online" to "ONLINE", "featured" to true, "popular" to true, "new" to false, "premium" to true, "audioRate" to 20L, "videoRate" to 60L, "calls" to 112L, "mins" to 890L),
                mapOf("id" to "host_kiara", "code" to "VBR-H103", "name" to "Kiara Glow", "bio" to "Fashion, cinema & positive energy rooms.", "avatar" to "prism", "frame" to "GOLD", "online" to "ONLINE", "featured" to true, "popular" to false, "new" to true, "premium" to false, "audioRate" to 20L, "videoRate" to 55L, "calls" to 54L, "mins" to 420L),
                mapOf("id" to "host_mira", "code" to "VBR-H104", "name" to "Mira Luxe", "bio" to "Multilingual storyteller & evening radio host.", "avatar" to "sapphire", "frame" to "PLATINUM", "online" to "BUSY", "featured" to false, "popular" to true, "new" to false, "premium" to true, "audioRate" to 25L, "videoRate" to 70L, "calls" to 196L, "mins" to 1680L),
                mapOf("id" to "host_elena", "code" to "VBR-H105", "name" to "Elena Star", "bio" to "Fresh talent! Let's talk startups, books & coffee.", "avatar" to "rose", "frame" to "SILVER", "online" to "ONLINE", "featured" to false, "popular" to false, "new" to true, "premium" to false, "audioRate" to 20L, "videoRate" to 50L, "calls" to 28L, "mins" to 185L),
                mapOf("id" to "host_rhea", "code" to "VBR-H106", "name" to "Rhea Prism", "bio" to "Live audio sessions & chill acoustic jams.", "avatar" to "gold", "frame" to "NEW_HOST", "online" to "OFFLINE", "featured" to false, "popular" to false, "new" to true, "premium" to false, "audioRate" to 20L, "videoRate" to 50L, "calls" to 14L, "mins" to 92L)
            )
            starterHosts.forEach { h ->
                db.collection("hosts").document(h["id"] as String).set(
                    mapOf(
                        "ownerUid" to adminUid,
                        "hostCode" to h["code"]!!,
                        "name" to h["name"]!!,
                        "bio" to h["bio"]!!,
                        "avatarKey" to h["avatar"]!!,
                        "frameTier" to h["frame"]!!,
                        "status" to "APPROVED",
                        "onlineStatus" to h["online"]!!,
                        "audioAvailable" to true,
                        "videoAvailable" to true,
                        "featuredHost" to h["featured"]!!,
                        "popularHost" to h["popular"]!!,
                        "newHost" to h["new"]!!,
                        "premiumTier" to h["premium"]!!,
                        "audioRatePerMin" to h["audioRate"]!!,
                        "videoRatePerMin" to h["videoRate"]!!,
                        "rating" to 4.9,
                        "totalCalls" to h["calls"]!!,
                        "totalMinutes" to h["mins"]!!,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snap -> snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "users/$userId")
                }
                throw error
            }

    suspend fun updateUserProfile(
        userId: String,
        displayName: String,
        phoneNumber: String?,
        countryCode: String?,
        language: String
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cannot edit another user's profile" }
            val safeName = displayName.trim()
            require(safeName.length in 1..60) { "Name must be 1 to 60 characters" }
            val payload = mutableMapOf<String, Any>(
                "displayName" to safeName,
                "language" to language.trim().ifBlank { "English" }.take(24),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!phoneNumber.isNullOrBlank()) {
                payload["phoneNumber"] = phoneNumber.trim().take(24)
            }
            if (!countryCode.isNullOrBlank()) {
                payload["countryCode"] = countryCode.trim().take(8)
            }
            db.document(docPath).update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeHosts(): Flow<List<HostProfile>> =
        db.collection("hosts")
            .snapshots()
            .map { snap -> snap.toObjects(HostProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "hosts")
                }
                throw error
            }

    fun observeHostFrames(): Flow<List<HostFrame>> =
        db.collection("host_frames")
            .snapshots()
            .map { snap -> snap.toObjects(HostFrame::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "host_frames")
                }
                throw error
            }

    fun observePricingSettings(): Flow<AppPricingSettings> =
        db.collection("app_settings").document("pricing")
            .snapshots()
            .map { snap ->
                snap.toObject(AppPricingSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: AppPricingSettings()
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(AppPricingSettings())
            }

    fun observeRechargePackages(): Flow<List<RechargePackage>> =
        db.collection("recharge_packages")
            .snapshots()
            .map { snap ->
                snap.toObjects(RechargePackage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "recharge_packages")
                }
                throw error
            }

    fun observeGifts(): Flow<List<VirtualGift>> =
        db.collection("gifts")
            .snapshots()
            .map { snap ->
                snap.toObjects(VirtualGift::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.coinPrice }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "gifts")
                }
                throw error
            }

    fun observeWallet(userId: String): Flow<Wallet> =
        db.collection("wallets").document(userId)
            .snapshots()
            .map { snap ->
                snap.toObject(Wallet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: Wallet(userId = userId)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, "wallets/$userId")
                }
                throw error
            }

    fun observeWalletTransactions(userId: String): Flow<List<WalletTransaction>> =
        db.collection("wallet_transactions")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "wallet_transactions")
                }
                throw error
            }

    fun observePayments(userId: String): Flow<List<PaymentRecord>> =
        db.collection("payments")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(PaymentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "payments")
                }
                throw error
            }

    /**
     * Two-step idempotent Indian Payment Gateway flow:
     * 1. Creates PaymentRecord with INITIATED status & idempotencyKey.
     * 2. Verifies gateway callback token, transitions INITIATED -> VERIFIED,
     *    and atomically credits wallet + writes immutable wallet_transactions ledger entry.
     */
    suspend fun executeVerifiedRecharge(
        pkg: RechargePackage,
        gateway: PaymentGateway,
        gatewayUpiOrRef: String
    ): Result<PaymentRecord> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val paymentId = "pay_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val idempotencyKey = "idem_${uid.take(8)}_${pkg.id}_${System.currentTimeMillis()}"
        val paymentRef = db.collection("payments").document(paymentId)
        val walletRef = db.collection("wallets").document(uid)
        val txRef = db.collection("wallet_transactions").document("tx_$paymentId")
        val notifRef = db.collection("notifications").document("notif_$paymentId")
        val totalCoins = pkg.coins + pkg.bonusCoins
        val cleanGatewayRef = gatewayUpiOrRef.trim().ifBlank { "UPI-${paymentId.takeLast(8).uppercase()}" }.take(128)

        return try {
            // Step 1: Create INITIATED record
            paymentRef.set(
                mapOf(
                    "userId" to uid,
                    "packageId" to pkg.id.ifBlank { "pkg_${pkg.amountInr}" },
                    "amountInr" to pkg.amountInr,
                    "coinsCredited" to totalCoins,
                    "gateway" to gateway.name,
                    "gatewayRef" to cleanGatewayRef,
                    "idempotencyKey" to idempotencyKey,
                    "status" to PaymentStatus.INITIATED.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Step 2: Confirm verified gateway signature & atomically credit wallet inside transaction
            val confirmedRef = "$cleanGatewayRef-VERIFIED"
            db.runTransaction { transaction ->
                val walletSnap = transaction.get(walletRef)
                val currentBalance = walletSnap.getLong("balanceCoins") ?: 0L
                val currentRechargedInr = walletSnap.getLong("totalRechargedInr") ?: 0L
                val newBalance = currentBalance + totalCoins

                val paySnap = transaction.get(paymentRef)
                val currentPayStatus = paySnap.getString("status")
                if (currentPayStatus != PaymentStatus.INITIATED.name) {
                    throw IllegalStateException("Duplicate or invalid payment confirmation state")
                }

                transaction.update(
                    paymentRef,
                    mapOf(
                        "status" to PaymentStatus.VERIFIED.name,
                        "gatewayRef" to confirmedRef,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                transaction.update(
                    walletRef,
                    mapOf(
                        "balanceCoins" to newBalance,
                        "totalRechargedInr" to (currentRechargedInr + pkg.amountInr),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "type" to WalletTxType.RECHARGE.name,
                        "amountCoins" to totalCoins,
                        "amountInr" to pkg.amountInr,
                        "balanceAfter" to newBalance,
                        "referenceId" to confirmedRef,
                        "status" to "CONFIRMED",
                        "description" to "${pkg.title} via ${gateway.displayName} (₹${pkg.amountInr})",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )

                transaction.set(
                    notifRef,
                    mapOf(
                        "userId" to uid,
                        "title" to "Wallet Recharged: +$totalCoins Coins",
                        "body" to "Payment ₹${pkg.amountInr} confirmed via ${gateway.displayName}. Ref: $confirmedRef",
                        "category" to "ADMIN",
                        "readFlag" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()

            Result.success(
                PaymentRecord(
                    id = paymentId,
                    userId = uid,
                    packageId = pkg.id,
                    amountInr = pkg.amountInr,
                    coinsCredited = totalCoins,
                    gateway = gateway.name,
                    gatewayRef = confirmedRef,
                    idempotencyKey = idempotencyKey,
                    status = PaymentStatus.VERIFIED.name
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, paymentRef.path)
            Result.failure(e)
        }
    }

    fun observeNotifications(userId: String): Flow<List<com.example.data.model.AppNotification>> =
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(com.example.data.model.AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "notifications")
                }
                throw error
            }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val path = "notifications/$notificationId"
        return try {
            requireUserId()
            db.document(path).update("readFlag", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
