package com.example.ui.screens.caller

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.AppPricingSettings
import com.example.data.model.CallType
import com.example.data.model.HostProfile
import com.example.data.model.HostStatus
import com.example.data.model.OnlineStatus
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.Wallet
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

@Composable
fun CallerHomeScreen(
    userProfileState: UiState<UserProfile?>,
    hostsState: UiState<List<HostProfile>>,
    wallet: Wallet,
    pricing: AppPricingSettings,
    notificationsState: UiState<List<AppNotification>>,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenRecharge: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDiscovery: () -> Unit,
    onOpenHostProfile: (HostProfile) -> Unit,
    onStartCall: (HostProfile, CallType) -> Unit,
    onOpenGiftShop: (HostProfile) -> Unit
) {
    val userName = (userProfileState as? UiState.Success)?.data?.displayName ?: "Explorer"
    val unreadNotifs = ((notificationsState as? UiState.Success)?.data ?: emptyList()).count { !it.readFlag }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header: Profile, Welcome, Wallet Balance, Recharge Shortcut, Notifications
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenProfile)
                        .padding(end = 8.dp)
                        .testTag("home_profile_chip")
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink)))
                            .border(1.5.dp, GoldenGlow.copy(0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedLavender
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftWhite,
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
                        color = DarkNavy.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GoldenGlow.copy(0.55f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenWallet)
                            .testTag("home_wallet_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = "Wallet balance",
                                tint = GoldenGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${wallet.balanceCoins}",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldenGlow,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink)))
                                    .clickable(onClick = onOpenRecharge)
                                    .testTag("home_recharge_shortcut"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Quick recharge",
                                    tint = SoftWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DarkNavy.copy(alpha = 0.65f))
                            .border(1.dp, ElectricViolet.copy(0.45f), CircleShape)
                            .testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = NeonPink) {
                                        Text("$unreadNotifs", color = SoftWhite)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifications",
                                tint = SoftWhite
                            )
                        }
                    }
                }
            }
        }

        // VIP Hero Card
        item {
            Glass3DCard(
                cornerRadius = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                onClick = onOpenRecharge
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_vibra_vip_emblem),
                        contentDescription = "Vibra VIP Crown",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pricing.promoLabel ?: "VIBRA LUXURY CALLING",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldenGlow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Audio ₹${pricing.audioRatePerMin}/m • Video ₹${pricing.videoRatePerMin}/m",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Instant UPI recharge • 3D framed verified hosts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedLavender
                        )
                    }
                    NeonGradientButton(
                        text = "Top Up",
                        onClick = onOpenRecharge,
                        modifier = Modifier.height(48.dp).testTag("hero_recharge_button")
                    )
                }
            }
        }

        // Hosts Content
        when (hostsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonPink)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Glass3DCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                        Text(
                            text = hostsState.message,
                            color = CrimsonAlert,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                val approved = hostsState.data.filter { it.hostStatusEnum == HostStatus.APPROVED }
                if (approved.isEmpty()) {
                    item {
                        Glass3DCard(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Approved Hosts Live Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftWhite
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Hosts added or approved in the Admin Control Center appear here in real time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedLavender
                                )
                                Spacer(Modifier.height(12.dp))
                                NeonGradientButton(
                                    text = "Explore Discovery",
                                    onClick = onOpenDiscovery
                                )
                            }
                        }
                    }
                } else {
                    val featured = approved.filter { it.featuredHost }.ifEmpty { approved.take(4) }
                    val onlineNow = approved.filter { it.onlineStatusEnum == OnlineStatus.ONLINE }
                    val newHosts = approved.filter { it.newHost }.ifEmpty { approved.takeLast(3) }
                    val popular = approved.filter { it.popularHost }.ifEmpty { approved.sortedByDescending { it.totalCalls } }
                    val recommended = approved.sortedByDescending { it.rating }
                    val premium = approved.filter { it.premiumTier }.ifEmpty { approved }
                    val recentlyActive = approved.sortedByDescending { it.totalMinutes }

                    val sections = listOf(
                        "Featured Hosts" to featured,
                        "Online Now" to onlineNow,
                        "New Hosts" to newHosts,
                        "Popular Hosts" to popular,
                        "Recommended Hosts" to recommended,
                        "Premium Hosts" to premium,
                        "Recently Active" to recentlyActive
                    )

                    sections.forEach { (title, list) ->
                        if (list.isNotEmpty()) {
                            item(key = title) {
                                HostCarouselSection(
                                    title = title,
                                    hosts = list,
                                    pricing = pricing,
                                    onSeeAll = onOpenDiscovery,
                                    onHostClick = onOpenHostProfile,
                                    onAudioCall = { onStartCall(it, CallType.AUDIO) },
                                    onVideoCall = { onStartCall(it, CallType.VIDEO) },
                                    onGiftClick = onOpenGiftShop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HostCarouselSection(
    title: String,
    hosts: List<HostProfile>,
    pricing: AppPricingSettings,
    onSeeAll: () -> Unit,
    onHostClick: (HostProfile) -> Unit,
    onAudioCall: (HostProfile) -> Unit,
    onVideoCall: (HostProfile) -> Unit,
    onGiftClick: (HostProfile) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = SoftWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = NeonPink,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(hosts, key = { "${title}_${it.id}" }) { host ->
                Host3DShowcaseCard(
                    host = host,
                    pricing = pricing,
                    onClick = { onHostClick(host) },
                    onAudioCall = { onAudioCall(host) },
                    onVideoCall = { onVideoCall(host) },
                    onGift = { onGiftClick(host) }
                )
            }
        }
    }
}

@Composable
fun Host3DShowcaseCard(
    host: HostProfile,
    pricing: AppPricingSettings,
    onClick: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onGift: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioRate = if (host.audioRatePerMin > 0) host.audioRatePerMin else pricing.audioRatePerMin
    val videoRate = if (host.videoRatePerMin > 0) host.videoRatePerMin else pricing.videoRatePerMin

    Glass3DCard(
        cornerRadius = 22.dp,
        modifier = modifier
            .width(196.dp)
            .testTag("host_card_${host.id}"),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DarkNavy.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = host.frameTierEnum.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldenGlow,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = GoldenGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", host.rating),
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftWhite
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Host3DFrameAvatar(
                name = host.name,
                avatarKey = host.avatarKey,
                frameTier = host.frameTierEnum,
                onlineStatus = host.onlineStatusEnum,
                size = 78.dp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = host.name,
                style = MaterialTheme.typography.titleMedium,
                color = SoftWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${host.hostCode} • ${host.onlineStatusEnum.name}",
                style = MaterialTheme.typography.labelSmall,
                color = if (host.onlineStatusEnum == OnlineStatus.ONLINE) EmeraldOnline else MutedLavender,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "₹$audioRate/m Audio • ₹$videoRate/m Video",
                style = MaterialTheme.typography.labelSmall,
                color = MutedLavender,
                maxLines = 1
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onAudioCall,
                    enabled = host.audioAvailable,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(ElectricViolet, RoyalBlue)))
                        .testTag("audio_call_btn_${host.id}")
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = "Audio call ${host.name}", tint = SoftWhite, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = onVideoCall,
                    enabled = host.videoAvailable,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(NeonPink, ElectricViolet)))
                        .testTag("video_call_btn_${host.id}")
                ) {
                    Icon(Icons.Rounded.Videocam, contentDescription = "Video call ${host.name}", tint = SoftWhite, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = onGift,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkNavy.copy(alpha = 0.85f))
                        .border(1.dp, GoldenGlow.copy(0.55f), RoundedCornerShape(14.dp))
                        .testTag("gift_btn_${host.id}")
                ) {
                    Icon(Icons.Rounded.CardGiftcard, contentDescription = "Gift ${host.name}", tint = GoldenGlow, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
