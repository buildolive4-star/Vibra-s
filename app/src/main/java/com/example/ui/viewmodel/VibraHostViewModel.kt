package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.HostAttendance
import com.example.data.model.HostEarning
import com.example.data.model.HostProfile
import com.example.data.model.HostSalary
import com.example.data.model.OnlineStatus
import com.example.data.model.UiState
import com.example.data.repository.VibraAdminAndHostRepository
import com.example.data.repository.unwrapFirestoreMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VibraHostViewModel(
    private val adminHostRepo: VibraAdminAndHostRepository,
    val currentUserId: String
) : ViewModel() {

    private val _hostBanner = MutableStateFlow<Pair<Boolean, String>?>(null)
    val hostBanner: StateFlow<Pair<Boolean, String>?> = _hostBanner.asStateFlow()

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
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val hostEarnings: StateFlow<UiState<List<HostEarning>>> =
        adminHostRepo.observeHostEarnings(currentUserId).toUiState("host earnings")

    val hostAttendance: StateFlow<UiState<List<HostAttendance>>> =
        adminHostRepo.observeHostAttendance(currentUserId).toUiState("attendance")

    val hostSalaries: StateFlow<UiState<List<HostSalary>>> =
        adminHostRepo.observeHostSalaries(currentUserId).toUiState("salaries")

    fun clearBanner() {
        _hostBanner.value = null
    }

    fun applyAsHost(name: String, bio: String) {
        viewModelScope.launch {
            adminHostRepo.applyOrRegisterAsHost(name, bio)
                .onSuccess { _hostBanner.value = true to "Host application submitted for Admin approval!" }
                .onFailure { _hostBanner.value = false to (it.localizedMessage ?: "Application failed") }
        }
    }

    fun updateAvailability(
        host: HostProfile,
        onlineStatus: OnlineStatus,
        bio: String,
        audioAvailable: Boolean,
        videoAvailable: Boolean
    ) {
        viewModelScope.launch {
            adminHostRepo.updateHostAvailabilityAndBio(host, onlineStatus, bio, audioAvailable, videoAvailable)
                .onSuccess { _hostBanner.value = true to "Availability updated (${onlineStatus.name})" }
                .onFailure { _hostBanner.value = false to (it.localizedMessage ?: "Failed to update availability") }
        }
    }

    fun toggleShift(host: HostProfile, checkIn: Boolean) {
        viewModelScope.launch {
            adminHostRepo.toggleHostAttendanceShift(host, checkIn)
                .onSuccess { _hostBanner.value = true to if (checkIn) "Checked in for shift" else "Checked out" }
                .onFailure { _hostBanner.value = false to (it.localizedMessage ?: "Attendance failed") }
        }
    }

    private companion object {
        const val TAG = "HostVM"
    }
}
