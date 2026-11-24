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

enum class AppMode { CALLER, HOST, ADMIN }

enum class UserRole { CALLER, HOST, ADMIN }
enum class AccountStatus { ACTIVE, SUSPENDED, BLOCKED }
enum class FrameTier(val label: String) {
    NEW_HOST("NEW HOST"),
    SILVER("SILVER"),
    GOLD("GOLD"),
    DIAMOND("DIAMOND"),
    PLATINUM("PLATINUM"),
    STAR("STAR")
}
enum class HostStatus { PENDING, APPROVED, REJECTED, SUSPENDED, BLOCKED }
enum class OnlineStatus { ONLINE, BUSY, OFFLINE }
enum class CallType { AUDIO, VIDEO }
enum class CallStatus { RINGING, ACTIVE, COMPLETED, MISSED, REJECTED }
enum class NetworkQuality { EXCELLENT, GOOD, FAIR }
enum class WalletTxType { RECHARGE, AUDIO_CALL, VIDEO_CALL, GIFT, REFUND, ADMIN_ADJUSTMENT }
enum class PaymentGateway(val displayName: String) {
    RAZORPAY_UPI("Razorpay UPI"),
    CASHFREE("Cashfree Instant"),
    PHONEPE_UPI("PhonePe Switch")
}
enum class PaymentStatus { INITIATED, VERIFIED, FAILED, REFUNDED }
enum class GiftCategory(val label: String) {
    LOVE("Love"),
    FLOWERS("Flowers"),
    ROYAL("Royal"),
    PREMIUM("Premium"),
    DIAMOND("Diamond"),
    SPECIAL("Special")
}
enum class SalaryStatus { PENDING, APPROVED, PROCESSING, PAID, FAILED }
enum class PayoutMethod(val label: String) { UPI("UPI"), BANK_TRANSFER("Bank Transfer") }
enum class ComplaintStatus { OPEN, UNDER_REVIEW, RESOLVED, DISMISSED }

inline fun <reified T : Enum<T>> safeEnumValue(raw: String?, default: T): T {
    if (raw.isNullOrBlank()) return default
    return enumValues<T>().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: default
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val phoneNumber: String? = null,
    val countryCode: String? = "+91",
    val photoUrl: String? = null,
    val language: String? = "English",
    val role: String = UserRole.CALLER.name,
    val accountStatus: String = AccountStatus.ACTIVE.name,
    val termsAccepted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val roleEnum: UserRole get() = safeEnumValue(role, UserRole.CALLER)
    val statusEnum: AccountStatus get() = safeEnumValue(accountStatus, AccountStatus.ACTIVE)
}

@Keep
@IgnoreExtraProperties
data class HostProfile(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val hostCode: String = "",
    val name: String = "",
    val bio: String? = null,
    val avatarKey: String? = "aurora",
    val frameTier: String = FrameTier.NEW_HOST.name,
    val status: String = HostStatus.PENDING.name,
    val onlineStatus: String = OnlineStatus.OFFLINE.name,
    val audioAvailable: Boolean = true,
    val videoAvailable: Boolean = true,
    val featuredHost: Boolean = false,
    val popularHost: Boolean = false,
    val newHost: Boolean = true,
    val premiumTier: Boolean = false,
    val audioRatePerMin: Long = 20L,
    val videoRatePerMin: Long = 60L,
    val rating: Double = 4.9,
    val totalCalls: Long = 0L,
    val totalMinutes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val frameTierEnum: FrameTier get() = safeEnumValue(frameTier, FrameTier.NEW_HOST)
    val hostStatusEnum: HostStatus get() = safeEnumValue(status, HostStatus.PENDING)
    val onlineStatusEnum: OnlineStatus get() = safeEnumValue(onlineStatus, OnlineStatus.OFFLINE)
}

@Keep
@IgnoreExtraProperties
data class HostFrame(
    @DocumentId val id: String = "",
    val name: String = "",
    val tier: String = FrameTier.NEW_HOST.name,
    val description: String? = null,
    val accentHex: String = "#8B5CFF",
    val secondaryHex: String = "#FF4FA3",
    val animationStyle: String = "GLOW",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val tierEnum: FrameTier get() = safeEnumValue(tier, FrameTier.NEW_HOST)
}

@Keep
@IgnoreExtraProperties
data class Wallet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val balanceCoins: Long = 0L,
    val totalRechargedInr: Long = 0L,
    val totalSpentCoins: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = WalletTxType.RECHARGE.name,
    val amountCoins: Long = 0L,
    val amountInr: Long = 0L,
    val balanceAfter: Long = 0L,
    val referenceId: String = "",
    val status: String = "CONFIRMED",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val typeEnum: WalletTxType get() = safeEnumValue(type, WalletTxType.RECHARGE)
}

@Keep
@IgnoreExtraProperties
data class RechargePackage(
    @DocumentId val id: String = "",
    val title: String = "",
    val amountInr: Long = 120L,
    val coins: Long = 120L,
    val bonusCoins: Long = 0L,
    val badge: String? = null,
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PaymentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val packageId: String = "",
    val amountInr: Long = 0L,
    val coinsCredited: Long = 0L,
    val gateway: String = PaymentGateway.RAZORPAY_UPI.name,
    val gatewayRef: String = "",
    val idempotencyKey: String = "",
    val status: String = PaymentStatus.INITIATED.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: PaymentStatus get() = safeEnumValue(status, PaymentStatus.INITIATED)
    val gatewayEnum: PaymentGateway get() = safeEnumValue(gateway, PaymentGateway.RAZORPAY_UPI)
}

@Keep
@IgnoreExtraProperties
data class AppPricingSettings(
    @DocumentId val id: String = "",
    val audioRatePerMin: Long = 20L,
    val videoRatePerMin: Long = 60L,
    val minBillingSeconds: Long = 60L,
    val billingIntervalSeconds: Long = 60L,
    val platformCommissionPct: Long = 30L,
    val hostSharePct: Long = 70L,
    val promoDiscountPct: Long = 0L,
    val promoLabel: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VirtualGift(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = GiftCategory.LOVE.name,
    val coinPrice: Long = 20L,
    val artKey: String = "rose",
    val accentHex: String = "#FF4FA3",
    val animationType: String = "SPARKLE",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: GiftCategory get() = safeEnumValue(category, GiftCategory.LOVE)
}

@Keep
@IgnoreExtraProperties
data class GiftTransaction(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val hostName: String = "",
    val giftId: String = "",
    val giftName: String = "",
    val coinPrice: Long = 0L,
    val hostShareCoins: Long = 0L,
    val platformCommissionCoins: Long = 0L,
    val callId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CallSession(
    @DocumentId val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val hostName: String = "",
    val callType: String = CallType.AUDIO.name,
    val status: String = CallStatus.ACTIVE.name,
    val ratePerMin: Long = 20L,
    val durationSeconds: Long = 0L,
    val billedCoins: Long = 0L,
    val hostEarnedCoins: Long = 0L,
    val platformCommissionCoins: Long = 0L,
    val networkQuality: String = NetworkQuality.EXCELLENT.name,
    val callerConsentRecording: Boolean = false,
    val hostConsentRecording: Boolean = false,
    val recordingActive: Boolean = false,
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val callTypeEnum: CallType get() = safeEnumValue(callType, CallType.AUDIO)
    val callStatusEnum: CallStatus get() = safeEnumValue(status, CallStatus.ACTIVE)
    val qualityEnum: NetworkQuality get() = safeEnumValue(networkQuality, NetworkQuality.EXCELLENT)
}

@Keep
@IgnoreExtraProperties
data class ChatThread(
    @DocumentId val id: String = "",
    val participantUids: List<String> = emptyList(),
    val callerId: String = "",
    val callerName: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val hostName: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val participantUids: List<String> = emptyList(),
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val messageType: String = "TEXT",
    val mediaLabel: String? = null,
    val giftId: String? = null,
    val deliveryStatus: String = "SENT",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class HostEarning(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val category: String = "AUDIO_CALL",
    val grossCoins: Long = 0L,
    val hostShareCoins: Long = 0L,
    val platformCommissionCoins: Long = 0L,
    val adjustmentCoins: Long = 0L,
    val status: String = "PENDING",
    val referenceId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class HostAttendance(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val hostName: String = "",
    val status: String = "ONLINE",
    val loginTimeLabel: String = "",
    val logoutTimeLabel: String? = null,
    val workedMinutes: Long = 0L,
    val dateKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class HostSalary(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val hostName: String = "",
    val periodLabel: String = "",
    val approvedAmountInr: Long = 0L,
    val pendingAmountInr: Long = 0L,
    val paidAmountInr: Long = 0L,
    val payoutMethod: String = PayoutMethod.UPI.name,
    val paymentReference: String? = null,
    val status: String = SalaryStatus.PENDING.name,
    val paidDateLabel: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val salaryStatusEnum: SalaryStatus get() = safeEnumValue(status, SalaryStatus.PENDING)
    val payoutMethodEnum: PayoutMethod get() = safeEnumValue(payoutMethod, PayoutMethod.UPI)
}

@Keep
@IgnoreExtraProperties
data class ConsentedRecording(
    @DocumentId val id: String = "",
    val callId: String = "",
    val callerId: String = "",
    val hostId: String = "",
    val hostOwnerUid: String = "",
    val callerConsented: Boolean = false,
    val hostConsented: Boolean = false,
    val durationSeconds: Long = 0L,
    val retentionDays: Long = 30L,
    val accessLogCount: Long = 0L,
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Complaint(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val reason: String = "",
    val details: String? = null,
    val actionType: String = "REPORT",
    val status: String = ComplaintStatus.OPEN.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: ComplaintStatus get() = safeEnumValue(status, ComplaintStatus.OPEN)
}

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "ADMIN",
    val readFlag: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminAuditLog(
    @DocumentId val id: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetCollection: String = "",
    val targetId: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
