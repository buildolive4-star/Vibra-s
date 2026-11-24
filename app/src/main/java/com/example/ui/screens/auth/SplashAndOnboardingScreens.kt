package com.example.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.Animated3DFootballCanvas
import com.example.ui.components.BetZoneBackdrop
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.LocalBetZoneColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val ext = LocalBetZoneColors.current
    var targetProgress by remember { mutableFloatStateOf(0.12f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1200),
        label = "splash_progress"
    )

    LaunchedEffect(Unit) {
        delay(120L)
        targetProgress = 1f
        delay(1350L)
        onFinished()
    }

    BetZoneBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Animated3DFootballCanvas(sizeDp = 152.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "777BET",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ext.electricBlue.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, ext.electricBlue.copy(alpha = 0.45f))
            ) {
                Text(
                    text = stringResource(R.string.betzone_tagline),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.neonGreen
                )
            }
            Spacer(Modifier.height(42.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(210.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ext.electricBlue,
                trackColor = ext.glassBorder.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Loading Live Arenas...",
                style = MaterialTheme.typography.bodySmall,
                color = ext.subtleText
            )
        }
    }
}

private data class OnboardingPage(
    val titleRes: Int,
    val descRes: Int,
    val imageRes: Int,
    val badge: String
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val ext = LocalBetZoneColors.current
    val scope = rememberCoroutineScope()
    val pages = remember {
        listOf(
            OnboardingPage(
                R.string.onboarding_title_1,
                R.string.onboarding_desc_1,
                R.drawable.img_betzone_onboard_live,
                "LIVE SPORTS ARENA"
            ),
            OnboardingPage(
                R.string.onboarding_title_2,
                R.string.onboarding_desc_2,
                R.drawable.img_betzone_onboard_predict,
                "VIRTUAL POINTS CHALLENGES"
            ),
            OnboardingPage(
                R.string.onboarding_title_3,
                R.string.onboarding_desc_3,
                R.drawable.img_betzone_onboard_universe,
                "GLOBAL TOURNAMENTS"
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    BetZoneBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "777BET",
                    style = MaterialTheme.typography.titleLarge,
                    color = ext.electricBlue,
                    fontWeight = FontWeight.ExtraBold
                )
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text("Skip", color = ext.subtleText)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { idx ->
                val page = pages[idx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Glass3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentBorder = ext.electricBlue,
                        cornerRadius = 26.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(18.dp))
                        ) {
                            Image(
                                painter = painterResource(page.imageRes),
                                contentDescription = stringResource(page.titleRes),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                                        )
                                    )
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(999.dp),
                                color = ext.electricBlue.copy(alpha = 0.88f)
                            ) {
                                Text(
                                    text = page.badge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(page.descRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ext.subtleText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (active) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) ext.neonGreen else ext.glassBorder)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            val isLast = pagerState.currentPage == pages.lastIndex
            Button(
                onClick = {
                    if (isLast) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag(if (isLast) "onboarding_get_started_button" else "onboarding_next_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLast) ext.neonGreen else ext.electricBlue,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isLast) "Get Started" else "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
