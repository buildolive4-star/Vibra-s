package com.example.ui.screens.caller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentRecord
import com.example.data.model.RechargePackage
import com.example.data.model.UiState
import com.example.data.model.Wallet
import com.example.data.model.WalletTransaction
import com.example.data.model.WalletTxType
import com.example.ui.components.Glass3DCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.PrivacyWatermarkBadge
import com.example.ui.components.SecureScreenEffect
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
fun WalletScreen(
    wallet: Wallet,
    pricing: AppPricingSettings,
    transactionsState: UiState<List<WalletTransaction>>,
    paymentsState: UiState<List<PaymentRecord>>,
    onOpenRecharge: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    SecureScreenEffect(enabled = true)
    if (onBack != null) BackHandler(onBack = onBack)

    var selectedTab by remember { mutableStateOf("ALL") }
    val txs = (transactionsState as? UiState.Success)?.data ?: emptyList()
    val payments = (paymentsState as? UiState.Success)?.data ?: emptyList()

    val filteredTx = remember(txs, selectedTab) {
        when (selectedTab) {
            "RECHARGE" -> txs.filter { it.typeEnum == WalletTxType.RECHARGE }
            "GIFT" -> txs.filter { it.typeEnum == WalletTxType.GIFT }
            "CALLS" -> txs.filter { it.typeEnum == WalletTxType.AUDIO_CALL || it.typeEnum == WalletTxType.VIDEO_CALL }
            "REFUND" -> txs.filter { it.typeEnum == WalletTxType.REFUND || it.typeEnum == WalletTxType.ADMIN_ADJUSTMENT }
            else -> txs
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                        }
                    }
                    Text("Vibra Wallet", style = MaterialTheme.typography.headlineLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                }
                PrivacyWatermarkBadge(userId = wallet.userId)
            }
        }

        item {
            Glass3DCard(cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = GoldenGlow, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Available Coin Balance", style = MaterialTheme.typography.titleMedium, color = MutedLavender)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${wallet.balanceCoins} Coins",
                        style = MaterialTheme.typography.displayLarge,
                        color = GoldenGlow,
                        fontWeight = FontWeight.Bold
                    )
                    val audioMins = if (pricing.audioRatePerMin > 0) wallet.balanceCoins / pricing.audioRatePerMin else 0L
                    val videoMins = if (pricing.videoRatePerMin > 0) wallet.balanceCoins / pricing.videoRatePerMin else 0L
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Est. $audioMins min Audio (₹${pricing.audioRatePerMin}/m) • $videoMins min Video (₹${pricing.videoRatePerMin}/m)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL RECHARGED", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Text("₹${wallet.totalRechargedInr}", style = MaterialTheme.typography.titleMedium, color = EmeraldOnline)
                        }
                        Column {
                            Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                            Text("${wallet.totalSpentCoins} Coins", style = MaterialTheme.typography.titleMedium, color = NeonPink)
                        }
                        NeonGradientButton(
                            text = "Recharge",
                            onClick = onOpenRecharge,
                            modifier = Modifier.testTag("wallet_recharge_btn")
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "All", "RECHARGE" to "Recharges", "GIFT" to "Gifts", "CALLS" to "Calls", "REFUND" to "Refunds").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTab == key,
                        onClick = { selectedTab = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricViolet, selectedLabelColor = SoftWhite)
                    )
                }
            }
        }

        if (payments.isNotEmpty() && (selectedTab == "ALL" || selectedTab == "RECHARGE")) {
            item {
                Text("Gateway Confirmations", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
            }
            items(payments.take(4), key = { "pay_${it.id}" }) { pay ->
                Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${pay.gatewayEnum.displayName} • ₹${pay.amountInr}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                            Text("Ref: ${pay.gatewayRef}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                        }
                        Surface(color = EmeraldOnline.copy(0.18f), shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = pay.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldOnline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Ledger History", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
        }

        if (filteredTx.isEmpty()) {
            item {
                Glass3DCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No ledger transactions yet. Tap Recharge to add coins with verified UPI confirmation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(filteredTx, key = { it.id }) { tx ->
                val isPositive = tx.amountCoins >= 0
                Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.description, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                            Text("${tx.type} • Ref ${tx.referenceId}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isPositive) "+${tx.amountCoins}c" else "${tx.amountCoins}c",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isPositive) EmeraldOnline else CrimsonAlert,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Bal: ${tx.balanceAfter}c", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RechargeScreen(
    packagesState: UiState<List<RechargePackage>>,
    pricing: AppPricingSettings,
    wallet: Wallet,
    onBack: () -> Unit,
    onConfirmPayment: (RechargePackage, PaymentGateway, String) -> Unit
) {
    BackHandler(onBack = onBack)
    SecureScreenEffect(enabled = true)

    val packages = ((packagesState as? UiState.Success)?.data ?: emptyList()).filter { it.active }
    val displayPackages = packages.ifEmpty {
        listOf(
            RechargePackage(id = "pkg_120", title = "Starter Spark", amountInr = 120, coins = 120, bonusCoins = 0, badge = "6 Min Audio", sortOrder = 1),
            RechargePackage(id = "pkg_240", title = "Neon Plus", amountInr = 240, coins = 240, bonusCoins = 25, badge = "+25 Bonus", sortOrder = 2),
            RechargePackage(id = "pkg_500", title = "Violet Luxe", amountInr = 500, coins = 500, bonusCoins = 75, badge = "Popular", sortOrder = 3),
            RechargePackage(id = "pkg_1000", title = "Diamond Vault", amountInr = 1000, coins = 1000, bonusCoins = 200, badge = "Best Value", sortOrder = 4),
            RechargePackage(id = "pkg_2000", title = "Royal Star VIP", amountInr = 2000, coins = 2000, bonusCoins = 500, badge = "+500 VIP", sortOrder = 5)
        )
    }

    var selectedGateway by remember { mutableStateOf(PaymentGateway.RAZORPAY_UPI) }
    var checkoutPkg by remember { mutableStateOf<RechargePackage?>(null) }
    var upiInput by remember { mutableStateOf("vibra.user@okaxis") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                }
                Text("Recharge Coins", style = MaterialTheme.typography.headlineLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Glass3DCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Indian Payment Gateway", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    }
                    Text(
                        text = "Balance updates only after two-step server-side transaction verification. Current: ${wallet.balanceCoins} coins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedLavender
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentGateway.entries.forEach { gw ->
                            FilterChip(
                                selected = selectedGateway == gw,
                                onClick = { selectedGateway = gw },
                                label = { Text(gw.displayName, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalBlue, selectedLabelColor = SoftWhite)
                            )
                        }
                    }
                }
            }
        }

        items(displayPackages, key = { it.id }) { pkg ->
            val totalCoins = pkg.coins + pkg.bonusCoins
            val audioMins = if (pricing.audioRatePerMin > 0) totalCoins / pricing.audioRatePerMin else 0L
            Glass3DCard(
                cornerRadius = 20.dp,
                onClick = { checkoutPkg = pkg },
                modifier = Modifier.fillMaxWidth().testTag("recharge_pkg_${pkg.amountInr}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pkg.title, style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                            if (!pkg.badge.isNullOrBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Surface(color = NeonPink.copy(0.22f), shape = RoundedCornerShape(8.dp)) {
                                    Text(pkg.badge, style = MaterialTheme.typography.labelSmall, color = NeonPink, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$totalCoins Coins (${pkg.coins} + ${pkg.bonusCoins} bonus) • ~$audioMins min audio",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldenGlow
                        )
                    }
                    Surface(
                        color = DarkNavy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink)))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(16.dp))
                            Text("${pkg.amountInr}", style = MaterialTheme.typography.titleMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val activePkg = checkoutPkg
    if (activePkg != null) {
        AlertDialog(
            onDismissRequest = { checkoutPkg = null },
            title = { Text("Confirm ₹${activePkg.amountInr} Recharge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gateway: ${selectedGateway.displayName}")
                    Text("Credits: ${activePkg.coins + activePkg.bonusCoins} Coins after verified confirmation")
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("UPI ID / Gateway Reference") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmPayment(activePkg, selectedGateway, upiInput)
                        checkoutPkg = null
                    }
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Verify & Pay ₹${activePkg.amountInr}")
                }
            },
            dismissButton = {
                TextButton(onClick = { checkoutPkg = null }) { Text("Cancel") }
            }
        )
    }
}
