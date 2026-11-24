package com.example.ui.screens.caller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallType
import com.example.data.model.HostProfile
import com.example.data.model.HostStatus
import com.example.data.model.OnlineStatus
import com.example.data.model.UiState
import com.example.ui.components.Glass3DCard
import com.example.ui.components.Host3DFrameAvatar
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite

enum class DiscoveryFilter(val label: String) {
    ALL("All"),
    ONLINE("Online"),
    NEW("New"),
    POPULAR("Popular"),
    RECENT("Recently Active"),
    PREMIUM("Premium"),
    AUDIO("Audio Available"),
    VIDEO("Video Available")
}

@Composable
fun HostDiscoveryScreen(
    hostsState: UiState<List<HostProfile>>,
    pricing: AppPricingSettings,
    onOpenHostProfile: (HostProfile) -> Unit,
    onStartCall: (HostProfile, CallType) -> Unit,
    onOpenGiftShop: (HostProfile) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(DiscoveryFilter.ALL) }

    val allApproved = ((hostsState as? UiState.Success)?.data ?: emptyList())
        .filter { it.hostStatusEnum == HostStatus.APPROVED }

    val filtered = remember(allApproved, searchQuery, selectedFilter) {
        allApproved.filter { host ->
            val matchesQuery = searchQuery.isBlank() ||
                host.name.contains(searchQuery, ignoreCase = true) ||
                host.hostCode.contains(searchQuery, ignoreCase = true) ||
                (host.bio ?: "").contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                DiscoveryFilter.ALL -> true
                DiscoveryFilter.ONLINE -> host.onlineStatusEnum == OnlineStatus.ONLINE
                DiscoveryFilter.NEW -> host.newHost
                DiscoveryFilter.POPULAR -> host.popularHost
                DiscoveryFilter.RECENT -> host.totalMinutes > 0L
                DiscoveryFilter.PREMIUM -> host.premiumTier
                DiscoveryFilter.AUDIO -> host.audioAvailable
                DiscoveryFilter.VIDEO -> host.videoAvailable
            }
            matchesQuery && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
            Text(
                text = "Discover Hosts",
                style = MaterialTheme.typography.headlineLarge,
                color = SoftWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by host name, ID, or vibe...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ElectricViolet) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    unfocusedBorderColor = ElectricViolet.copy(0.45f),
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite,
                    unfocusedPlaceholderColor = MutedLavender
                ),
                modifier = Modifier.fillMaxWidth().testTag("discovery_search_input")
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DiscoveryFilter.entries) { filter ->
                val selected = selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricViolet,
                        selectedLabelColor = SoftWhite,
                        containerColor = DarkNavy.copy(0.75f),
                        labelColor = MutedLavender
                    ),
                    modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No matching hosts found", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                            Spacer(Modifier.height(4.dp))
                            Text("Try clearing your search or switching filter chips.", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { host ->
                    val audioRate = if (host.audioRatePerMin > 0) host.audioRatePerMin else pricing.audioRatePerMin
                    val videoRate = if (host.videoRatePerMin > 0) host.videoRatePerMin else pricing.videoRatePerMin
                    Glass3DCard(
                        cornerRadius = 20.dp,
                        modifier = Modifier.fillMaxWidth().testTag("discovery_host_${host.id}"),
                        onClick = { onOpenHostProfile(host) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Host3DFrameAvatar(
                                name = host.name,
                                avatarKey = host.avatarKey,
                                frameTier = host.frameTierEnum,
                                onlineStatus = host.onlineStatusEnum,
                                size = 68.dp
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(host.name, style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${host.hostCode} • ${host.frameTierEnum.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldenGlow
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = host.bio ?: "Available on Vibra",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedLavender,
                                    maxLines = 2
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Audio ₹$audioRate/m • Video ₹$videoRate/m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldOnline
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onStartCall(host, CallType.AUDIO) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(ElectricViolet, RoyalBlue)))
                                ) {
                                    Icon(Icons.Rounded.Call, contentDescription = "Audio call", tint = SoftWhite)
                                }
                                IconButton(
                                    onClick = { onStartCall(host, CallType.VIDEO) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(NeonPink, ElectricViolet)))
                                ) {
                                    Icon(Icons.Rounded.Videocam, contentDescription = "Video call", tint = SoftWhite)
                                }
                                IconButton(
                                    onClick = { onOpenGiftShop(host) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkNavy)
                                        .border(1.dp, GoldenGlow.copy(0.5f), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(Icons.Rounded.CardGiftcard, contentDescription = "Send gift", tint = GoldenGlow)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HostProfileScreen(
    host: HostProfile,
    pricing: AppPricingSettings,
    onBack: () -> Unit,
    onStartCall: (HostProfile, CallType) -> Unit,
    onOpenChat: (HostProfile) -> Unit,
    onOpenGiftShop: (HostProfile) -> Unit,
    onReportOrBlock: (reason: String, details: String, actionType: String) -> Unit
) {
    BackHandler(onBack = onBack)

    var showReportDialog by remember { mutableStateOf(false) }
    var reportActionType by remember { mutableStateOf("REPORT") }
    var reportReason by remember { mutableStateOf("Inappropriate behavior") }
    var reportDetails by remember { mutableStateOf("") }

    val audioRate = if (host.audioRatePerMin > 0) host.audioRatePerMin else pricing.audioRatePerMin
    val videoRate = if (host.videoRatePerMin > 0) host.videoRatePerMin else pricing.videoRatePerMin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).testTag("host_profile_back")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SoftWhite)
            }
            Text("Host Profile", style = MaterialTheme.typography.titleLarge, color = SoftWhite)
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(14.dp))

        Glass3DCard(cornerRadius = 28.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Host3DFrameAvatar(
                    name = host.name,
                    avatarKey = host.avatarKey,
                    frameTier = host.frameTierEnum,
                    onlineStatus = host.onlineStatusEnum,
                    size = 116.dp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = host.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = DarkNavy.copy(0.75f), shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = host.hostCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedLavender,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(color = GoldenGlow.copy(0.18f), shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = "${host.frameTierEnum.label} FRAME",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldenGlow,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = host.bio ?: "Verified Vibra Host ready for audio & video conversations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedLavender
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = DarkNavy.copy(0.68f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AUDIO RATE", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Spacer(Modifier.height(4.dp))
                            Text("₹$audioRate / min", style = MaterialTheme.typography.titleMedium, color = ElectricViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        color = DarkNavy.copy(0.68f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("VIDEO RATE", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Spacer(Modifier.height(4.dp))
                            Text("₹$videoRate / min", style = MaterialTheme.typography.titleMedium, color = NeonPink, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeonGradientButton(
                        text = "Audio Call",
                        enabled = host.audioAvailable,
                        colors = listOf(ElectricViolet, RoyalBlue),
                        onClick = { onStartCall(host, CallType.AUDIO) },
                        modifier = Modifier.weight(1f).testTag("profile_audio_call"),
                        leadingIcon = { Icon(Icons.Rounded.Call, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(18.dp)) }
                    )
                    NeonGradientButton(
                        text = "Video Call",
                        enabled = host.videoAvailable,
                        colors = listOf(NeonPink, ElectricViolet),
                        onClick = { onStartCall(host, CallType.VIDEO) },
                        modifier = Modifier.weight(1f).testTag("profile_video_call"),
                        leadingIcon = { Icon(Icons.Rounded.Videocam, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenChat(host) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("profile_chat_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Message", color = SoftWhite)
                    }
                    OutlinedButton(
                        onClick = { onOpenGiftShop(host) },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("profile_gift_btn")
                    ) {
                        Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = GoldenGlow, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Send Gift", color = GoldenGlow)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    reportActionType = "REPORT"
                    showReportDialog = true
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_report_btn")
            ) {
                Icon(Icons.Rounded.ReportProblem, contentDescription = null, tint = GoldenGlow, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Report", color = SoftWhite)
            }
            OutlinedButton(
                onClick = {
                    reportActionType = "BLOCK"
                    showReportDialog = true
                },
                modifier = Modifier.weight(1f).height(48.dp).testTag("profile_block_btn")
            ) {
                Icon(Icons.Rounded.Block, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Block", color = CrimsonAlert)
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(if (reportActionType == "BLOCK") "Block ${host.name}?" else "Report ${host.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Details (optional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                    onClick = {
                        onReportOrBlock(reportReason, reportDetails, reportActionType)
                        showReportDialog = false
                    }
                ) {
                    Text(if (reportActionType == "BLOCK") "Block User" else "Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}
