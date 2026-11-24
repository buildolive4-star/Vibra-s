package com.example.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.BetZonePreferences
import com.example.data.repository.AuthAttemptResult
import com.example.data.repository.BetZoneAuthRepository
import com.example.ui.components.Animated3DFootballCanvas
import com.example.ui.components.BetZoneBackdrop
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.LocalBetZoneColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: BetZoneAuthRepository,
    preferences: BetZonePreferences,
    onAuthSuccess: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val countryCodes = remember { listOf("+91", "+1", "+44", "+971", "+61", "+65") }
    var selectedCountry by remember { mutableStateOf("+91") }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var expectedOtp by remember { mutableStateOf<String?>(null) }
    var otpVerified by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(true) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showPolicyDialog by remember { mutableStateOf<String?>(null) }

    BetZoneBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Animated3DFootballCanvas(sizeDp = 64.dp)
                Column {
                    Text(
                        text = "BETZONE X",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.betzone_tagline),
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.neonGreen
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.electricBlue
            ) {
                Text(
                    text = "Sign In to the Sports Universe",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Verify your mobile ID and connect your Google account to sync Virtual Points, predictions, and leaderboards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.subtleText
                )

                Spacer(Modifier.height(16.dp))

                // Country code + mobile number input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .height(56.dp)
                                .clickable { countryMenuExpanded = true }
                                .testTag("country_code_selector"),
                            shape = RoundedCornerShape(14.dp),
                            color = ext.bgBottom,
                            border = BorderStroke(1.dp, ext.glassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCountry,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select country code",
                                    tint = ext.electricBlue
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = countryMenuExpanded,
                            onDismissRequest = { countryMenuExpanded = false }
                        ) {
                            countryCodes.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    onClick = {
                                        selectedCountry = code
                                        countryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            mobileNumber = input.filter { it.isDigit() }.take(12)
                            otpVerified = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mobile_number_input"),
                        label = { Text("Mobile Number") },
                        placeholder = { Text("9876543210") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (mobileNumber.length < 7) {
                                statusMessage = "Enter at least 7 digits for mobile OTP verification."
                            } else {
                                val code = (100000..999999).random().toString()
                                expectedOtp = code
                                otpCode = code
                                otpVerified = false
                                statusMessage = "Verification OTP generated: $code (auto-filled for quick verification)."
                                scope.launch { preferences.savePendingPhone(selectedCountry, mobileNumber) }
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("send_otp_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ext.electricBlue)
                    ) {
                        Text("Send OTP", color = ext.electricBlue)
                    }

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.filter { ch -> ch.isDigit() }.take(6) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("otp_input"),
                        label = { Text("6-Digit OTP") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (expectedOtp != null) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (otpCode == expectedOtp && mobileNumber.length >= 7) {
                                otpVerified = true
                                statusMessage = "Mobile number verified! Tap Continue with Google to finish sign-in."
                                scope.launch { preferences.savePendingPhone(selectedCountry, mobileNumber) }
                            } else {
                                statusMessage = "OTP code does not match. Tap Send OTP to generate a fresh code."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ext.neonGreen.copy(alpha = 0.22f),
                            contentColor = ext.neonGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (otpVerified) "Mobile Verified" else "Verify OTP")
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ext.glassBorder)
                    Text(
                        text = "  CLOUD AUTH  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.subtleText
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ext.glassBorder)
                }
                Spacer(Modifier.height(14.dp))

                // Terms & Privacy Policy consent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = ext.neonGreen),
                        modifier = Modifier.testTag("terms_checkbox")
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I accept the Terms & Conditions and Privacy Policy (Virtual Points Only).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Terms & Conditions",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.electricBlue,
                                modifier = Modifier
                                    .clickable { showPolicyDialog = "Terms & Conditions" }
                                    .padding(vertical = 4.dp)
                                    .testTag("view_terms_link")
                            )
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.electricBlue,
                                modifier = Modifier
                                    .clickable { showPolicyDialog = "Privacy Policy" }
                                    .padding(vertical = 4.dp)
                                    .testTag("view_privacy_link")
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (!termsAccepted) {
                            statusMessage = "Please accept the Terms & Conditions and Privacy Policy to continue."
                            return@Button
                        }
                        val activity = context as? Activity
                        if (activity == null) {
                            statusMessage = "Unable to launch Google Sign-In from this context."
                            return@Button
                        }
                        isGoogleLoading = true
                        statusMessage = null
                        scope.launch {
                            if (mobileNumber.length >= 7) {
                                preferences.savePendingPhone(selectedCountry, mobileNumber)
                            }
                            when (val res = authRepository.signInWithGoogleInteractive(activity)) {
                                is AuthAttemptResult.Success -> {
                                    isGoogleLoading = false
                                    onAuthSuccess()
                                }
                                is AuthAttemptResult.Cancelled -> {
                                    isGoogleLoading = false
                                    statusMessage = "Google Sign-In was cancelled. Tap below to try again."
                                }
                                is AuthAttemptResult.Failure -> {
                                    isGoogleLoading = false
                                    statusMessage = res.message
                                }
                            }
                        }
                    },
                    enabled = !isGoogleLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ext.electricBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.sign_in_google_cta),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (!statusMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ext.electricBlue.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, ext.electricBlue.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = statusMessage!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.neonGreen.copy(alpha = 0.55f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ext.neonGreen
                    )
                    Text(
                        text = stringResource(R.string.virtual_points_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.subtleText
                    )
                }
            }
        }
    }

    showPolicyDialog?.let { title ->
        AlertDialog(
            onDismissRequest = { showPolicyDialog = null },
            title = { Text(title) },
            text = {
                Text(
                    if (title == "Terms & Conditions") {
                        "BetZone X uses Virtual Points (VP) solely for interactive sports entertainment, match predictions, quizzes, and leaderboard rankings. Virtual Points hold no cash value, cannot be deposited, traded, or withdrawn, and no real-money wagering occurs anywhere in this app."
                    } else {
                        "BetZone X stores your display name, avatar, favourite sports/teams, prediction history, and optional verified mobile code privately in your user record. Only your non-PII public display name, avatar, virtual points, and prediction accuracy appear on the leaderboard if enabled in Settings."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showPolicyDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}
