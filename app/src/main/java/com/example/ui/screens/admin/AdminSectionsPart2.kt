package com.example.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAuditLog
import com.example.data.model.AppPricingSettings
import com.example.data.model.Complaint
import com.example.data.model.HostAttendance
import com.example.data.model.HostEarning
import com.example.data.model.HostProfile
import com.example.data.model.HostSalary
import com.example.data.model.PaymentRecord
import com.example.data.model.PayoutMethod
import com.example.data.model.RechargePackage
import com.example.data.model.SalaryStatus
import com.example.data.model.UserProfile
import com.example.data.model.VirtualGift
import com.example.data.model.Wallet
import com.example.ui.components.AnimatedStatLineChart
import com.example.ui.components.Glass3DCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GoldenGlow
import com.example.ui.theme.MutedLavender
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftWhite
import com.example.ui.viewmodel.VibraAdminViewModel

@Composable
fun AdminRechargeManagementSection(
    payments: List<PaymentRecord>,
    packages: List<RechargePackage>,
    adminVm: VibraAdminViewModel
) {
    var editingPkg by remember { mutableStateOf<RechargePackage?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NeonGradientButton(text = "+ Add Recharge Package", onClick = { creatingNew = true }, modifier = Modifier.fillMaxWidth())
        }
        item { Text("Active Packages", style = MaterialTheme.typography.titleMedium, color = SoftWhite) }
        items(packages, key = { it.id }) { pkg ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${pkg.title} (₹${pkg.amountInr})", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("${pkg.coins} + ${pkg.bonusCoins} bonus coins • Active=${pkg.active}", style = MaterialTheme.typography.bodySmall, color = GoldenGlow)
                    }
                    OutlinedButton(onClick = { editingPkg = pkg }) { Text("Edit", color = SoftWhite) }
                }
            }
        }
        item { Text("Recent Gateway Recharges (${payments.size})", style = MaterialTheme.typography.titleMedium, color = SoftWhite) }
        items(payments, key = { it.id }) { pay ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${pay.gateway} • ₹${pay.amountInr}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("User ${pay.userId.take(10)} • Ref ${pay.gatewayRef}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                    }
                    Text("+${pay.coinsCredited}c • ${pay.status}", style = MaterialTheme.typography.labelLarge, color = EmeraldOnline)
                }
            }
        }
    }

    if (creatingNew || editingPkg != null) {
        val base = editingPkg ?: RechargePackage()
        var title by remember(base) { mutableStateOf(base.title.ifBlank { "Custom Pack" }) }
        var inr by remember(base) { mutableStateOf(base.amountInr.toString()) }
        var coins by remember(base) { mutableStateOf(base.coins.toString()) }
        var bonus by remember(base) { mutableStateOf(base.bonusCoins.toString()) }
        var badge by remember(base) { mutableStateOf(base.badge ?: "") }
        var active by remember(base) { mutableStateOf(base.active) }

        AlertDialog(
            onDismissRequest = { creatingNew = false; editingPkg = null },
            title = { Text(if (creatingNew) "New Recharge Package" else "Edit Package") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                    OutlinedTextField(value = inr, onValueChange = { inr = it.filter(Char::isDigit) }, label = { Text("Amount (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = coins, onValueChange = { coins = it.filter(Char::isDigit) }, label = { Text("Coins") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = bonus, onValueChange = { bonus = it.filter(Char::isDigit) }, label = { Text("Bonus Coins") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Badge") })
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Active")
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.saveRechargePackage(
                        pkgId = editingPkg?.id,
                        title = title,
                        amountInr = inr.toLongOrNull() ?: 120L,
                        coins = coins.toLongOrNull() ?: 120L,
                        bonusCoins = bonus.toLongOrNull() ?: 0L,
                        badge = badge,
                        active = active
                    )
                    creatingNew = false
                    editingPkg = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { creatingNew = false; editingPkg = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminWalletManagementSection(wallets: List<Wallet>, users: List<UserProfile>, adminVm: VibraAdminViewModel) {
    var adjustingWallet by remember { mutableStateOf<Wallet?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(wallets, key = { it.userId }) { w ->
            val name = users.firstOrNull { it.userId == w.userId }?.displayName ?: w.userId.take(10)
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(name, style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("Balance: ${w.balanceCoins} c • Recharged: ₹${w.totalRechargedInr}", style = MaterialTheme.typography.bodySmall, color = GoldenGlow)
                    }
                    OutlinedButton(onClick = { adjustingWallet = w }) { Text("Adjust", color = SoftWhite) }
                }
            }
        }
    }

    val target = adjustingWallet
    if (target != null) {
        var deltaText by remember { mutableStateOf("100") }
        var reason by remember { mutableStateOf("Admin goodwill credit") }
        AlertDialog(
            onDismissRequest = { adjustingWallet = null },
            title = { Text("Adjust Wallet (${target.userId.take(10)})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = deltaText, onValueChange = { deltaText = it }, label = { Text("Delta Coins (+/-)") })
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Audit Reason") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.adjustWallet(target.userId, deltaText.toLongOrNull() ?: 0L, reason)
                    adjustingWallet = null
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { adjustingWallet = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminHostEarningsSection(earnings: List<HostEarning>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(earnings, key = { it.id }) { e ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Host ${e.hostId} • ${e.category}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("Gross ${e.grossCoins}c • Commission ${e.platformCommissionCoins}c", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                    }
                    Text("+${e.hostShareCoins}c", style = MaterialTheme.typography.titleSmall, color = GoldenGlow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminSalaryManagementSection(salaries: List<HostSalary>, hosts: List<HostProfile>, adminVm: VibraAdminViewModel) {
    var showCreate by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NeonGradientButton(
                text = "+ Create / Release Host Salary",
                enabled = hosts.isNotEmpty(),
                onClick = { showCreate = true },
                modifier = Modifier.fillMaxWidth().testTag("admin_create_salary_btn")
            )
        }
        items(salaries, key = { it.id }) { sal ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${sal.hostName} (${sal.periodLabel})", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text(sal.status, style = MaterialTheme.typography.labelLarge, color = if (sal.salaryStatusEnum == SalaryStatus.PAID) EmeraldOnline else GoldenGlow)
                    }
                    Text("Approved ₹${sal.approvedAmountInr} • Paid ₹${sal.paidAmountInr} • ${sal.payoutMethod}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    val host = hosts.firstOrNull { it.id == sal.hostId }
                    if (host != null && sal.salaryStatusEnum != SalaryStatus.PAID) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                adminVm.processSalary(sal, host, sal.pendingAmountInr.coerceAtLeast(sal.approvedAmountInr).coerceAtLeast(1000L), sal.payoutMethodEnum, "UTR${System.currentTimeMillis() % 100000}", SalaryStatus.APPROVED)
                            }) { Text("Approve", color = ElectricViolet) }
                            OutlinedButton(onClick = {
                                adminVm.processSalary(sal, host, sal.approvedAmountInr.coerceAtLeast(sal.pendingAmountInr).coerceAtLeast(1000L), sal.payoutMethodEnum, "PAID-${System.currentTimeMillis() % 100000}", SalaryStatus.PAID)
                            }) { Text("Mark Paid", color = EmeraldOnline) }
                        }
                    }
                }
            }
        }
    }

    if (showCreate && hosts.isNotEmpty()) {
        var selHost by remember { mutableStateOf(hosts.first()) }
        var amount by remember { mutableStateOf("5000") }
        var method by remember { mutableStateOf(PayoutMethod.UPI) }
        var ref by remember { mutableStateOf("UPI-PAYOUT-001") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Issue Host Salary Payout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(hosts) { h -> FilterChip(selected = selHost.id == h.id, onClick = { selHost = h }, label = { Text(h.name) }) }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Amount (INR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PayoutMethod.entries.forEach { m -> FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.label) }) }
                    }
                    OutlinedTextField(value = ref, onValueChange = { ref = it }, label = { Text("Payment Reference / UTR") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.processSalary(null, selHost, amount.toLongOrNull() ?: 5000L, method, ref, SalaryStatus.PAID)
                    showCreate = false
                }) { Text("Issue & Pay") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminGiftsManagementSection(gifts: List<VirtualGift>, adminVm: VibraAdminViewModel) {
    var editingGift by remember { mutableStateOf<VirtualGift?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(gifts, key = { it.id }) { gift ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${gift.name} (${gift.category})", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("${gift.coinPrice} Coins • Active=${gift.active}", style = MaterialTheme.typography.bodySmall, color = GoldenGlow)
                    }
                    OutlinedButton(onClick = { editingGift = gift }) { Text("Price", color = SoftWhite) }
                }
            }
        }
    }

    val g = editingGift
    if (g != null) {
        var price by remember(g) { mutableStateOf(g.coinPrice.toString()) }
        var active by remember(g) { mutableStateOf(g.active) }
        AlertDialog(
            onDismissRequest = { editingGift = null },
            title = { Text("Edit ${g.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit) }, label = { Text("Coin Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Active")
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    adminVm.updateGiftPrice(g, price.toLongOrNull() ?: g.coinPrice, active)
                    editingGift = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingGift = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminCallPricingSection(pricing: AppPricingSettings, adminVm: VibraAdminViewModel) {
    var audioRate by remember(pricing.audioRatePerMin) { mutableStateOf(pricing.audioRatePerMin.toString()) }
    var videoRate by remember(pricing.videoRatePerMin) { mutableStateOf(pricing.videoRatePerMin.toString()) }
    var minSecs by remember(pricing.minBillingSeconds) { mutableStateOf(pricing.minBillingSeconds.toString()) }
    var commission by remember(pricing.platformCommissionPct) { mutableStateOf(pricing.platformCommissionPct.toString()) }
    var promoDiscount by remember(pricing.promoDiscountPct) { mutableStateOf(pricing.promoDiscountPct.toString()) }
    var promoLabel by remember(pricing.promoLabel) { mutableStateOf(pricing.promoLabel ?: "") }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Global Call & Commission Settings", style = MaterialTheme.typography.titleLarge, color = SoftWhite, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = audioRate, onValueChange = { audioRate = it.filter(Char::isDigit) }, label = { Text("Audio Rate (₹ / min)") }, modifier = Modifier.fillMaxWidth().testTag("pricing_audio_input"))
                    OutlinedTextField(value = videoRate, onValueChange = { videoRate = it.filter(Char::isDigit) }, label = { Text("Video Rate (₹ / min)") }, modifier = Modifier.fillMaxWidth().testTag("pricing_video_input"))
                    OutlinedTextField(value = minSecs, onValueChange = { minSecs = it.filter(Char::isDigit) }, label = { Text("Minimum Billing Seconds") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = commission, onValueChange = { commission = it.filter(Char::isDigit) }, label = { Text("Platform Commission %") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = promoDiscount, onValueChange = { promoDiscount = it.filter(Char::isDigit) }, label = { Text("Promo Discount %") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = promoLabel, onValueChange = { promoLabel = it }, label = { Text("Promo Banner Label") }, modifier = Modifier.fillMaxWidth())
                    NeonGradientButton(
                        text = "Publish Live Pricing",
                        onClick = {
                            val comm = (commission.toLongOrNull() ?: 30L).coerceIn(5L, 80L)
                            adminVm.updatePricing(
                                pricing.copy(
                                    audioRatePerMin = audioRate.toLongOrNull() ?: 20L,
                                    videoRatePerMin = videoRate.toLongOrNull() ?: 60L,
                                    minBillingSeconds = minSecs.toLongOrNull() ?: 60L,
                                    platformCommissionPct = comm,
                                    hostSharePct = 100L - comm,
                                    promoDiscountPct = promoDiscount.toLongOrNull() ?: 0L,
                                    promoLabel = promoLabel.ifBlank { null }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("pricing_save_btn")
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAttendanceSection(attendance: List<HostAttendance>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(attendance, key = { it.id }) { a ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${a.hostName} (${a.dateKey})", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text("In: ${a.loginTimeLabel} • Out: ${a.logoutTimeLabel ?: "Active"}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    }
                    Text("${a.workedMinutes}m • ${a.status}", style = MaterialTheme.typography.labelLarge, color = GoldenGlow)
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsSection(users: List<UserProfile>, adminVm: VibraAdminViewModel) {
    var targetUid by remember(users) { mutableStateOf(users.firstOrNull()?.userId ?: "ALL") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send Admin Announcement", style = MaterialTheme.typography.titleLarge, color = SoftWhite)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(users) { u ->
                            FilterChip(selected = targetUid == u.userId, onClick = { targetUid = u.userId }, label = { Text(u.displayName) })
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Notification Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth())
                    NeonGradientButton(
                        text = "Send Notification",
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        onClick = {
                            adminVm.sendAnnouncement(targetUid, title, body)
                            title = ""
                            body = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AdminComplaintsSection(complaints: List<Complaint>, adminVm: VibraAdminViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(complaints, key = { it.id }) { c ->
            Glass3DCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${c.actionType}: ${c.targetName}", style = MaterialTheme.typography.titleSmall, color = SoftWhite)
                        Text(c.status, style = MaterialTheme.typography.labelLarge, color = if (c.status == "RESOLVED") EmeraldOnline else CrimsonAlert)
                    }
                    Text("Reason: ${c.reason} ${c.details?.let { "• $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = MutedLavender)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { adminVm.resolveComplaint(c, "UNDER_REVIEW") }) { Text("Review", color = GoldenGlow) }
                        OutlinedButton(onClick = { adminVm.resolveComplaint(c, "RESOLVED") }) { Text("Resolve", color = EmeraldOnline) }
                        OutlinedButton(onClick = { adminVm.resolveComplaint(c, "DISMISSED") }) { Text("Dismiss", color = MutedLavender) }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFinancialReportsSection(payments: List<PaymentRecord>, earnings: List<HostEarning>, salaries: List<HostSalary>) {
    val grossInr = payments.sumOf { it.amountInr }
    val commissionCoins = earnings.sumOf { it.platformCommissionCoins }
    val paidSalariesInr = salaries.sumOf { it.paidAmountInr }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminKpiTile("Recharge Gross", "₹$grossInr", GoldenGlow, Modifier.weight(1f))
                AdminKpiTile("Platform Comm.", "$commissionCoins c", ElectricViolet, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AdminKpiTile("Salaries Paid", "₹$paidSalariesInr", EmeraldOnline, Modifier.weight(1f))
                AdminKpiTile("Net Margin", "₹${grossInr - paidSalariesInr}", NeonPink, Modifier.weight(1f))
            }
        }
        item {
            Glass3DCard(cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Monthly Revenue & Margin Curve", style = MaterialTheme.typography.titleMedium, color = SoftWhite)
                    Spacer(Modifier.height(8.dp))
                    AnimatedStatLineChart(
                        values = listOf(500f, 1200f, 1800f, 2400f, 3200f, grossInr.toFloat().coerceAtLeast(3600f)),
                        labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Now"),
                        primaryColor = EmeraldOnline,
                        secondaryColor = GoldenGlow
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSecurityAuditSection(auditLogs: List<AdminAuditLog>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(auditLogs, key = { it.id }) { log ->
            Glass3DCard(cornerRadius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${log.action} → ${log.targetCollection}/${log.targetId.take(10)}", style = MaterialTheme.typography.titleSmall, color = GoldenGlow)
                    Text(log.summary, style = MaterialTheme.typography.bodySmall, color = SoftWhite)
                    Text("Admin: ${log.adminEmail}", style = MaterialTheme.typography.labelSmall, color = MutedLavender)
                }
            }
        }
    }
}

@Composable
fun AdminSettingsSection(pricing: AppPricingSettings, adminVm: VibraAdminViewModel) {
    AdminCallPricingSection(pricing = pricing, adminVm = adminVm)
}
