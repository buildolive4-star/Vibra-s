package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.FrameTier
import com.example.data.model.OnlineStatus
import com.example.ui.theme.AmberBusy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedGlass
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.FrameDiamondPrimary
import com.example.ui.theme.FrameDiamondSecondary
import com.example.ui.theme.FrameGoldPrimary
import com.example.ui.theme.FrameGoldSecondary
import com.example.ui.theme.FrameNewHostPrimary
import com.example.ui.theme.FrameNewHostSecondary
import com.example.ui.theme.FramePlatinumPrimary
import com.example.ui.theme.FramePlatinumSecondary
import com.example.ui.theme.FrameSilverPrimary
import com.example.ui.theme.FrameSilverSecondary
import com.example.ui.theme.FrameStarPrimary
import com.example.ui.theme.FrameStarSecondary
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MidnightPurple
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.SubtleSlate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VibraCosmicBackground(
    modifier: Modifier = Modifier,
    particlesEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightPurple, DarkNavy, Color(0xFF060712))
                )
            )
            .drawBehind {
                // Soft ambient radial orbs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricViolet.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.14f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.18f, size.height * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.42f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.88f, size.height * 0.42f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoyalBlue.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.85f),
                        radius = size.width * 0.60f
                    ),
                    radius = size.width * 0.60f,
                    center = Offset(size.width * 0.25f, size.height * 0.85f)
                )

                if (particlesEnabled) {
                    val starPositions = listOf(
                        Offset(0.12f, 0.09f), Offset(0.82f, 0.13f), Offset(0.35f, 0.27f),
                        Offset(0.71f, 0.34f), Offset(0.18f, 0.52f), Offset(0.90f, 0.63f),
                        Offset(0.45f, 0.78f), Offset(0.66f, 0.88f)
                    )
                    starPositions.forEachIndexed { idx, rel ->
                        val dy = sin(phase + idx * 0.9f) * 10f
                        val cx = size.width * rel.x
                        val cy = size.height * rel.y + dy
                        val color = if (idx % 3 == 0) GoldenGlow else if (idx % 2 == 0) ElectricViolet else NeonPink
                        drawCircle(
                            color = color.copy(alpha = 0.35f + 0.25f * sin(phase + idx)),
                            radius = (2.5f + (idx % 3) * 1.4f),
                            center = Offset(cx, cy)
                        )
                    }
                }
            },
        content = content
    )
}

@Composable
fun Glass3DCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColors: List<Color> = listOf(ElectricViolet.copy(alpha = 0.55f), NeonPink.copy(alpha = 0.32f), RoyalBlue.copy(alpha = 0.40f)),
    containerColor: Color = DeepSurface.copy(alpha = 0.82f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .shadow(
            elevation = 14.dp,
            shape = shape,
            ambientColor = ElectricViolet.copy(alpha = 0.28f),
            spotColor = NeonPink.copy(alpha = 0.25f)
        )
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    ElevatedGlass.copy(alpha = 0.88f),
                    containerColor
                )
            )
        )
        .border(
            width = 1.2.dp,
            brush = Brush.linearGradient(borderColors),
            shape = shape
        )
        .let { mod ->
            if (onClick != null) mod.clickable(onClick = onClick) else mod
        }

    Box(modifier = baseModifier, content = content)
}

@Composable
fun NeonGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(ElectricViolet, NeonPink),
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = colors.first().copy(alpha = 0.45f),
                spotColor = colors.last().copy(alpha = 0.45f)
            )
            .background(
                brush = Brush.horizontalGradient(
                    if (enabled) colors else listOf(SubtleSlate.copy(alpha = 0.4f), SubtleSlate.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = SoftWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun avatarGradientForKey(key: String?): List<Color> = when (key) {
    "aurora" -> listOf(Color(0xFF8B5CFF), Color(0xFFFF4FA3))
    "velvet" -> listOf(Color(0xFFFF4FA3), Color(0xFF4169FF))
    "prism" -> listOf(Color(0xFF4CE5FF), Color(0xFF8B5CFF))
    "sapphire" -> listOf(Color(0xFF4169FF), Color(0xFF2EE59D))
    "rose" -> listOf(Color(0xFFFF4D6D), Color(0xFFFFD36E))
    else -> listOf(Color(0xFFFFD36E), Color(0xFF8B5CFF))
}

@Composable
fun Host3DFrameAvatar(
    name: String,
    avatarKey: String?,
    frameTier: FrameTier,
    onlineStatus: OnlineStatus?,
    size: Dp = 74.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "frame_${frameTier.name}")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (frameTier) {
                    FrameTier.STAR -> 4500
                    FrameTier.DIAMOND -> 5500
                    FrameTier.GOLD -> 6500
                    FrameTier.PLATINUM -> 7500
                    else -> 9500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame_rot"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "frame_pulse"
    )

    val (primaryColor, secondaryColor) = when (frameTier) {
        FrameTier.NEW_HOST -> FrameNewHostPrimary to FrameNewHostSecondary
        FrameTier.SILVER -> FrameSilverPrimary to FrameSilverSecondary
        FrameTier.GOLD -> FrameGoldPrimary to FrameGoldSecondary
        FrameTier.DIAMOND -> FrameDiamondPrimary to FrameDiamondSecondary
        FrameTier.PLATINUM -> FramePlatinumPrimary to FramePlatinumSecondary
        FrameTier.STAR -> FrameStarPrimary to FrameStarSecondary
    }

    val avatarColors = avatarGradientForKey(avatarKey)
    val initials = name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "V" }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = this.size.minDimension * 0.085f
            val r = (this.size.minDimension / 2f) - strokeW

            // Outer ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.34f * pulse), Color.Transparent),
                    radius = this.size.minDimension * 0.56f
                )
            )

            // Rotating 3D metallic / prismatic border
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor,
                            secondaryColor,
                            SoftWhite.copy(alpha = 0.9f),
                            primaryColor,
                            secondaryColor
                        )
                    ),
                    radius = r,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // Tier-specific particle / facet accents
            when (frameTier) {
                FrameTier.GOLD, FrameTier.STAR -> {
                    for (i in 0 until 6) {
                        val angle = ((rotation + i * 60f) * PI / 180.0).toFloat()
                        val px = center.x + (r + strokeW * 0.35f) * cos(angle)
                        val py = center.y + (r + strokeW * 0.35f) * sin(angle)
                        drawCircle(
                            color = if (i % 2 == 0) GoldenGlow else secondaryColor,
                            radius = strokeW * 0.42f,
                            center = Offset(px, py)
                        )
                    }
                }
                FrameTier.DIAMOND -> {
                    for (i in 0 until 4) {
                        val angle = ((rotation * 0.7f + i * 90f) * PI / 180.0).toFloat()
                        val px = center.x + (r + strokeW * 0.25f) * cos(angle)
                        val py = center.y + (r + strokeW * 0.25f) * sin(angle)
                        val dSize = strokeW * 0.75f
                        val diamond = Path().apply {
                            moveTo(px, py - dSize)
                            lineTo(px + dSize, py)
                            lineTo(px, py + dSize)
                            lineTo(px - dSize, py)
                            close()
                        }
                        drawPath(diamond, color = SoftWhite.copy(alpha = 0.92f))
                    }
                }
                else -> Unit
            }
        }

        // Inner circular portrait sphere
        Box(
            modifier = Modifier
                .size(size * 0.77f)
                .clip(CircleShape)
                .background(Brush.linearGradient(avatarColors))
                .border(1.5.dp, DarkNavy.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = if (size >= 90.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
        }

        // Top VIP / Star badge for STAR or DIAMOND frames
        if (frameTier == FrameTier.STAR || frameTier == FrameTier.DIAMOND) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                    .border(1.dp, DarkNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (frameTier == FrameTier.STAR) Icons.Rounded.Star else Icons.Rounded.AutoAwesome,
                    contentDescription = frameTier.label,
                    tint = DarkNavy,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Online / Busy / Offline indicator dot
        if (onlineStatus != null) {
            val dotColor = when (onlineStatus) {
                OnlineStatus.ONLINE -> EmeraldOnline
                OnlineStatus.BUSY -> AmberBusy
                OnlineStatus.OFFLINE -> SubtleSlate
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(2.5.dp, DarkNavy, CircleShape)
            )
        }
    }
}

@Composable
fun VibraAudioWaveform(
    isActive: Boolean,
    amplitudeLevel: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = modifier.height(72.dp).fillMaxWidth()) {
        val barCount = 28
        val spacing = size.width / (barCount * 1.8f)
        val barWidth = spacing * 0.9f
        val startX = (size.width - (barCount * (barWidth + spacing))) / 2f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val normPos = (i.toFloat() / barCount)
            val envelope = sin(normPos * PI).toFloat()
            val wave = if (isActive) {
                0.28f + 0.72f * kotlin.math.abs(sin(phase * 1.4f + i * 0.42f)) * (0.55f + 0.45f * amplitudeLevel.coerceIn(0f, 1f))
            } else {
                0.15f
            }
            val barH = (size.height * 0.85f * envelope * wave).coerceAtLeast(8f)
            val color = when {
                i % 3 == 0 -> NeonPink
                i % 2 == 0 -> ElectricViolet
                else -> RoyalBlue
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(startX + i * (barWidth + spacing), centerY - barH / 2f),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun AnimatedStatLineChart(
    values: List<Float>,
    labels: List<String>,
    primaryColor: Color = ElectricViolet,
    secondaryColor: Color = NeonPink,
    modifier: Modifier = Modifier
) {
    val safeValues = if (values.isEmpty()) listOf(10f, 24f, 18f, 36f, 42f, 58f) else values
    val maxVal = (safeValues.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            val count = safeValues.size
            val stepX = if (count > 1) size.width / (count - 1) else size.width

            // Grid lines
            for (g in 0..3) {
                val y = size.height * g / 3f
                drawLine(
                    color = SoftWhite.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            val points = safeValues.mapIndexed { idx, v ->
                Offset(
                    x = idx * stepX,
                    y = size.height - ((v / maxVal) * size.height * 0.85f) - size.height * 0.06f
                )
            }

            val linePath = Path().apply {
                points.forEachIndexed { idx, pt ->
                    if (idx == 0) moveTo(pt.x, pt.y)
                    else {
                        val prev = points[idx - 1]
                        val ctrlX = (prev.x + pt.x) / 2f
                        cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.38f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
            points.forEach { pt ->
                drawCircle(color = DarkNavy, radius = 6.5f, center = pt)
                drawCircle(color = GoldenGlow, radius = 4f, center = pt)
            }
        }
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { lbl ->
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedLavender
                    )
                }
            }
        }
    }
}

@Composable
fun SecureScreenEffect(enabled: Boolean = true) {
    val context = LocalContext.current
    DisposableEffect(enabled, context) {
        val activity = context.findActivity()
        if (enabled && activity != null) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (enabled && activity != null) {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

@Composable
fun PrivacyWatermarkBadge(
    userId: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkNavy.copy(alpha = 0.65f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = "Protected session",
                tint = EmeraldOnline,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ID ${userId.take(8)} • FLAG_SECURE",
                style = MaterialTheme.typography.labelSmall,
                color = MutedLavender
            )
        }
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
