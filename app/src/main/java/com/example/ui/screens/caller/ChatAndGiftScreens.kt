package com.example.ui.screens.caller

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.GiftCategory
import com.example.data.model.HostProfile
import com.example.data.model.UiState
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
import com.example.ui.components.Glass3DCard
import com.example.ui.components.VirtualGift3DArtwork
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SoftWhite

@Composable
fun ChatHubScreen(
    threadsState: UiState<List<ChatThread>>,
    activeThread: ChatThread?,
    messagesState: UiState<List<ChatMessage>>,
    hosts: List<HostProfile>,
    currentUserId: String,
    onSelectThread: (ChatThread) -> Unit,
    onStartChatWithHost: (HostProfile) -> Unit,
    onClearThread: () -> Unit,
    onSendMessage: (text: String, messageType: String, mediaLabel: String?) -> Unit,
    onOpenGiftShop: (HostProfile) -> Unit,
    onReportOrBlock: (targetId: String, targetName: String, actionType: String) -> Unit
) {
    if (activeThread != null) {
        BackHandler { onClearThread() }
        val linkedHost = hosts.firstOrNull { it.id == activeThread.hostId }
        ChatThreadView(
            thread = activeThread,
            messages = (messagesState as? UiState.Success)?.data ?: emptyList(),
            currentUserId = currentUserId,
            onBack = onClearThread,
            onSendMessage = onSendMessage,
            onOpenGiftShop = {
                if (linkedHost != null) onOpenGiftShop(linkedHost)
            },
            onReport = { onReportOrBlock(activeThread.hostId, activeThread.hostName, "REPORT") },
            onBlock = { onReportOrBlock(activeThread.hostId, activeThread.hostName, "BLOCK") }
        )
    } else {
        val threads = (threadsState as? UiState.Success)?.data ?: emptyList()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Encrypted Vibra rooms with GIFs, media & luxury gifts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedLavender
                )
            }

            if (hosts.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(hosts.take(6), key = { "quick_chat_${it.id}" }) { host ->
                            Surface(
                                color = DarkNavy.copy(0.75f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, ElectricViolet.copy(0.45f), RoundedCornerShape(16.dp))
                                    .clickable { onStartChatWithHost(host) }
                            ) {
                                Text(
                                    text = "+ Chat ${host.name}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SoftWhite,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (threads.isEmpty()) {
                item {
                    Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Conversations Yet", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Tap any host chip above or open a host profile to start chatting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedLavender
                            )
                        }
                    }
                }
            } else {
                items(threads, key = { it.id }) { thread ->
                    Glass3DCard(
                        cornerRadius = 18.dp,
                        modifier = Modifier.fillMaxWidth().testTag("chat_thread_${thread.id}"),
                        onClick = { onSelectThread(thread) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = thread.hostName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(thread.hostName, style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                                Text(
                                    text = thread.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedLavender,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun ChatThreadView(
    thread: ChatThread,
    messages: List<ChatMessage>,
    currentUserId: String,
    onBack: () -> Unit,
    onSendMessage: (String, String, String?) -> Unit,
    onOpenGiftShop: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showEmojiBar by remember { mutableStateOf(false) }
    var showGifBar by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSendMessage("Shared photo attachment", "IMAGE", uri.lastPathSegment ?: "Photo")
        }
    }

    val emojis = remember { listOf("✨", "💜", "🔥", "🌹", "👑", "💎", "🎶", "😊") }
    val gifs = remember { listOf("NEON_WAVE.gif", "HEART_BURST.gif", "CROWN_SPARK.gif", "STAR_PARTY.gif") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(bottom = 82.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy.copy(0.75f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SoftWhite)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(thread.hostName, style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                Text("Real-time encrypted chat", style = MaterialTheme.typography.labelSmall, color = EmeraldOnline)
            }
            IconButton(onClick = onOpenGiftShop, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.CardGiftcard, contentDescription = "Send gift", tint = GoldenGlow)
            }
            IconButton(onClick = onReport, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.ReportProblem, contentDescription = "Report user", tint = GoldenGlow)
            }
            IconButton(onClick = onBlock, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Block, contentDescription = "Block user", tint = CrimsonAlert)
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isMine) ElectricViolet.copy(0.85f) else DeepSurface,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (isMine) NeonPink.copy(0.6f) else ElectricViolet.copy(0.3f),
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (msg.messageType != "TEXT") {
                                Text(
                                    text = "[${msg.messageType}] ${msg.mediaLabel ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldenGlow
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                            Text(msg.text, style = MaterialTheme.typography.bodyMedium, color = SoftWhite)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                Text(msg.deliveryStatus, style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Rounded.DoneAll, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showEmojiBar) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(emojis) { emoji ->
                    Surface(
                        color = DeepSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { inputText += emoji }
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        if (showGifBar) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gifs) { gif ->
                    Surface(
                        color = RoyalBlue.copy(0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            onSendMessage("Sent $gif", "GIF", gif)
                            showGifBar = false
                        }
                    ) {
                        Text(gif.removeSuffix(".gif"), style = MaterialTheme.typography.labelMedium, color = SoftWhite, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        // Composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkNavy.copy(0.85f))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showEmojiBar = !showEmojiBar; showGifBar = false }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Rounded.EmojiEmotions, contentDescription = "Emoji", tint = GoldenGlow)
            }
            IconButton(onClick = { showGifBar = !showGifBar; showEmojiBar = false }, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Rounded.GifBox, contentDescription = "GIF", tint = NeonPink)
            }
            IconButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Rounded.Image, contentDescription = "Pick photo", tint = ElectricViolet)
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Message ${thread.hostName}...") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    unfocusedBorderColor = ElectricViolet.copy(0.4f),
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite
                ),
                modifier = Modifier.weight(1f).testTag("chat_message_input")
            )
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText, "TEXT", null)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink)))
                    .testTag("chat_send_btn")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = SoftWhite)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftShopSheet(
    host: HostProfile,
    giftsState: UiState<List<VirtualGift>>,
    wallet: Wallet,
    onDismiss: () -> Unit,
    onSendGift: (VirtualGift) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<GiftCategory?>(null) }

    val allGifts = ((giftsState as? UiState.Success)?.data ?: emptyList()).filter { it.active }
    val filtered = if (selectedCategory == null) allGifts else allGifts.filter { it.categoryEnum == selectedCategory }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Luxury 3D Gift Shop", style = MaterialTheme.typography.headlineMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                    Text("Send to ${host.name}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                }
                Surface(color = DarkNavy, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "${wallet.balanceCoins} Coins",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldenGlow,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPink, selectedLabelColor = SoftWhite)
                    )
                }
                items(GiftCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricViolet, selectedLabelColor = SoftWhite)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(340.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { gift ->
                    Glass3DCard(
                        cornerRadius = 18.dp,
                        onClick = {
                            onSendGift(gift)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("gift_item_${gift.artKey}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VirtualGift3DArtwork(artKey = gift.artKey, accentHex = gift.accentHex, size = 58.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(gift.name, style = MaterialTheme.typography.titleSmall, color = SoftWhite, maxLines = 1)
                            Text("${gift.coinPrice} Coins", style = MaterialTheme.typography.labelMedium, color = GoldenGlow)
                        }
                    }
                }
            }
        }
    }
}
