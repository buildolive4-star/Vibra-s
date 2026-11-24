package com.example.ui.screens.predictions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketOption
import com.example.data.model.MatchItem
import com.example.data.model.PredictionMarket
import com.example.data.model.PredictionStatus
import com.example.data.model.UserPrediction
import com.example.data.model.UserProfile
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.LocalBetZoneColors
import kotlin.math.roundToLong

@Composable
fun PredictionsHubScreen(
    profile: UserProfile?,
    matches: List<MatchItem>,
    predictions: List<UserPrediction>,
    initialMatchId: String?,
    isBusy: Boolean,
    onPlacePrediction: (MatchItem, PredictionMarket, MarketOption, Long) -> Unit,
    onSettlePrediction: (UserPrediction, Boolean) -> Unit
) {
    val ext = LocalBetZoneColors.current
    var hubTab by remember { mutableIntStateOf(0) } // 0 = Prediction Centre, 1 = My Predictions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { hubTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tab_prediction_centre"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hubTab == 0) ext.electricBlue else ext.glassSurface,
                    contentColor = if (hubTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Prediction Centre")
            }
            Button(
                onClick = { hubTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tab_my_predictions"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hubTab == 1) ext.neonGreen else ext.glassSurface,
                    contentColor = if (hubTab == 1) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("My Predictions (${predictions.size})")
            }
        }

        if (hubTab == 0) {
            PredictionCentreContent(
                profile = profile,
                matches = matches,
                initialMatchId = initialMatchId,
                isBusy = isBusy,
                onPlacePrediction = { m, mk, opt, stake ->
                    onPlacePrediction(m, mk, opt, stake)
                }
            )
        } else {
            MyPredictionsContent(
                profile = profile,
                predictions = predictions,
                isBusy = isBusy,
                onSettlePrediction = onSettlePrediction
            )
        }
    }
}

@Composable
private fun PredictionCentreContent(
    profile: UserProfile?,
    matches: List<MatchItem>,
    initialMatchId: String?,
    isBusy: Boolean,
    onPlacePrediction: (MatchItem, PredictionMarket, MarketOption, Long) -> Unit
) {
    val ext = LocalBetZoneColors.current
    val initialMatch = matches.firstOrNull { it.id == initialMatchId } ?: matches.first()
    var selectedMatch by remember(matches, initialMatchId) { mutableStateOf(initialMatch) }
    var selectedMarket by remember(selectedMatch) { mutableStateOf(selectedMatch.markets.first()) }
    var selectedOption by remember(selectedMarket) { mutableStateOf(selectedMarket.options.first()) }
    var stakePoints by remember { mutableLongStateOf(250L) }

    val balance = profile?.virtualPoints ?: 0L
    val stakes = remember { listOf(100L, 250L, 500L, 1000L, 2500L) }
    val potentialReturn = ((stakePoints * selectedOption.multiplierX100.toDouble()) / 100.0).roundToLong()

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 114.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Strict Virtual-Points-Only Notice
        item {
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = ext.neonGreen
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ext.neonGreen)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Virtual Points Prediction Arena", style = MaterialTheme.typography.titleSmall, color = ext.neonGreen)
                        Text(
                            text = "Play with Virtual Points (VP) only — zero real-money wagering, deposits, or cash prizes. Available Balance: $balance VP",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.subtleText
                        )
                    }
                }
            }
        }

        // Match Picker
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Select Fixture", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(matches, key = { it.id }) { m ->
                        FilterChip(
                            selected = m.id == selectedMatch.id,
                            onClick = {
                                selectedMatch = m
                                selectedMarket = m.markets.first()
                                selectedOption = selectedMarket.options.first()
                            },
                            label = { Text("${m.homeTeam.shortCode} vs ${m.awayTeam.shortCode}") }
                        )
                    }
                }
            }
        }

        // Market Picker
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.electricBlue) {
                Text("2. ${selectedMatch.matchTitle}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("${selectedMatch.sport.label} • ${selectedMatch.tournament}", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedMatch.markets, key = { it.id }) { mk ->
                        FilterChip(
                            selected = mk.id == selectedMarket.id,
                            onClick = {
                                selectedMarket = mk
                                selectedOption = mk.options.first()
                            },
                            label = { Text(mk.title) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Choose Outcome", style = MaterialTheme.typography.labelMedium, color = ext.electricBlue)
                Spacer(Modifier.height(8.dp))
                selectedMarket.options.forEach { option ->
                    val chosen = option.id == selectedOption.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("prediction_option_${option.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (chosen) ext.electricBlue.copy(alpha = 0.22f) else ext.bgBottom,
                        border = BorderStroke(1.dp, if (chosen) ext.electricBlue else ext.glassBorder),
                        onClick = { selectedOption = option }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ext.goldenYellow.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "%.2fx VP".format(option.multiplierX100 / 100.0),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ext.goldenYellow
                                )
                            }
                        }
                    }
                }
            }
        }

        // Virtual Points Stake Selector & Submit
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.goldenYellow) {
                Text("3. Virtual Points Stake", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stakes) { amt ->
                        FilterChip(
                            selected = stakePoints == amt,
                            onClick = { stakePoints = amt },
                            label = { Text("$amt VP") }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Potential Virtual Points Return", style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
                    Text(
                        text = "$potentialReturn VP",
                        style = MaterialTheme.typography.titleMedium,
                        color = ext.neonGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onPlacePrediction(selectedMatch, selectedMarket, selectedOption, stakePoints) },
                    enabled = !isBusy && balance >= stakePoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_prediction_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (balance < stakePoints) "Insufficient Virtual Points" else "Confirm Prediction ($stakePoints VP)",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MyPredictionsContent(
    profile: UserProfile?,
    predictions: List<UserPrediction>,
    isBusy: Boolean,
    onSettlePrediction: (UserPrediction, Boolean) -> Unit
) {
    val ext = LocalBetZoneColors.current
    var filter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, COMPLETED, WON, LOST

    val filtered = predictions.filter { p ->
        when (filter) {
            "ACTIVE" -> p.parsedStatus == PredictionStatus.ACTIVE
            "COMPLETED" -> p.parsedStatus != PredictionStatus.ACTIVE
            "WON" -> p.parsedStatus == PredictionStatus.WON
            "LOST" -> p.parsedStatus == PredictionStatus.LOST
            else -> true
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 114.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.electricBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Prediction Accuracy", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        Text("${profile?.accuracyPct ?: 0}%", style = MaterialTheme.typography.headlineMedium, color = ext.neonGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Calls", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        Text("${profile?.totalPredictions ?: predictions.size}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Correct", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        Text("${profile?.correctPredictions ?: 0}", style = MaterialTheme.typography.titleLarge, color = ext.goldenYellow)
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "ALL" to "All",
                    "ACTIVE" to "Active",
                    "COMPLETED" to "Completed",
                    "WON" to "Correct",
                    "LOST" to "Incorrect"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = filter == key,
                        onClick = { filter = key },
                        label = { Text(label) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ext.electricBlue)
                        Text(
                            text = "No predictions in this category yet. Lock in a virtual-points pick from the Prediction Centre!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.subtleText
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id.ifBlank { "${it.matchId}_${it.selectedPick}" } }) { pred ->
                val statusColor = when (pred.parsedStatus) {
                    PredictionStatus.ACTIVE -> ext.electricBlue
                    PredictionStatus.WON -> ext.neonGreen
                    PredictionStatus.LOST -> ext.liveCrimson
                }
                Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = statusColor) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${pred.sport} • ${pred.tournament}", style = MaterialTheme.typography.labelSmall, color = ext.subtleText)
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = statusColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = pred.parsedStatus.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(pred.matchTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("${pred.marketType}: ${pred.selectedPick}", style = MaterialTheme.typography.bodyMedium, color = ext.electricBlue)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stake: ${pred.stakePoints} VP", style = MaterialTheme.typography.labelMedium, color = ext.subtleText)
                        Text("Reward: ${pred.potentialRewardPoints} VP", style = MaterialTheme.typography.labelMedium, color = ext.goldenYellow)
                    }
                    if (pred.resultSummary.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(pred.resultSummary, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }
                    if (pred.parsedStatus == PredictionStatus.ACTIVE && pred.id.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSettlePrediction(pred, true) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Settle Win")
                            }
                            OutlinedButton(
                                onClick = { onSettlePrediction(pred, false) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Settle Loss")
                            }
                        }
                    }
                }
            }
        }
    }
}
