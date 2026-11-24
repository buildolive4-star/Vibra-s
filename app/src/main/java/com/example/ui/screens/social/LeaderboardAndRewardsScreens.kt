package com.example.ui.screens.social

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.mock.MockSportsData
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.RewardClaim
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyChallenge
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.LocalBetZoneColors

@Composable
fun LeaderboardScreen(
    currentUid: String,
    entries: List<LeaderboardEntry>,
    selectedPeriod: LeaderboardPeriod,
    onSelectPeriod: (LeaderboardPeriod) -> Unit,
    onOpenRewards: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val factor = when (selectedPeriod) {
        LeaderboardPeriod.DAILY -> 0.18
        LeaderboardPeriod.WEEKLY -> 0.52
        LeaderboardPeriod.MONTHLY -> 1.0
    }
    val adjusted = entries.map { entry ->
        if (entry.userId == currentUid) entry
        else entry.copy(virtualPoints = (entry.virtualPoints * factor).toLong().coerceAtLeast(350L))
    }.sortedByDescending { it.virtualPoints }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 114.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Global Leaderboard", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Top Virtual Points Strategists", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                }
                OutlinedButton(
                    onClick = onOpenRewards,
                    modifier = Modifier.height(48.dp).testTag("leaderboard_open_rewards"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ext.goldenYellow)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = ext.goldenYellow)
                    Spacer(Modifier.width(6.dp))
                    Text("Rewards", color = ext.goldenYellow)
                }
            }
        }

        item {
            TabRow(
                selectedTabIndex = selectedPeriod.ordinal,
                containerColor = Color.Transparent
            ) {
                LeaderboardPeriod.entries.forEach { period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        text = { Text(period.label) },
                        modifier = Modifier.testTag("leaderboard_tab_${period.name.lowercase()}")
                    )
                }
            }
        }

        itemsIndexed(adjusted, key = { idx, it -> "${selectedPeriod.name}_${it.userId}_$idx" }) { index, entry ->
            val rank = index + 1
            val isMe = entry.userId == currentUid
            val rankColor = when (rank) {
                1 -> ext.goldenYellow
                2 -> ext.electricBlue
                3 -> ext.neonGreen
                else -> ext.subtleText
            }
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = if (isMe) ext.neonGreen else if (rank <= 3) rankColor else ext.glassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = rankColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, rankColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("#$rank", style = MaterialTheme.typography.labelLarge, color = rankColor)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isMe) "${entry.displayName} (You)" else entry.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${entry.accuracyPct}% Accuracy • ${entry.badges.size} Badges",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.subtleText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.virtualPoints} VP",
                            style = MaterialTheme.typography.titleMedium,
                            color = ext.goldenYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${entry.correctPredictions}/${entry.totalPredictions} Won",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.neonGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardsScreen(
    profile: UserProfile?,
    claims: List<RewardClaim>,
    todayKey: String,
    isBusy: Boolean,
    onBack: () -> Unit,
    onClaimDaily: () -> Unit,
    onClaimChallenge: (WeeklyChallenge) -> Unit,
    onClaimQuiz: (String, String, Int) -> Unit
) {
    val ext = LocalBetZoneColors.current
    val claimedKeys = claims.map { it.rewardKey }.toSet()
    val dailyClaimed = claimedKeys.contains("daily_$todayKey")
    val totalPredictions = (profile?.totalPredictions ?: 0L).toInt()
    val correctPredictions = (profile?.correctPredictions ?: 0L).toInt()

    var activeQuizIndex by remember { mutableIntStateOf(0) }
    val currentQuiz = MockSportsData.sportsQuizzes[activeQuizIndex % MockSportsData.sportsQuizzes.size]
    var selectedQuizOption by remember { mutableStateOf<Int?>(null) }
    var quizFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("rewards_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rewards & Quizzes", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text("Earn Virtual Points daily — zero real-money purchase needed", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                }
            }
        }

        // Daily Login Reward
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.goldenYellow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Login Reward", style = MaterialTheme.typography.titleMedium, color = ext.goldenYellow)
                        Text("Current Streak: ${profile?.streakDays ?: 1} Day(s) • +250 VP", style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }
                    Button(
                        onClick = onClaimDaily,
                        enabled = !dailyClaimed && !isBusy,
                        modifier = Modifier.height(48.dp).testTag("claim_daily_reward_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ext.goldenYellow, contentColor = Color.Black)
                    ) {
                        Text(if (dailyClaimed) "Claimed" else "Claim +250 VP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Weekly Challenges
        item {
            Text("Weekly Challenges", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        items(MockSportsData.weeklyChallenges, key = { it.id }) { challenge ->
            val progressValue = if (challenge.id == "wc_win_2") correctPredictions else totalPredictions
            val ratio = (progressValue.toFloat() / challenge.targetCount.toFloat()).coerceIn(0f, 1f)
            val alreadyClaimed = claimedKeys.contains(challenge.id)
            val canClaim = progressValue >= challenge.targetCount && !alreadyClaimed && !isBusy

            Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(challenge.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(challenge.description, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = ext.neonGreen.copy(alpha = 0.16f)) {
                        Text("+${challenge.rewardPoints} VP", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = ext.neonGreen)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
                    color = ext.electricBlue,
                    trackColor = ext.glassBorder
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${progressValue.coerceAtMost(challenge.targetCount)}/${challenge.targetCount} Completed", style = MaterialTheme.typography.labelSmall, color = ext.subtleText)
                    Button(
                        onClick = { onClaimChallenge(challenge) },
                        enabled = canClaim,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (alreadyClaimed) "Claimed" else if (canClaim) "Claim Bonus" else "In Progress")
                    }
                }
            }
        }

        // Interactive Sports Quiz
        item {
            Text("Sports Trivia Quiz", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            val quizClaimed = claimedKeys.contains(currentQuiz.id)
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.electricBlue) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = ext.electricBlue)
                    Text("${currentQuiz.sport} Challenge • +${currentQuiz.bonusPoints} VP", style = MaterialTheme.typography.labelLarge, color = ext.electricBlue)
                }
                Spacer(Modifier.height(8.dp))
                Text(currentQuiz.question, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                currentQuiz.options.forEachIndexed { idx, opt ->
                    val chosen = selectedQuizOption == idx
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("quiz_option_$idx"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (chosen) ext.electricBlue.copy(alpha = 0.22f) else ext.bgBottom,
                        border = BorderStroke(1.dp, if (chosen) ext.electricBlue else ext.glassBorder),
                        onClick = { selectedQuizOption = idx }
                    ) {
                        Text(opt, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (quizFeedback != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(quizFeedback!!, style = MaterialTheme.typography.bodySmall, color = ext.neonGreen)
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val picked = selectedQuizOption
                            if (picked == null) {
                                quizFeedback = "Select an option first."
                            } else if (picked == currentQuiz.correctIndex) {
                                quizFeedback = "Spot on! Virtual Points awarded."
                                onClaimQuiz(currentQuiz.id, "Quiz: ${currentQuiz.sport}", currentQuiz.bonusPoints)
                            } else {
                                quizFeedback = "Not quite — try another answer!"
                            }
                        },
                        enabled = !quizClaimed && !isBusy,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("submit_quiz_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (quizClaimed) "Completed" else "Submit Answer")
                    }
                    OutlinedButton(
                        onClick = {
                            activeQuizIndex += 1
                            selectedQuizOption = null
                            quizFeedback = null
                        },
                        modifier = Modifier.height(48.dp).testTag("next_quiz_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next Quiz")
                    }
                }
            }
        }

        // Achievement Badges Showcase
        item {
            Text("Achievement Badges", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        items(MockSportsData.badgesCatalog, key = { it.id }) { badge ->
            val unlocked = profile?.badges?.contains(badge.id) == true
            Glass3DCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = if (unlocked) ext.neonGreen else ext.glassBorder
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (unlocked) Icons.Default.MilitaryTech else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) ext.goldenYellow else ext.subtleText,
                        modifier = Modifier.size(30.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(badge.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(badge.subtitle, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = (if (unlocked) ext.neonGreen else ext.subtleText).copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (unlocked) "UNLOCKED" else "LOCKED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (unlocked) ext.neonGreen else ext.subtleText
                        )
                    }
                }
            }
        }
    }
}
