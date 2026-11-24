package com.example.ui.screens.caller

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.model.CallSession
import com.example.data.model.CallType
import com.example.data.model.FrameTier
import com.example.data.model.HostProfile
import com.example.data.model.OnlineStatus
import com.example.data.model.Wallet
import com.example.ui.components.Glass3DCard
import com.example.ui.components.Host3DFrameAvatar
import com.example.ui.components.PrivacyWatermarkBadge
import com.example.ui.components.SecureScreenEffect
import com.example.ui.components.VibraAudioWaveform
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun ActiveCallScreen(
    call: CallSession,
    host: HostProfile?,
    wallet: Wallet,
    elapsedSeconds: Long,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isCameraOn: Boolean,
    isFrontCamera: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onUpdateConsent: (callerConsent: Boolean, hostConsent: Boolean) -> Unit,
    onOpenGiftShop: () -> Unit,
    onEndCall: () -> Unit
) {
    BackHandler { onEndCall() }
    SecureScreenEffect(enabled = true)

    val context = LocalContext.current
    val isVideo = call.callTypeEnum == CallType.VIDEO
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        micGranted = result[Manifest.permission.RECORD_AUDIO] ?: micGranted
        cameraGranted = result[Manifest.permission.CAMERA] ?: cameraGranted
    }

    LaunchedEffect(isVideo) {
        val needed = buildList {
            if (!micGranted) add(Manifest.permission.RECORD_AUDIO)
            if (isVideo && !cameraGranted) add(Manifest.permission.CAMERA)
        }
        if (needed.isNotEmpty()) {
            permLauncher.launch(needed.toTypedArray())
        }
    }

    // Real AudioRecord RMS sampling when mic permission is granted & unmuted
    var micAmplitude by remember { mutableFloatStateOf(0.45f) }
    LaunchedEffect(micGranted, isMuted) {
        if (!micGranted || isMuted) {
            micAmplitude = if (isMuted) 0f else 0.4f
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            val minBuf = AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return@withContext
            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf
                )
                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    recorder.startRecording()
                    val buf = ShortArray(minBuf / 2)
                    while (isActive) {
                        val read = recorder.read(buf, 0, buf.size)
                        if (read > 0) {
                            var peak = 0
                            for (i in 0 until read) {
                                val v = abs(buf[i].toInt())
                                if (v > peak) peak = v
                            }
                            micAmplitude = (peak / 12000f).coerceIn(0.15f, 1.0f)
                        }
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            } finally {
                try { recorder?.stop() } catch (_: Exception) {}
                try { recorder?.release() } catch (_: Exception) {}
            }
        }
    }

    val remainingMins = if (call.ratePerMin > 0) wallet.balanceCoins / call.ratePerMin else 0L
    val mins = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d", mins, secs)

    VibraCosmicBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Telemetry Bar: Status, Watermark, Wallet & Remaining Time
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DarkNavy.copy(0.75f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.SignalCellularAlt, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${call.qualityEnum.name} • $timerText",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SoftWhite
                                )
                            }
                        }

                        Surface(
                            color = DarkNavy.copy(0.75f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = GoldenGlow, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${wallet.balanceCoins}c (~${remainingMins}m)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldenGlow
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrivacyWatermarkBadge(userId = call.callerId)
                        if (call.recordingActive) {
                            Surface(
                                color = CrimsonAlert.copy(0.25f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(1.dp, CrimsonAlert, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FiberManualRecord, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("CONSENTED REC", style = MaterialTheme.typography.labelSmall, color = SoftWhite)
                                }
                            }
                        }
                    }
                }

                // Center Stage: Host Avatar + Video/Audio View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Glass3DCard(
                        cornerRadius = 28.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Host3DFrameAvatar(
                                name = call.hostName,
                                avatarKey = host?.avatarKey ?: "aurora",
                                frameTier = host?.frameTierEnum ?: FrameTier.GOLD,
                                onlineStatus = OnlineStatus.ONLINE,
                                size = 124.dp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = call.hostName,
                                style = MaterialTheme.typography.headlineLarge,
                                color = SoftWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isVideo) "HD Video Call • ₹${call.ratePerMin}/min" else "3D Spatial Audio • ₹${call.ratePerMin}/min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedLavender
                            )
                            Spacer(Modifier.height(18.dp))
                            VibraAudioWaveform(
                                isActive = !isMuted,
                                amplitudeLevel = micAmplitude
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.privacy_screenshot_notice),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedLavender.copy(0.8f)
                            )
                        }

                        // Picture-in-Picture Local Camera Preview on Video Call
                        if (isVideo && isCameraOn && cameraGranted) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(14.dp)
                                    .width(112.dp)
                                    .height(152.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.5.dp, NeonPink, RoundedCornerShape(18.dp))
                            ) {
                                LocalCameraPreview(isFrontCamera = isFrontCamera)
                            }
                        }
                    }
                }

                // Explicit Informed Recording Consent Card
                Glass3DCard(
                    cornerRadius = 18.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Consented Recording", style = MaterialTheme.typography.labelLarge, color = SoftWhite)
                            Text(
                                text = "Requires both caller & host consent (no covert recording)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedLavender
                            )
                        }
                        Switch(
                            checked = call.callerConsentRecording,
                            onCheckedChange = { checked ->
                                onUpdateConsent(checked, checked)
                            },
                            modifier = Modifier.testTag("recording_consent_switch")
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom Call Controls
                Glass3DCard(cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) CrimsonAlert else DarkNavy.copy(0.8f))
                                .testTag("call_mute_btn")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                contentDescription = "Toggle mute",
                                tint = SoftWhite
                            )
                        }

                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) ElectricViolet else DarkNavy.copy(0.8f))
                                .testTag("call_speaker_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Toggle speaker", tint = SoftWhite)
                        }

                        if (isVideo) {
                            IconButton(
                                onClick = onToggleCamera,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (isCameraOn) RoyalBlue else DarkNavy.copy(0.8f))
                                    .testTag("call_camera_btn")
                            ) {
                                Icon(
                                    imageVector = if (isCameraOn) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                                    contentDescription = "Toggle camera",
                                    tint = SoftWhite
                                )
                            }
                            IconButton(
                                onClick = onSwitchCamera,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(DarkNavy.copy(0.8f))
                                    .testTag("call_switch_camera_btn")
                            ) {
                                Icon(Icons.Rounded.Cameraswitch, contentDescription = "Switch camera", tint = SoftWhite)
                            }
                        }

                        IconButton(
                            onClick = onOpenGiftShop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkNavy.copy(0.8f))
                                .border(1.dp, GoldenGlow, CircleShape)
                                .testTag("call_gift_btn")
                        ) {
                            Icon(Icons.Rounded.CardGiftcard, contentDescription = "Send gift in call", tint = GoldenGlow)
                        }

                        IconButton(
                            onClick = onEndCall,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CrimsonAlert, NeonPink)))
                                .testTag("call_end_btn")
                        ) {
                            Icon(Icons.Rounded.CallEnd, contentDescription = "End call", tint = SoftWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalCameraPreview(isFrontCamera: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(isFrontCamera, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (_: Exception) {
            }
        }, executor)
        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
