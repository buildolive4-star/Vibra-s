package com.example.ui.screens.caller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.UiState
import com.example.ui.components.Glass3DCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftWhite

@Composable
fun NotificationsScreen(
    notificationsState: UiState<List<AppNotification>>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val notifications = (notificationsState as? UiState.Success)?.data ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                }
                Text("Notifications", style = MaterialTheme.typography.headlineLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
            }
        }

        if (notifications.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("All Caught Up", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                        Text("Recharges, gifts, salary payouts, and admin announcements appear here.", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                }
            }
        } else {
            items(notifications, key = { it.id }) { notif ->
                Glass3DCard(
                    cornerRadius = 18.dp,
                    modifier = Modifier.fillMaxWidth().clickable { onMarkRead(notif.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = if (notif.readFlag) MutedLavender else NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall, color = SoftWhite, fontWeight = FontWeight.Bold)
                                Surface(color = GoldenGlow.copy(0.16f), shape = MaterialTheme.shapes.extraSmall) {
                                    Text(notif.category, style = MaterialTheme.typography.labelSmall, color = GoldenGlow, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(notif.body, style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                            if (!notif.readFlag) {
                                Spacer(Modifier.height(6.dp))
                                Text("Tap to mark read", style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
                            }
                        }
                    }
                }
            }
        }
    }
}
