package com.threestrandscattle.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.CutType
import com.threestrandscattle.app.models.NotificationPreferences
import com.threestrandscattle.app.services.NotificationService
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: SaleStore, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationService = remember { NotificationService.getInstance(context) }
    val isAuthorized by notificationService.isAuthorized.collectAsState()
    val prefs by store.notificationPrefs.collectAsState()
    var showTestAlert by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationService.setAuthorized(granted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = ThemeColors.Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = ThemeColors.Primary)
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Done", color = ThemeColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeColors.Background)
            )
        },
        containerColor = ThemeColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notification Status
            SettingsSection(title = "Notification Status") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isAuthorized) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isAuthorized) ThemeColors.Bronze else ThemeColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Push Notifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = ThemeColors.TextPrimary
                        )
                        Text(
                            if (isAuthorized) "Enabled" else "Disabled",
                            style = ThemeTypography.CaptionFont,
                            color = if (isAuthorized) ThemeColors.Bronze else ThemeColors.TextSecondary
                        )
                    }
                    if (!isAuthorized) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Enable", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Notification Types
            SettingsSection(
                title = "Notification Types",
                footer = "Choose which notifications you'd like to receive from 3 Strands Cattle Co."
            ) {
                SettingsToggle(
                    icon = Icons.Filled.Bolt,
                    title = "Flash Sales",
                    color = ThemeColors.BronzeGold,
                    checked = prefs.flashSalesEnabled,
                    onCheckedChange = {
                        store.updatePreferences { p -> p.copy(flashSalesEnabled = it) }
                    }
                )
                HorizontalDivider(color = ThemeColors.CardBackground)
                SettingsToggle(
                    icon = Icons.Filled.TrendingDown,
                    title = "Price Drops",
                    color = ThemeColors.Bronze,
                    checked = prefs.priceDropsEnabled,
                    onCheckedChange = {
                        store.updatePreferences { p -> p.copy(priceDropsEnabled = it) }
                    }
                )
                HorizontalDivider(color = ThemeColors.CardBackground)
                SettingsToggle(
                    icon = Icons.Filled.AutoAwesome,
                    title = "New Arrivals",
                    color = ThemeColors.Primary,
                    checked = prefs.newArrivalsEnabled,
                    onCheckedChange = {
                        store.updatePreferences { p -> p.copy(newArrivalsEnabled = it) }
                    }
                )
                HorizontalDivider(color = ThemeColors.CardBackground)
                SettingsToggle(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Weekly Deals",
                    color = Color(0xFF4A90D9),
                    checked = prefs.weeklyDealsEnabled,
                    onCheckedChange = {
                        store.updatePreferences { p -> p.copy(weeklyDealsEnabled = it) }
                    }
                )
            }

            // Preferred Cuts
            SettingsSection(
                title = "Preferred Cuts",
                footer = "Only get notified about flash sales for cuts you care about."
            ) {
                CutType.entries.forEachIndexed { index, cut ->
                    if (index > 0) HorizontalDivider(color = ThemeColors.CardBackground)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${cut.emoji}  ${cut.displayName}",
                            fontSize = 15.sp,
                            color = ThemeColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = prefs.preferredCuts.contains(cut),
                            onCheckedChange = { isOn ->
                                store.updatePreferences { p ->
                                    val newCuts = p.preferredCuts.toMutableSet()
                                    if (isOn) newCuts.add(cut) else newCuts.remove(cut)
                                    p.copy(preferredCuts = newCuts)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ThemeColors.Primary,
                                uncheckedThumbColor = ThemeColors.TextSecondary,
                                uncheckedTrackColor = ThemeColors.CardBackground
                            )
                        )
                    }
                }
            }

            // Testing
            SettingsSection(title = "Testing") {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            notificationService.scheduleTestNotification("Test Flash Sale", "25%")
                        }
                        showTestAlert = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ThemeColors.Primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Send Test Notification",
                            fontSize = 15.sp,
                            color = ThemeColors.TextPrimary
                        )
                    }
                }
            }

            // About
            SettingsSection(
                title = "About",
                footer = "3 Strands Cattle Co. — Veteran owned. Faith driven. Florida sourced."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ThemeColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Version", fontSize = 15.sp, color = ThemeColors.TextPrimary)
                    }
                    Text("1.0.0", style = ThemeTypography.CaptionFont, color = ThemeColors.TextSecondary)
                }
                HorizontalDivider(color = ThemeColors.CardBackground)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ThemeColors.Bronze
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Website", fontSize = 15.sp, color = ThemeColors.TextPrimary)
                    }
                    Text("3strandsbeef.com", style = ThemeTypography.CaptionFont, color = ThemeColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showTestAlert) {
        AlertDialog(
            onDismissRequest = { showTestAlert = false },
            title = { Text("Test Sent!") },
            text = { Text("A test notification will appear in ~5 seconds.") },
            confirmButton = {
                TextButton(onClick = { showTestAlert = false }) {
                    Text("OK", color = ThemeColors.Primary)
                }
            },
            containerColor = ThemeColors.CardBackground,
            titleContentColor = ThemeColors.TextPrimary,
            textContentColor = ThemeColors.TextSecondary
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    footer: String? = null,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ThemeColors.TextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeDimens.ScreenPadding),
            shape = RoundedCornerShape(ThemeDimens.CornerRadius),
            colors = CardDefaults.cardColors(containerColor = ThemeColors.CardBackground)
        ) {
            content()
        }
        if (footer != null) {
            Text(
                footer,
                fontSize = 12.sp,
                color = ThemeColors.TextSecondary,
                modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding + 4.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            title,
            fontSize = 15.sp,
            color = ThemeColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ThemeColors.Primary,
                uncheckedThumbColor = ThemeColors.TextSecondary,
                uncheckedTrackColor = ThemeColors.CardBackground
            )
        )
    }
}
