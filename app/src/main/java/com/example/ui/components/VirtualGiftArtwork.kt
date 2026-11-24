package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanCrystal
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite

@Composable
fun VirtualGift3DArtwork(
    artKey: String,
    accentHex: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "gift_$artKey")
    val floatOffset by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_float"
    )

    val primary = parseHexSafe(accentHex, NeonPink)

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = floatOffset },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Ambient 3D halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = w * 0.52f
                )
            )

            when (artKey) {
                "rose" -> {
                    // Stem & leaves
                    drawLine(
                        color = Color(0xFF2EE59D),
                        start = Offset(cx, cy + h * 0.05f),
                        end = Offset(cx, h * 0.88f),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round
                    )
                    drawOval(
                        color = Color(0xFF1FA671),
                        topLeft = Offset(cx - w * 0.22f, cy + h * 0.14f),
                        size = Size(w * 0.22f, h * 0.11f)
                    )
                    // Layered 3D petals
                    drawCircle(
                        brush = Brush.radialGradient(listOf(NeonPink, Color(0xFFB30047))),
                        radius = w * 0.26f,
                        center = Offset(cx, cy - h * 0.10f)
                    )
                    drawCircle(
                        brush = Brush.linearGradient(listOf(Color(0xFFFF8BC2), Color(0xFFD6005A))),
                        radius = w * 0.16f,
                        center = Offset(cx - w * 0.04f, cy - h * 0.13f)
                    )
                }
                "heart" -> {
                    val heart = Path().apply {
                        moveTo(cx, cy + h * 0.30f)
                        cubicTo(cx - w * 0.44f, cy + h * 0.04f, cx - w * 0.36f, cy - h * 0.34f, cx, cy - h * 0.10f)
                        cubicTo(cx + w * 0.36f, cy - h * 0.34f, cx + w * 0.44f, cy + h * 0.04f, cx, cy + h * 0.30f)
                        close()
                    }
                    drawPath(
                        heart,
                        brush = Brush.linearGradient(listOf(Color(0xFFFF7EB6), primary, Color(0xFF9C114B)))
                    )
                    drawCircle(color = SoftWhite.copy(alpha = 0.65f), radius = w * 0.05f, center = Offset(cx - w * 0.13f, cy - h * 0.12f))
                }
                "teddy" -> {
                    val fur = Brush.radialGradient(listOf(Color(0xFFFFDF94), Color(0xFFC68642)))
                    drawCircle(brush = fur, radius = w * 0.12f, center = Offset(cx - w * 0.20f, cy - h * 0.22f))
                    drawCircle(brush = fur, radius = w * 0.12f, center = Offset(cx + w * 0.20f, cy - h * 0.22f))
                    drawCircle(brush = fur, radius = w * 0.24f, center = Offset(cx, cy - h * 0.06f))
                    drawOval(brush = fur, topLeft = Offset(cx - w * 0.22f, cy + h * 0.08f), size = Size(w * 0.44f, h * 0.30f))
                    drawCircle(color = Color(0xFF3A1F0D), radius = w * 0.03f, center = Offset(cx - w * 0.08f, cy - h * 0.08f))
                    drawCircle(color = Color(0xFF3A1F0D), radius = w * 0.03f, center = Offset(cx + w * 0.08f, cy - h * 0.08f))
                    drawCircle(color = NeonPink, radius = w * 0.05f, center = Offset(cx, cy + h * 0.20f))
                }
                "butterfly" -> {
                    val wingBrush = Brush.linearGradient(listOf(GoldenGlow, NeonPink, ElectricViolet))
                    drawOval(brush = wingBrush, topLeft = Offset(cx - w * 0.38f, cy - h * 0.26f), size = Size(w * 0.36f, h * 0.32f))
                    drawOval(brush = wingBrush, topLeft = Offset(cx + w * 0.02f, cy - h * 0.26f), size = Size(w * 0.36f, h * 0.32f))
                    drawOval(brush = wingBrush, topLeft = Offset(cx - w * 0.28f, cy + h * 0.02f), size = Size(w * 0.26f, h * 0.24f))
                    drawOval(brush = wingBrush, topLeft = Offset(cx + w * 0.02f, cy + h * 0.02f), size = Size(w * 0.26f, h * 0.24f))
                    drawRoundRect(color = SoftWhite, topLeft = Offset(cx - w * 0.03f, cy - h * 0.22f), size = Size(w * 0.06f, h * 0.46f), cornerRadius = CornerRadius(6f))
                }
                "ring" -> {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(GoldenGlow, SoftWhite, GoldenGlow)),
                        radius = w * 0.24f,
                        center = Offset(cx, cy + h * 0.08f),
                        style = Stroke(width = w * 0.09f)
                    )
                    val gem = Path().apply {
                        moveTo(cx, cy - h * 0.36f)
                        lineTo(cx + w * 0.18f, cy - h * 0.16f)
                        lineTo(cx, cy - h * 0.02f)
                        lineTo(cx - w * 0.18f, cy - h * 0.16f)
                        close()
                    }
                    drawPath(gem, brush = Brush.linearGradient(listOf(SoftWhite, CyanCrystal, RoyalBlue)))
                }
                "crown" -> {
                    val crown = Path().apply {
                        moveTo(cx - w * 0.34f, cy + h * 0.22f)
                        lineTo(cx - w * 0.38f, cy - h * 0.14f)
                        lineTo(cx - w * 0.16f, cy + h * 0.02f)
                        lineTo(cx, cy - h * 0.28f)
                        lineTo(cx + w * 0.16f, cy + h * 0.02f)
                        lineTo(cx + w * 0.38f, cy - h * 0.14f)
                        lineTo(cx + w * 0.34f, cy + h * 0.22f)
                        close()
                    }
                    drawPath(crown, brush = Brush.linearGradient(listOf(GoldenGlow, Color(0xFFFF9800))))
                    drawCircle(color = NeonPink, radius = w * 0.05f, center = Offset(cx, cy + h * 0.06f))
                    drawCircle(color = CyanCrystal, radius = w * 0.04f, center = Offset(cx - w * 0.18f, cy + h * 0.08f))
                    drawCircle(color = CyanCrystal, radius = w * 0.04f, center = Offset(cx + w * 0.18f, cy + h * 0.08f))
                }
                "star" -> {
                    val star = Path().apply {
                        moveTo(cx, cy - h * 0.36f)
                        lineTo(cx + w * 0.10f, cy - h * 0.10f)
                        lineTo(cx + w * 0.36f, cy - h * 0.08f)
                        lineTo(cx + w * 0.16f, cy + h * 0.10f)
                        lineTo(cx + w * 0.24f, cy + h * 0.34f)
                        lineTo(cx, cy + h * 0.20f)
                        lineTo(cx - w * 0.24f, cy + h * 0.34f)
                        lineTo(cx - w * 0.16f, cy + h * 0.10f)
                        lineTo(cx - w * 0.36f, cy - h * 0.08f)
                        lineTo(cx - w * 0.10f, cy - h * 0.10f)
                        close()
                    }
                    drawPath(star, brush = Brush.linearGradient(listOf(GoldenGlow, ElectricViolet, NeonPink)))
                }
                "car" -> {
                    val body = Path().apply {
                        moveTo(cx - w * 0.40f, cy + h * 0.14f)
                        lineTo(cx - w * 0.34f, cy - h * 0.04f)
                        lineTo(cx - w * 0.14f, cy - h * 0.18f)
                        lineTo(cx + w * 0.16f, cy - h * 0.18f)
                        lineTo(cx + w * 0.36f, cy - h * 0.02f)
                        lineTo(cx + w * 0.40f, cy + h * 0.14f)
                        close()
                    }
                    drawPath(body, brush = Brush.horizontalGradient(listOf(RoyalBlue, ElectricViolet, NeonPink)))
                    drawCircle(color = SoftWhite, radius = w * 0.09f, center = Offset(cx - w * 0.22f, cy + h * 0.16f))
                    drawCircle(color = SoftWhite, radius = w * 0.09f, center = Offset(cx + w * 0.22f, cy + h * 0.16f))
                }
                "yacht" -> {
                    val hull = Path().apply {
                        moveTo(cx - w * 0.42f, cy + h * 0.06f)
                        lineTo(cx + w * 0.42f, cy + h * 0.02f)
                        lineTo(cx + w * 0.28f, cy + h * 0.24f)
                        lineTo(cx - w * 0.32f, cy + h * 0.24f)
                        close()
                    }
                    drawPath(hull, brush = Brush.horizontalGradient(listOf(SoftWhite, CyanCrystal)))
                    val cabin = Path().apply {
                        moveTo(cx - w * 0.20f, cy + h * 0.04f)
                        lineTo(cx - w * 0.10f, cy - h * 0.20f)
                        lineTo(cx + w * 0.16f, cy - h * 0.20f)
                        lineTo(cx + w * 0.26f, cy + h * 0.02f)
                        close()
                    }
                    drawPath(cabin, brush = Brush.linearGradient(listOf(ElectricViolet, RoyalBlue)))
                }
                else -> {
                    // Royal Palace / Crystal Gift
                    drawRect(
                        brush = Brush.verticalGradient(listOf(GoldenGlow, ElectricViolet)),
                        topLeft = Offset(cx - w * 0.28f, cy - h * 0.06f),
                        size = Size(w * 0.56f, h * 0.34f)
                    )
                    val roof = Path().apply {
                        moveTo(cx, cy - h * 0.38f)
                        lineTo(cx + w * 0.32f, cy - h * 0.06f)
                        lineTo(cx - w * 0.32f, cy - h * 0.06f)
                        close()
                    }
                    drawPath(roof, brush = Brush.linearGradient(listOf(NeonPink, GoldenGlow)))
                }
            }
        }
    }
}

fun parseHexSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
