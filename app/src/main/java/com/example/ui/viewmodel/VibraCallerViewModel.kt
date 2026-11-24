package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.GiftTransaction
import com.example.data.model.HostFrame
import com.example.data.model.HostProfile
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentRecord
import com.example.data.model.RechargePackage
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
import com.example.data.repository.VibraCallAndChatRepository
import com.example.data.repository.VibraPlatformRepository
import com.example.data.repository.unwrapFirestoreMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VibraCallerViewModel(
    private val platformRepo: VibraPlatformRepository,
    private val callChatRepo: VibraCallAndChatRepository,
    val currentUserId: String,
    initialDisplayName: String,
    initialPhone: String?,
    initialCountryCode: String?,
    initialPhotoUrl: String?
) : ViewModel() {

    private val _actionBanner = MutableStateFlow<Pair<Boolean, String>?>(null)
    val actionBanner: StateFlow<Pair<Boolean, String>?> = _actionBanner.asStateFlow()

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    private val _callElapsedSeconds = MutableStateFlow(0L)
    val callElapsedSeconds: StateFlow<Long> = _callElapsedSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _activeChatThread = MutableStateFlow<ChatThread?>(null)
    val activeChatThread: StateFlow<ChatThread?> = _activeChatThread.asStateFlow()

    private var timerJob: Job? = null

    val isAdmin: Boolean = platformRepo.isCurrentUserAdmin()

    init {
        viewModelScope.launch {
            platformRepo.ensureUserInitialized(
                uid = currentUserId,
                displayName = initialDisplayName,
                phoneNumber = initialPhone,
                countryCode = initialCountryCode,
                photoUrl = initialPhotoUrl
            ).onFailure { err ->
                _actionBanner.value = false to (err.localizedMessage ?: "Account initialization failed")
            }
        }
    }

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                if (error is CancellationException && generateSequence<Throwable>(error) { it.cause }
                        .none { it is com.google.firebase.firestore.FirebaseFirestoreException }
                ) {
                    throw error
                }
                val msg = error.unwrapFirestoreMessage("Failed to load $label")
                Log.w(TAG, "Error observing $label: $msg", error)
                emit(UiState.Error(msg))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val userProfile: StateFlow<UiState<UserProfile?>> =
        platformRepo.observeUserProfile(currentUserId).toUiState("profile")

    val hosts: StateFlow<UiState<List<HostProfile>>> =
        platformRepo.observeHosts().toUiState("hosts")

    val hostFrames: StateFlow<UiState<List<HostFrame>>> =
        platformRepo.observeHostFrames().toUiState("host frames")

    val pricingSettings: StateFlow<AppPricingSettings> =
        platformRepo.observePricingSettings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AppPricingSettings()
            )

    val rechargePackages: StateFlow<UiState<List<RechargePackage>>> =
        platformRepo.observeRechargePackages().toUiState("packages")

    val gifts: StateFlow<UiState<List<VirtualGift>>> =
        platformRepo.observeGifts().toUiState("gifts")

    val wallet: StateFlow<Wallet> =
        platformRepo.observeWallet(currentUserId)
            .catch { error ->
                if (error is CancellationException && generateSequence<Throwable>(error) { it.cause }
                        .none { it is com.google.firebase.firestore.FirebaseFirestoreException }
                ) {
                    throw error
                }
                Log.w(TAG, "Wallet stream error", error)
                emit(Wallet(userId = currentUserId))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = Wallet(userId = currentUserId)
            )

    val walletTransactions: StateFlow<UiState<List<WalletTransaction>>> =
        platformRepo.observeWalletTransactions(currentUserId).toUiState("transactions")

    val payments: StateFlow<UiState<List<PaymentRecord>>> =
        platformRepo.observePayments(currentUserId).toUiState("payments")

    val callHistory: StateFlow<UiState<List<CallSession>>> =
        callChatRepo.observeCallsForUser(currentUserId).toUiState("call history")

    val chatThreads: StateFlow<UiState<List<ChatThread>>> =
        callChatRepo.observeChatsForUser(currentUserId).toUiState("chats")

    val giftHistory: StateFlow<UiState<List<GiftTransaction>>> =
        callChatRepo.observeGiftTransactionsForUser(currentUserId).toUiState("gift history")

    val notifications: StateFlow<UiState<List<AppNotification>>> =
        platformRepo.observeNotifications(currentUserId).toUiState("notifications")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<UiState<List<ChatMessage>>> =
        _activeChatThread.flatMapLatest { thread ->
            if (thread == null) {
                flowOf(emptyList())
            } else {
                callChatRepo.observeMessages(thread.id, currentUserId)
            }
        }.toUiState("messages")

    fun clearBanner() {
        _actionBanner.value = null
    }

    fun showBanner(success: Boolean, message: String) {
        _actionBanner.value = success to message
    }

    fun updateProfile(displayName: String, phone: String?, countryCode: String?, language: String) {
        viewModelScope.launch {
            platformRepo.updateUserProfile(currentUserId, displayName, phone, countryCode, language)
                .onSuccess { _actionBanner.value = true to "Profile saved" }
                .onFailure { _actionBanner.value = false to (it.localizedMessage ?: "Update failed") }
        }
    }

    fun rechargeWallet(pkg: RechargePackage, gateway: PaymentGateway, upiOrRef: String) {
        viewModelScope.launch {
            platformRepo.executeVerifiedRecharge(pkg, gateway, upiOrRef)
                .onSuccess { rec ->
                    _actionBanner.value = true to "Verified ₹${rec.amountInr}: +${rec.coinsCredited} coins added!"
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Payment verification failed")
                }
        }
    }

    fun startCall(host: HostProfile, callType: CallType, onStarted: (CallSession) -> Unit) {
        val callerName = (userProfile.value as? UiState.Success)?.data?.displayName ?: "Caller"
        viewModelScope.launch {
            callChatRepo.startCallSession(callerName, host, callType, pricingSettings.value)
                .onSuccess { session ->
                    _activeCall.value = session
                    _callElapsedSeconds.value = 0L
                    _isMuted.value = false
                    _isSpeakerOn.value = true
                    _isCameraOn.value = (callType == CallType.VIDEO)
                    startCallTimer(session)
                    onStarted(session)
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Unable to start call")
                }
        }
    }

    private fun startCallTimer(session: CallSession) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_activeCall.value?.id == session.id) {
                delay(1000L)
                val next = _callElapsedSeconds.value + 1L
                _callElapsedSeconds.value = next
                // Live remaining balance check: auto-end if wallet coins run out
                val currentBal = wallet.value.balanceCoins
                val estimatedCost = ((next / 60L) + 1L) * session.ratePerMin
                if (currentBal <= 0L || (next > 60L && estimatedCost > currentBal)) {
                    endActiveCall(CallStatus.COMPLETED)
                    _actionBanner.value = false to "Call ended: wallet balance reached limit."
                    break
                }
            }
        }
    }

    fun toggleMute() { _isMuted.value = !_isMuted.value }
    fun toggleSpeaker() { _isSpeakerOn.value = !_isSpeakerOn.value }
    fun toggleCamera() { _isCameraOn.value = !_isCameraOn.value }
    fun switchCamera() { _isFrontCamera.value = !_isFrontCamera.value }

    fun toggleRecordingConsent(callerConsent: Boolean, hostConsent: Boolean) {
        val call = _activeCall.value ?: return
        viewModelScope.launch {
            callChatRepo.updateRecordingConsent(call, callerConsent, hostConsent)
                .onSuccess {
                    _activeCall.value = call.copy(
                        callerConsentRecording = callerConsent,
                        hostConsentRecording = hostConsent,
                        recordingActive = callerConsent && hostConsent
                    )
                    _actionBanner.value = true to if (callerConsent && hostConsent) {
                        "Dual-consent recording active"
                    } else {
                        "Recording consent updated (requires both participants)"
                    }
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Consent update failed")
                }
        }
    }

    fun endActiveCall(status: CallStatus = CallStatus.COMPLETED, onFinished: () -> Unit = {}) {
        val call = _activeCall.value ?: return
        val elapsed = _callElapsedSeconds.value
        timerJob?.cancel()
        _activeCall.value = null
        viewModelScope.launch {
            callChatRepo.settleAndEndCall(call, elapsed, pricingSettings.value, status)
                .onSuccess { settled ->
                    _actionBanner.value = true to "Call settled: ${settled.durationSeconds}s • ${settled.billedCoins} coins"
                    onFinished()
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Call settlement failed")
                    onFinished()
                }
        }
    }

    fun sendGift(host: HostProfile, gift: VirtualGift, callId: String? = null) {
        val senderName = (userProfile.value as? UiState.Success)?.data?.displayName ?: "Caller"
        viewModelScope.launch {
            callChatRepo.sendVirtualGift(senderName, host, gift, pricingSettings.value, callId)
                .onSuccess {
                    _actionBanner.value = true to "Sent ${gift.name} (${gift.coinPrice} coins) to ${host.name}!"
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Gift sending failed")
                }
        }
    }

    fun openChatWithHost(host: HostProfile, onOpened: (ChatThread) -> Unit) {
        val callerName = (userProfile.value as? UiState.Success)?.data?.displayName ?: "Caller"
        viewModelScope.launch {
            callChatRepo.getOrCreateChatThread(callerName, host)
                .onSuccess { thread ->
                    _activeChatThread.value = thread
                    onOpened(thread)
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Unable to open chat")
                }
        }
    }

    fun selectChatThread(thread: ChatThread) {
        _activeChatThread.value = thread
    }

    fun clearActiveChatThread() {
        _activeChatThread.value = null
    }

    fun sendMessage(text: String, messageType: String = "TEXT", mediaLabel: String? = null, giftId: String? = null) {
        val thread = _activeChatThread.value ?: return
        val senderName = (userProfile.value as? UiState.Success)?.data?.displayName ?: "Caller"
        viewModelScope.launch {
            callChatRepo.sendChatMessage(thread, senderName, text, messageType, mediaLabel, giftId)
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Message failed")
                }
        }
    }

    fun submitReportOrBlock(targetId: String, targetName: String, reason: String, details: String?, actionType: String) {
        viewModelScope.launch {
            callChatRepo.submitComplaint(targetId, targetName, reason, details, actionType)
                .onSuccess {
                    _actionBanner.value = true to if (actionType == "BLOCK") {
                        "$targetName blocked and logged for safety review"
                    } else {
                        "Report submitted to Vibra Safety Team"
                    }
                }
                .onFailure {
                    _actionBanner.value = false to (it.localizedMessage ?: "Failed to submit report")
                }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            platformRepo.markNotificationRead(notificationId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val TAG = "CallerVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
