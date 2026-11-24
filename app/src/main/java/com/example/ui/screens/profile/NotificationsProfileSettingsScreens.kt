package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.mock.MockSportsData
import com.example.data.model.SportCategory
import com.example.data.model.UserNotification
import com.example.data.model.UserProfile
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.LocalBetZoneColors

@Composable
fun NotificationsScreen(
    notifications: List<UserNotification>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenMatch: (String) -> Unit
) {
    val ext = LocalBetZoneColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onMarkAllRead,
                    modifier = Modifier.testTag("mark_all_read_button")
                ) {
                    Text("Mark All Read", color = ext.electricBlue)
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text("All caught up! Match alerts and prediction outcomes will appear here.", style = MaterialTheme.typography.bodyMedium, color = ext.subtleText)
                }
            }
        } else {
            items(notifications, key = { it.id.ifBlank { it.title } }) { item ->
                Glass3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentBorder = if (item.unread) ext.electricBlue else ext.glassBorder,
                    onClick = {
                        if (item.id.isNotBlank()) onMarkRead(item.id)
                        if (item.matchId.isNotBlank()) onOpenMatch(item.matchId)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (item.unread) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (item.unread) ext.neonGreen else ext.subtleText
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(item.body, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
                        }
                        if (item.unread) {
                            Surface(shape = CircleShape, color = ext.neonGreen) {
                                Text("NEW", modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    uid: String,
    profile: UserProfile?,
    onToggleFavoriteSport: (String) -> Unit,
    onToggleFavoriteTeam: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRewards: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    val favSports = profile?.favoriteSports ?: emptyList()
    val favTeams = profile?.favoriteTeams ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 114.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Profile", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_open_settings")) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ext.electricBlue)
                }
            }
        }

        // Identity + Metrics Card
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth(), accentBorder = ext.electricBlue) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = ext.electricBlue.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, ext.neonGreen)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ext.electricBlue, modifier = Modifier.size(34.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile?.displayName ?: "Sports Strategist", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("ID: ${uid.take(12).uppercase()}", style = MaterialTheme.typography.labelSmall, color = ext.subtleText)
                        if (profile != null && profile.phoneNumber.isNotBlank()) {
                            Text("${profile.countryCode} ${profile.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = ext.electricBlue)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileMetricColumn("Virtual Points", "${profile?.virtualPoints ?: 2500} VP", ext.goldenYellow)
                    ProfileMetricColumn("Accuracy", "${profile?.accuracyPct ?: 0}%", ext.neonGreen)
                    ProfileMetricColumn("Predictions", "${profile?.totalPredictions ?: 0}", ext.electricBlue)
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenRewards,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("profile_rewards_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ext.goldenYellow, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Rewards")
                    }
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("profile_settings_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ext.electricBlue)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = ext.electricBlue)
                        Spacer(Modifier.width(6.dp))
                        Text("Settings", color = ext.electricBlue)
                    }
                }
            }
        }

        // Achievement Showcase
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                Text("Achievements Showcase", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockSportsData.badgesCatalog.forEach { badge ->
                        val earned = profile?.badges?.contains(badge.id) == true
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (earned) ext.goldenYellow else ext.glassBorder).copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, if (earned) ext.goldenYellow else ext.glassBorder)
                        ) {
                            Text(
                                text = if (earned) "★ ${badge.title}" else badge.title,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (earned) ext.goldenYellow else ext.subtleText
                            )
                        }
                    }
                }
            }
        }

        // Favourite Sports
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                Text("Favourite Sports", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SportCategory.entries.forEach { sport ->
                        val selected = favSports.contains(sport.label)
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleFavoriteSport(sport.label) },
                            label = { Text(sport.label) }
                        )
                    }
                }
            }
        }

        // Favourite Teams
        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                Text("Favourite Teams", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MockSportsData.allKnownTeams.forEach { team ->
                        val selected = favTeams.contains(team)
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleFavoriteTeam(team) },
                            label = { Text(team) },
                            leadingIcon = {
                                if (selected) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = ext.goldenYellow, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricColumn(label: String, value: String, color: Color) {
    val ext = LocalBetZoneColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
    }
}

@Composable
fun SettingsScreen(
    profile: UserProfile?,
    darkMode: Boolean,
    language: String,
    onBack: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onChangeLanguage: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onTogglePublicLeaderboard: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Settings", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        item {
            Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                SettingsActionRow(Icons.Default.Edit, "Edit Profile", profile?.displayName ?: "Sports Strategist") {
                    showEditDialog = true
                }
                SettingsActionRow(Icons.Default.Language, "Change Language", language) {
                    showLanguageDialog = true
                }
                SettingsSwitchRow(Icons.Default.Brightness6, "Dark Mode Theme", "Deep navy & neon arena styling", darkMode, "settings_dark_mode_switch") {
                    onToggleDarkMode(it)
                }
                SettingsSwitchRow(Icons.Default.Notifications, "Notification Preferences", "Live match, team & reward alerts", profile?.notificationsEnabled ?: true, "settings_notifications_switch") {
                    onToggleNotifications(it)
                }
                SettingsSwitchRow(Icons.Default.PrivacyTip, "Privacy: Public Leaderboard", "Show non-PII rank & points on leaderboard", profile?.privacyPublicLeaderboard ?: true, "settings_privacy_switch") {
                    onTogglePublicLeaderboard(it)
                }
                SettingsActionRow(Icons.Default.Shield, "Terms & Conditions", "Virtual Points Only entertainment terms") {
                    infoDialogTitle = "Terms & Conditions"
                }
                SettingsActionRow(Icons.AutoMirrored.Filled.HelpOutline, "Help & Support", "FAQs, prediction scoring & account guide") {
                    infoDialogTitle = "Help & Support"
                }
                SettingsActionRow(Icons.Default.Info, "About Application", "777Bet v1.0 • Build 2026") {
                    infoDialogTitle = "About 777Bet"
                }
            }
        }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_logout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ext.liveCrimson, contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        var nameInput by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
        val avatars = listOf("cobalt_falcon", "electric_wolf", "neon_phoenix", "gold_crown")
        var selectedAvatar by remember(profile) { mutableStateOf(profile?.avatarKey ?: "cobalt_falcon") }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it.take(40) },
                        label = { Text("Display Name") },
                        singleLine = true
                    )
                    Text("Avatar Style", style = MaterialTheme.typography.labelMedium)
                    avatars.forEach { av ->
                        FilterChip(
                            selected = selectedAvatar == av,
                            onClick = { selectedAvatar = av },
                            label = { Text(av.replace("_", " ").uppercase()) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(nameInput, selectedAvatar)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("English", "Hindi", "Spanish", "Arabic")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = {
                                onChangeLanguage(lang)
                                showLanguageDialog = false
                            },
                            label = { Text(lang) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Done") }
            }
        )
    }

    infoDialogTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null },
            title = { Text(title) },
            text = {
                Text(
                    when (title) {
                        "Terms & Conditions" -> "777Bet operates exclusively with Virtual Points (VP) for sports predictions, trivia quizzes, and social leaderboard competition. No real-money deposits, bets, or cashouts exist."
                        "Help & Support" -> "Earn 2,500 VP on sign-in, claim +250 VP daily rewards, complete weekly prediction challenges, and lock picks in the Prediction Centre. Accuracy % updates automatically as predictions settle."
                        else -> "777Bet — Premium 3D Sports Entertainment & Virtual Prediction Platform. Powered by Jetpack Compose & Firebase."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val ext = LocalBetZoneColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ext.electricBlue)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val ext = LocalBetZoneColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ext.electricBlue)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ext.subtleText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
