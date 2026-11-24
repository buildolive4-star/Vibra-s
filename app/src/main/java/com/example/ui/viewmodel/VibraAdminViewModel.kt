package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.model.PaymentRecord
import com.example.data.model.PayoutMethod
import com.example.data.model.SalaryStatus
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
import com.example.data.repository.VibraAdminAndHostRepository
import com.example.data.repository.unwrapFirestoreMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VibraAdminViewModel(
    private val adminRepo: VibraAdminAndHostRepository,
    val isAdmin: Boolean
) : ViewModel() {

    private val _adminBanner = MutableStateFlow<Pair<Boolean, String>?>(null)
    val adminBanner: StateFlow<Pair<Boolean, String>?> = _adminBanner.asStateFlow()

    private fun <T> Flow<List<T>>.toAdminState(label: String): StateFlow<UiState<List<T>>> =
        if (!isAdmin) {
            flowOf<UiState<List<T>>>(UiState.Success(emptyList())).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                UiState.Success(emptyList())
            )
        } else {
            this.map<List<T>, UiState<List<T>>> { UiState.Success(it) }
                .catch { error ->
                    if (error is CancellationException && generateSequence<Throwable>(error) { it.cause }
                            .none { it is com.google.firebase.firestore.FirebaseFirestoreException }
                    ) {
                        throw error
                    }
                    val msg = error.unwrapFirestoreMessage("Failed to load $label")
                    Log.w(TAG, "Admin stream error ($label): $msg", error)
                    emit(UiState.Error(msg))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
        }

    val allUsers: StateFlow<UiState<List<UserProfile>>> = adminRepo.observeAllUsersAdmin().toAdminState("users")
    val allWallets: StateFlow<UiState<List<Wallet>>> = adminRepo.observeAllWalletsAdmin().toAdminState("wallets")
    val allCalls: StateFlow<UiState<List<CallSession>>> = adminRepo.observeAllCallsAdmin().toAdminState("calls")
    val allPayments: StateFlow<UiState<List<PaymentRecord>>> = adminRepo.observeAllPaymentsAdmin().toAdminState("payments")
    val allEarnings: StateFlow<UiState<List<HostEarning>>> = adminRepo.observeAllEarningsAdmin().toAdminState("earnings")
    val allSalaries: StateFlow<UiState<List<HostSalary>>> = adminRepo.observeAllSalariesAdmin().toAdminState("salaries")
    val allAttendance: StateFlow<UiState<List<HostAttendance>>> = adminRepo.observeAllAttendanceAdmin().toAdminState("attendance")
    val allRecordings: StateFlow<UiState<List<ConsentedRecording>>> = adminRepo.observeAllRecordingsAdmin().toAdminState("recordings")
    val allComplaints: StateFlow<UiState<List<Complaint>>> = adminRepo.observeAllComplaintsAdmin().toAdminState("complaints")
    val auditLogs: StateFlow<UiState<List<AdminAuditLog>>> = adminRepo.observeAdminAuditLogs().toAdminState("audit logs")

    fun clearBanner() {
        _adminBanner.value = null
    }

    private fun runAdminAction(successMsg: String, block: suspend () -> Result<Unit>) {
        if (!isAdmin) {
            _adminBanner.value = false to "Admin authorization required"
            return
        }
        viewModelScope.launch {
            block()
                .onSuccess { _adminBanner.value = true to successMsg }
                .onFailure { _adminBanner.value = false to (it.localizedMessage ?: "Admin action failed") }
        }
    }

    fun createHost(name: String, bio: String, frameTier: FrameTier, audioRate: Long, videoRate: Long) =
        runAdminAction("Host $name created") { adminRepo.adminCreateHost(name, bio, frameTier, audioRate, videoRate) }

    fun updateHostStatusAndFrame(host: HostProfile, status: HostStatus, frame: FrameTier) =
        runAdminAction("Updated ${host.name} -> ${status.name} / ${frame.name}") {
            adminRepo.adminUpdateHostStatusAndFrame(host, status, frame)
        }

    fun toggleFrameActive(frame: HostFrame, active: Boolean) =
        runAdminAction("${frame.name} ${if (active) "activated" else "paused"}") {
            adminRepo.adminToggleFrameActive(frame, active)
        }

    fun createCustomFrame(name: String, tier: FrameTier, desc: String, accentHex: String, secondaryHex: String) =
        runAdminAction("Created frame $name") {
            adminRepo.adminCreateCustomFrame(name, tier, desc, accentHex, secondaryHex)
        }

    fun updatePricing(settings: AppPricingSettings) =
        runAdminAction("Platform pricing updated") { adminRepo.adminUpdateCallPricing(settings) }

    fun saveRechargePackage(pkgId: String?, title: String, amountInr: Long, coins: Long, bonusCoins: Long, badge: String, active: Boolean) =
        runAdminAction("Recharge package ₹$amountInr saved") {
            adminRepo.adminSaveRechargePackage(pkgId, title, amountInr, coins, bonusCoins, badge, active)
        }

    fun updateGiftPrice(gift: VirtualGift, price: Long, active: Boolean) =
        runAdminAction("Updated ${gift.name} to $price coins") {
            adminRepo.adminUpdateGiftPrice(gift, price, active)
        }

    fun adjustWallet(targetUid: String, delta: Long, reason: String) =
        runAdminAction("Wallet adjusted ($delta coins)") {
            adminRepo.adminAdjustWallet(targetUid, delta, reason)
        }

    fun processSalary(existing: HostSalary?, host: HostProfile, amountInr: Long, method: PayoutMethod, ref: String, status: SalaryStatus) =
        runAdminAction("Salary ${status.name} for ${host.name}") {
            adminRepo.adminCreateOrUpdateSalary(existing, host, amountInr, method, ref, status)
        }

    fun deleteRecording(rec: ConsentedRecording) =
        runAdminAction("Recording deleted") { adminRepo.adminDeleteConsentedRecording(rec) }

    fun resolveComplaint(complaint: Complaint, status: String) =
        runAdminAction("Complaint marked $status") { adminRepo.adminResolveComplaint(complaint, status) }

    fun sendAnnouncement(targetUid: String, title: String, body: String) =
        runAdminAction("Announcement sent") { adminRepo.adminSendAnnouncement(targetUid, title, body) }

    fun updateUserStatus(user: UserProfile, status: String) =
        runAdminAction("${user.displayName} status -> $status") { adminRepo.adminUpdateUserStatus(user, status) }

    fun adjustCallTime(call: CallSession, durationSeconds: Long, billedCoins: Long) =
        runAdminAction("Adjusted call ${call.id}") { adminRepo.adminAdjustCallTime(call, durationSeconds, billedCoins) }

    private companion object {
        const val TAG = "AdminVM"
    }
}
