package com.example.ui.components

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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SportCategory
import com.example.ui.theme.LocalBetZoneColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BetZoneBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val ext = LocalBetZoneColors.current
    val electric = ext.electricBlue
    val neon = ext.neonGreen
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ext.bgTop, ext.bgBottom, ext.bgTop)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(electric.copy(alpha = if (ext.isDark) 0.18f else 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.85f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neon.copy(alpha = if (ext.isDark) 0.12f else 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.55f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.12f, size.height * 0.55f)
            )
        }
        content()
    }
}

@Composable
fun Glass3DCard(
    modifier: Modifier = Modifier,
    accentBorder: Color? = null,
    cornerRadius: Dp = 22.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val ext = LocalBetZoneColors.current
    val borderColor = accentBorder ?: ext.glassBorder
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier
            .shadow(
                elevation = if (ext.isDark) 14.dp else 6.dp,
                shape = shape,
                ambientColor = ext.electricBlue.copy(alpha = 0.25f),
                spotColor = ext.electricBlue.copy(alpha = 0.25f)
            )
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = shape,
        color = ext.glassSurface.copy(alpha = if (ext.isDark) 0.88f else 0.96f),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (ext.isDark) 0.06f else 0.45f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun Animated3DFootballCanvas(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 116.dp
) {
    val ext = LocalBetZoneColors.current
    val infinite = rememberInfiniteTransition(label = "football3d")
    val spinDeg by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.34f

        // Outer neon aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ext.electricBlue.copy(alpha = 0.40f), Color.Transparent),
                center = center,
                radius = radius * 1.45f * pulse
            ),
            radius = radius * 1.45f * pulse,
            center = center
        )

        // Orbital neon rings
        drawCircle(
            color = ext.neonGreen.copy(alpha = 0.5f),
            radius = radius * 1.24f,
            center = center,
            style = Stroke(width = 2.5f)
        )

        // 3D sphere shading
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFC9DBFF),
                    Color(0xFF1F3568)
                ),
                center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f),
                radius = radius * 1.35f
            ),
            radius = radius,
            center = center
        )

        // Rotating pentagon core & seams
        val radOffset = Math.toRadians(spinDeg.toDouble())
        val polyPath = Path()
        val pentRadius = radius * 0.38f
        for (i in 0 until 5) {
            val angle = radOffset + i * (2.0 * Math.PI / 5.0)
            val px = center.x + (pentRadius * cos(angle)).toFloat()
            val py = center.y + (pentRadius * sin(angle)).toFloat()
            if (i == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
            val outerX = center.x + (radius * 0.88f * cos(angle)).toFloat()
            val outerY = center.y + (radius * 0.88f * sin(angle)).toFloat()
            drawLine(
                color = Color(0xFF0A1633),
                start = Offset(px, py),
                end = Offset(outerX, outerY),
                strokeWidth = 3f
            )
        }
        polyPath.close()
        drawPath(path = polyPath, color = Color(0xFF0A1633))

        // Specular electric rim
        drawCircle(
            color = ext.electricBlue.copy(alpha = 0.75f),
            radius = radius,
            center = center,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
fun LivePulseBadge(modifier: Modifier = Modifier) {
    val ext = LocalBetZoneColors.current
    val infinite = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "dot_alpha"
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = ext.liveCrimson.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, ext.liveCrimson.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(ext.liveCrimson)
            )
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelSmall,
                color = ext.liveCrimson,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun TeamCrestBadge(
    shortCode: String,
    primaryHex: Long,
    secondaryHex: Long,
    size: Dp = 44.dp
) {
    val c1 = Color(primaryHex)
    val c2 = Color(secondaryHex)
    Box(
        modifier = Modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = c1, spotColor = c1)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(c1, c2)))
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shortCode.take(4),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun SportPillBadge(
    sport: SportCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalBetZoneColors.current
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("sport_pill_${sport.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) ext.electricBlue else ext.glassSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) ext.electricBlue else ext.glassBorder
        ),
        onClick = onClick
    ) {
        Text(
            text = sport.label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun SkeletonMatchCard(modifier: Modifier = Modifier) {
    val ext = LocalBetZoneColors.current
    val infinite = rememberInfiniteTransition(label = "skeleton")
    val shimmer by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmer"
    )
    Glass3DCard(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ext.glassBorder.copy(alpha = shimmer))
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ext.glassBorder.copy(alpha = shimmer))
            )
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ext.glassBorder.copy(alpha = shimmer))
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ext.glassBorder.copy(alpha = shimmer))
            )
        }
    }
}

enum class BottomNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SPORTS("Sports", Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
    PREDICTIONS("Predictions", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    LEADERBOARD("Leaderboard", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BetZoneFloatingBottomBar(
    currentTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalBetZoneColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = ext.electricBlue, spotColor = ext.electricBlue),
            shape = RoundedCornerShape(28.dp),
            color = ext.glassSurface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, ext.electricBlue.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val selected = tab == currentTab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) ext.electricBlue.copy(alpha = 0.18f) else Color.Transparent
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 6.dp, horizontal = 2.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (selected) ext.electricBlue else ext.subtleText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ext.electricBlue else ext.subtleText,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
