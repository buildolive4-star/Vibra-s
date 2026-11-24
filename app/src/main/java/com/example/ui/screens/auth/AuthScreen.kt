package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.repository.VibraAuthRepository
import com.example.ui.components.Glass3DCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.VibraCosmicBackground
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite

@Composable
fun AuthScreen(
    onAuthSuccess: (phoneNumber: String?, countryCode: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val countryCodes = remember { listOf("+91" to "IN", "+1" to "US", "+44" to "UK", "+971" to "AE", "+65" to "SG") }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    var countryMenuOpen by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        VibraAuthRepository.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                onAuthSuccess(phoneNumber.takeIf { it.isNotBlank() }, selectedCountryCode)
            },
            onUnauthenticated = {},
            scope = scope
        )
    }

    VibraCosmicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Glass3DCard(
                cornerRadius = 26.dp,
                modifier = Modifier.fillMaxWidth().height(164.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vibra_hero_crystals),
                    contentDescription = "Vibra Luxury Crystals",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(DarkNavy.copy(alpha = 0.25f), DarkNavy.copy(alpha = 0.85f))
                            )
                        )
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)
                ) {
                    Text(
                        text = "Welcome to VIBRA",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Private 3D Audio, HD Video & Luxury Social Calling",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Glass3DCard(
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = GoldenGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Account & Mobile Verification",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftWhite
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Link your mobile number (optional) for caller ID and verify securely with Google Sign-In.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Surface(
                                color = DarkNavy.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, ElectricViolet.copy(0.5f), RoundedCornerShape(14.dp))
                                    .clickable { countryMenuOpen = true }
                                    .padding(horizontal = 14.dp)
                                    .testTag("country_code_picker")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedCountryCode,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoftWhite
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = countryMenuOpen,
                                onDismissRequest = { countryMenuOpen = false }
                            ) {
                                countryCodes.forEach { (code, region) ->
                                    DropdownMenuItem(
                                        text = { Text("$code ($region)") },
                                        onClick = {
                                            selectedCountryCode = code
                                            countryMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                phoneNumber = input.filter { it.isDigit() }.take(15)
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("98765 43210") },
                            leadingIcon = {
                                Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = ElectricViolet)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPink,
                                unfocusedBorderColor = ElectricViolet.copy(0.4f),
                                focusedTextColor = SoftWhite,
                                unfocusedTextColor = SoftWhite,
                                focusedLabelColor = NeonPink,
                                unfocusedLabelColor = MutedLavender
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("phone_input")
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { termsAccepted = !termsAccepted }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ElectricViolet,
                                checkmarkColor = SoftWhite
                            ),
                            modifier = Modifier.testTag("terms_checkbox")
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "I accept the Vibra Terms of Service, Privacy Policy & 18+ Community Guidelines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedLavender
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = CrimsonAlert.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, CrimsonAlert.copy(0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftWhite,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonPink,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else {
                        NeonGradientButton(
                            text = stringResource(R.string.sign_in_google_cta),
                            enabled = termsAccepted,
                            colors = listOf(ElectricViolet, NeonPink, RoyalBlue),
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                VibraAuthRepository.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess(phoneNumber.takeIf { it.isNotBlank() }, selectedCountryCode)
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button")
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = EmeraldOnline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Secured by Google Credential Manager & Zero-Trust Rules",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedLavender
                        )
                    }
                }
            }
        }
    }
}
