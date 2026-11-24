package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.mock.MockSportsData
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatus
import com.example.data.model.SportCategory
import com.example.data.model.TournamentInfo
import com.example.data.model.UserProfile
import com.example.ui.components.Glass3DCard
import com.example.ui.components.LivePulseBadge
import com.example.ui.components.SkeletonMatchCard
import com.example.ui.components.SportPillBadge
import com.example.ui.components.TeamCrestBadge
import com.example.ui.theme.LocalBetZoneColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    profile: UserProfile?,
    matches: List<MatchItem>,
    unreadNotificationCount: Int,
    onSelectSport: (SportCategory) -> Unit,
    onSelectTournament: (TournamentInfo) -> Unit,
    onOpenMatchDetails: (String) -> Unit,
    onOpenLiveCentre: (String) -> Unit,
    onOpenPredictionCentre: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRewards: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val liveMatches = matches.filter { it.status == MatchStatus.LIVE }
    val upcomingMatches = matches.filter { it.status == MatchStatus.UPCOMING }
    val pointsText = NumberFormat.getNumberInstance(Locale.US).format(profile?.virtualPoints ?: 2500L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onOpenProfile)
                            .testTag("home_avatar_button"),
                        shape = CircleShape,
                        color = ext.electricBlue.copy(alpha = 0.20f),
                        border = BorderStroke(1.5.dp, ext.electricBlue)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User profile",
                                tint = ext.electricBlue
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "777BET",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = profile?.displayName ?: "Sports Strategist",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.subtleText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clickable(onClick = onOpenRewards)
                            .testTag("home_vp_pill"),
                        shape = RoundedCornerShape(999.dp),
                        color = ext.goldenYellow.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, ext.goldenYellow.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Virtual Points",
                                tint = ext.goldenYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$pointsText VP",
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.goldenYellow,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.testTag("home_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp),
                                shape = CircleShape,
                                color = ext.liveCrimson
                            ) {
                                Text(
                                    text = unreadNotificationCount.coerceAtMost(9).toString(),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Animated Promotional Stadium Banner
        item {
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.electricBlue,
                cornerRadius = 26.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(174.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_betzone_hero_stadium),
                        contentDescription = "Welcome to the Sports Universe banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF050814).copy(alpha = 0.90f),
                                        Color(0xFF050814).copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ext.neonGreen.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, ext.neonGreen.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "VIRTUAL POINTS ARENA • 0% REAL MONEY",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.neonGreen
                            )
                        }

                        Column {
                            Text(
                                text = "WELCOME TO THE SPORTS UNIVERSE.",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onOpenPredictionCentre(liveMatches.firstOrNull()?.id ?: matches.first().id) },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("hero_predict_cta"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ext.electricBlue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Predict Now")
                                }
                                OutlinedButton(
                                    onClick = onOpenRewards,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("hero_rewards_cta"),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, ext.goldenYellow)
                                ) {
                                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = ext.goldenYellow, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Rewards", color = ext.goldenYellow)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sports Categories Horizontal Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sports Categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SportCategory.entries) { sport ->
                        SportPillBadge(
                            sport = sport,
                            selected = false,
                            onClick = { onSelectSport(sport) }
                        )
                    }
                }
            }
        }

        // Live Matches Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LivePulseBadge()
                    Text(
                        text = "Live Matches",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "${liveMatches.size} Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.neonGreen
                )
            }
        }

        if (liveMatches.isEmpty()) {
            item { SkeletonMatchCard() }
        } else {
            items(liveMatches, key = { it.id }) { match ->
                LiveMatchCard(
                    match = match,
                    onDetailsClick = { onOpenMatchDetails(match.id) },
                    onLiveCentreClick = { onOpenLiveCentre(match.id) }
                )
            }
        }

        // Upcoming Matches Section
        item {
            Text(
                text = "Upcoming Matches",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(upcomingMatches, key = { it.id }) { match ->
            UpcomingMatchCard(
                match = match,
                onClick = { onOpenMatchDetails(match.id) }
            )
        }

        // Popular Tournaments Visual Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Popular Tournaments",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(MockSportsData.popularTournaments, key = { it.id }) { tournament ->
                        val accent = Color(tournament.accentHex)
                        Glass3DCard(
                            modifier = Modifier
                                .width(172.dp)
                                .testTag("tournament_card_${tournament.id}"),
                            accentBorder = accent,
                            onClick = { onSelectTournament(tournament) }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accent.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, accent)
                            ) {
                                Text(
                                    text = tournament.badgeCode,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = tournament.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${tournament.sport.label} • ${tournament.activeMatches} Fixtures",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.subtleText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMatchCard(
    match: MatchItem,
    onDetailsClick: () -> Unit,
    onLiveCentreClick: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    Glass3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_match_card_${match.id}"),
        accentBorder = ext.electricBlue,
        onClick = onDetailsClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${match.sport.label.uppercase()} • ${match.tournament}",
                style = MaterialTheme.typography.labelMedium,
                color = ext.electricBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            LivePulseBadge()
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamCrestBadge(match.homeTeam.shortCode, match.homeTeam.primaryHex, match.homeTeam.secondaryHex)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = match.homeTeam.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = match.homeTeam.score,
                    style = MaterialTheme.typography.headlineSmall,
                    color = ext.neonGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ext.electricBlue.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = match.timerLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ext.electricBlue
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("VS", style = MaterialTheme.typography.labelSmall, color = ext.subtleText)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamCrestBadge(match.awayTeam.shortCode, match.awayTeam.primaryHex, match.awayTeam.secondaryHex)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = match.awayTeam.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = match.awayTeam.score,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = match.summaryNote,
            style = MaterialTheme.typography.bodySmall,
            color = ext.subtleText
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDetailsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("match_details_btn_${match.id}"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ext.electricBlue)
            ) {
                Text("Match Details", color = ext.electricBlue)
            }
            Button(
                onClick = onLiveCentreClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("live_centre_btn_${match.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ext.neonGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("Live Centre", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpcomingMatchCard(
    match: MatchItem,
    onClick: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val hrs = match.countdownSeconds / 3600
    val mins = (match.countdownSeconds % 3600) / 60
    val secs = match.countdownSeconds % 60
    val countdownText = "%02dh : %02dm : %02ds".format(hrs, mins, secs)

    Glass3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_match_card_${match.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${match.sport.label} • ${match.tournament}",
                style = MaterialTheme.typography.labelMedium,
                color = ext.goldenYellow
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = ext.electricBlue, modifier = Modifier.size(15.dp))
                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.electricBlue
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                TeamCrestBadge(match.homeTeam.shortCode, match.homeTeam.primaryHex, match.homeTeam.secondaryHex, size = 38.dp)
                Text(
                    text = match.homeTeam.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("vs", style = MaterialTheme.typography.labelMedium, color = ext.subtleText, modifier = Modifier.padding(horizontal = 6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = match.awayTeam.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TeamCrestBadge(match.awayTeam.shortCode, match.awayTeam.primaryHex, match.awayTeam.secondaryHex, size = 38.dp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${match.timerLabel} • ${match.venue}",
            style = MaterialTheme.typography.bodySmall,
            color = ext.subtleText
        )
    }
}
