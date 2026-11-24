package com.example.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.Glass3DCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.VibraCosmicBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VibraSplashScreen(
    onFinished: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.72f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(700),
        label = "splash_alpha"
    )
    val infinite = rememberInfiniteTransition(label = "splash_rings")
    val ringRot by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "ring_rot"
    )

    LaunchedEffect(Unit) {
        revealed = true
        delay(1450L)
        onFinished()
    }

    VibraCosmicBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                rotate(ringRot) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(ElectricViolet, NeonPink, Color.Transparent, RoyalBlue, GoldenGlow, Color.Transparent)
                        ),
                        radius = size.minDimension * 0.48f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                rotate(-ringRot * 0.7f) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, RoyalBlue, NeonPink, Color.Transparent)
                        ),
                        radius = size.minDimension * 0.39f,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vibra_launcher_fg),
                    contentDescription = "Vibra 3D Crystal Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(ElectricViolet, NeonPink, GoldenGlow)),
                            RoundedCornerShape(30.dp)
                        )
                )
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "VIBRA",
                    style = MaterialTheme.typography.displayLarge.copy(letterSpacing = 6.sp),
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.vibra_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedLavender
                )
            }
        }
    }
}

private data class OnboardingPage(
    val titleRes: Int,
    val descRes: Int,
    val drawableRes: Int,
    val badge: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun VibraOnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                titleRes = R.string.onboarding_title_1,
                descRes = R.string.onboarding_desc_1,
                drawableRes = R.drawable.img_vibra_hero_crystals,
                badge = "3D HOST FRAMES",
                icon = Icons.Rounded.AutoAwesome
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_2,
                descRes = R.string.onboarding_desc_2,
                drawableRes = R.drawable.img_vibra_onboarding_connect,
                badge = "HD AUDIO & VIDEO",
                icon = Icons.Rounded.GraphicEq
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_3,
                descRes = R.string.onboarding_desc_3,
                drawableRes = R.drawable.img_vibra_onboarding_gifts,
                badge = "LUXURY GIFTS",
                icon = Icons.Rounded.CardGiftcard
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    VibraCosmicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .widthIn(max = 560.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIBRA",
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 3.sp),
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text("Skip", color = MutedLavender)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Glass3DCard(
                        cornerRadius = 28.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                    ) {
                        Image(
                            painter = painterResource(id = page.drawableRes),
                            contentDescription = stringResource(page.titleRes),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xBB090B19))
                                .border(1.dp, GoldenGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = null,
                                    tint = GoldenGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = page.badge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldenGlow
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.displayMedium,
                        color = SoftWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(page.descRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedLavender,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (active) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) Brush.horizontalGradient(listOf(ElectricViolet, NeonPink))
                                else Brush.horizontalGradient(listOf(MutedLavender.copy(0.35f), MutedLavender.copy(0.35f)))
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            NeonGradientButton(
                text = if (pagerState.currentPage == pages.lastIndex) "Enter Vibra" else "Next",
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        onComplete()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("onboarding_next_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = SoftWhite
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
