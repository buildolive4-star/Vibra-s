package com.example.ui.screens.sports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchItem
import com.example.data.model.MatchStatus
import com.example.ui.components.Glass3DCard
import com.example.ui.components.LivePulseBadge
import com.example.ui.components.TeamCrestBadge
import com.example.ui.theme.LocalBetZoneColors

@Composable
fun MatchDetailsScreen(
    match: MatchItem,
    onBack: () -> Unit,
    onOpenLiveCentre: () -> Unit,
    onOpenPredict: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val tabs = remember { listOf("Overview", "Statistics", "Commentary", "Lineups", "History") }
    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("match_details_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.tournament,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${match.sport.label} • ${match.venue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.subtleText
                    )
                }
                if (match.status == MatchStatus.LIVE) {
                    LivePulseBadge()
                }
            }
        }

        // Scoreboard Header Card
        item {
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.electricBlue
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamCrestBadge(match.homeTeam.shortCode, match.homeTeam.primaryHex, match.homeTeam.secondaryHex, 52.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(match.homeTeam.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(match.homeTeam.score, style = MaterialTheme.typography.headlineMedium, color = ext.neonGreen, fontWeight = FontWeight.ExtraBold)
                        Text(match.homeTeam.subScore, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ext.electricBlue.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = match.timerLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.electricBlue
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(match.status.label, style = MaterialTheme.typography.labelSmall, color = ext.subtleText)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TeamCrestBadge(match.awayTeam.shortCode, match.awayTeam.primaryHex, match.awayTeam.secondaryHex, 52.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(match.awayTeam.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(match.awayTeam.score, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                        Text(match.awayTeam.subScore, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenLiveCentre,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_live_centre_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ext.electricBlue)
                    ) {
                        Icon(Icons.Default.SportsScore, contentDescription = null, tint = ext.electricBlue)
                        Spacer(Modifier.width(6.dp))
                        Text("Live Centre", color = ext.electricBlue)
                    }
                    Button(
                        onClick = onOpenPredict,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_predict_from_match_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Predict (VP)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5-Tab Bar
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title) },
                        modifier = Modifier.testTag("match_tab_${title.lowercase()}")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Match Overview", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text(match.summaryNote, style = MaterialTheme.typography.bodyLarge, color = ext.neonGreen)
                    Spacer(Modifier.height(10.dp))
                    Text("Venue: ${match.venue}", style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
                    Spacer(Modifier.height(10.dp))
                    Text("Live Momentum (${match.homeTeam.shortCode} vs ${match.awayTeam.shortCode})", style = MaterialTheme.typography.labelMedium, color = ext.electricBlue)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { match.momentumHome },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = ext.electricBlue,
                        trackColor = ext.glassBorder
                    )
                }
            }
            1 -> items(match.stats) { row ->
                StatComparisonCard(row.label, row.homeValue, row.awayValue, row.homeRatio)
            }
            2 -> items(match.commentary) { entry ->
                CommentaryCard(entry.stamp, entry.headline, entry.detail, entry.highlight)
            }
            3 -> {
                if (match.lineups.isEmpty()) {
                    item {
                        Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Singles / Roster info updates prior to toss or kickoff.", style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
                        }
                    }
                } else {
                    items(match.lineups) { player ->
                        Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${player.name} (${player.teamSide})", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text(player.role, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Rating ${player.rating}", style = MaterialTheme.typography.labelMedium, color = ext.neonGreen)
                                    Text(player.statHighlight, style = MaterialTheme.typography.bodySmall, color = ext.electricBlue)
                                }
                            }
                        }
                    }
                }
            }
            else -> items(match.headToHead) { h2h ->
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(h2h.scoreLine, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(h2h.dateLabel, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ext.goldenYellow.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = h2h.winnerName,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.goldenYellow
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMatchCentreScreen(
    match: MatchItem,
    onBack: () -> Unit,
    onOpenPredict: () -> Unit
) {
    val ext = LocalBetZoneColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("live_centre_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Live Match Centre",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                LivePulseBadge()
            }
        }

        // Large Live Scoreboard + Momentum Timeline
        item {
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.neonGreen
            ) {
                Text(
                    text = "${match.tournament} • ${match.timerLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.electricBlue
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        TeamCrestBadge(match.homeTeam.shortCode, match.homeTeam.primaryHex, match.homeTeam.secondaryHex, 58.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(match.homeTeam.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(match.homeTeam.score, style = MaterialTheme.typography.displaySmall, color = ext.neonGreen)
                    }
                    Text("VS", style = MaterialTheme.typography.titleMedium, color = ext.subtleText)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        TeamCrestBadge(match.awayTeam.shortCode, match.awayTeam.primaryHex, match.awayTeam.secondaryHex, 58.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(match.awayTeam.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(match.awayTeam.score, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Match Momentum Timeline (${(match.momentumHome * 100).toInt()}% ${match.homeTeam.shortCode})",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.subtleText
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { match.momentumHome },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = ext.neonGreen,
                    trackColor = ext.electricBlue.copy(alpha = 0.35f)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onOpenPredict,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("live_centre_predict_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ext.electricBlue, contentColor = Color.White)
                ) {
                    Text("Open Prediction Centre (Virtual Points)", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("Team Statistics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        items(match.stats) { row ->
            StatComparisonCard(row.label, row.homeValue, row.awayValue, row.homeRatio)
        }

        if (match.lineups.isNotEmpty()) {
            item {
                Text("Player Performance Cards", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            items(match.lineups) { player ->
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(player.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("${player.teamSide} • ${player.role}", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ext.neonGreen.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${player.statHighlight} • ★${player.rating}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.neonGreen
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (match.sport.name == "CRICKET") "Ball-by-Ball Commentary" else "Match Events & Timeline",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(match.commentary) { entry ->
            CommentaryCard(entry.stamp, entry.headline, entry.detail, entry.highlight)
        }
    }
}

@Composable
private fun StatComparisonCard(
    label: String,
    homeValue: String,
    awayValue: String,
    homeRatio: Float
) {
    val ext = LocalBetZoneColors.current
    Glass3DCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(homeValue, style = MaterialTheme.typography.labelLarge, color = ext.neonGreen)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
            Text(awayValue, style = MaterialTheme.typography.labelLarge, color = ext.electricBlue)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { homeRatio.coerceIn(0.05f, 0.95f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = ext.neonGreen,
            trackColor = ext.electricBlue.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun CommentaryCard(
    stamp: String,
    headline: String,
    detail: String,
    highlight: Boolean
) {
    val ext = LocalBetZoneColors.current
    Glass3DCard(
        modifier = Modifier.fillMaxWidth(),
        accentBorder = if (highlight) ext.goldenYellow else ext.glassBorder
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = (if (highlight) ext.goldenYellow else ext.electricBlue).copy(alpha = 0.18f)
            ) {
                Text(
                    text = stamp,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (highlight) ext.goldenYellow else ext.electricBlue
                )
            }
            Text(headline, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(6.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
    }
}
