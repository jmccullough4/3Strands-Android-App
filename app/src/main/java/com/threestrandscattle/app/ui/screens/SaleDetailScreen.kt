package com.threestrandscattle.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.FlashSale
import com.threestrandscattle.app.services.NotificationService
import com.threestrandscattle.app.ui.components.getIconForSale
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(sale: FlashSale, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showReminderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = ThemeColors.Primary)
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
            // Hero image area
            Box(
                modifier = Modifier
                    .padding(horizontal = ThemeDimens.ScreenPadding)
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black, ThemeColors.CardBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = getIconForSale(sale.imageSystemName),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = ThemeColors.BronzeGold
                    )

                    Text(
                        text = "${sale.discountPercent}% OFF",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemeColors.BronzeGold
                    )

                    if (!sale.isExpired) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ThemeColors.BronzeGold
                            )
                            Text(
                                sale.timeRemaining,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeColors.BronzeGold
                            )
                        }
                    } else {
                        Text(
                            "Sale Ended",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeColors.TextSecondary
                        )
                    }
                }
            }

            // Details
            Column(
                modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title and type
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${sale.cutType.emoji} ${sale.cutType.displayName}".uppercase(),
                        style = ThemeTypography.CaptionFont,
                        color = ThemeColors.TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = sale.title,
                        style = ThemeTypography.HeroFont,
                        color = ThemeColors.Primary
                    )
                }

                // Price card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ThemeDimens.CornerRadius))
                        .background(ThemeColors.Bronze.copy(alpha = 0.05f))
                        .padding(ThemeDimens.CardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Sale Price",
                            style = ThemeTypography.CaptionFont,
                            color = ThemeColors.TextSecondary
                        )
                        Text(
                            sale.formattedSalePrice,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.Bronze
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Was",
                            style = ThemeTypography.CaptionFont,
                            color = ThemeColors.TextSecondary
                        )
                        Text(
                            sale.formattedOriginalPrice,
                            fontSize = 20.sp,
                            color = ThemeColors.TextSecondary,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        icon = Icons.Filled.Scale,
                        label = "Weight",
                        value = "${String.format(Locale.US, "%.1f", sale.weightLbs)} lbs",
                        modifier = Modifier.weight(1f)
                    )
                    InfoChip(
                        icon = Icons.Filled.AttachMoney,
                        label = "Per Pound",
                        value = sale.pricePerLb,
                        modifier = Modifier.weight(1f)
                    )
                    InfoChip(
                        icon = Icons.Filled.LocalOffer,
                        label = "Savings",
                        value = "$${String.format(Locale.US, "%.2f", sale.originalPrice - sale.salePrice)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Description
                Text(
                    text = sale.description,
                    style = ThemeTypography.BodyFont,
                    color = ThemeColors.TextSecondary,
                    lineHeight = 22.sp
                )

                // CTA buttons
                if (!sale.isExpired) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { /* Deep link to website */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeDimens.CornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Bronze)
                        ) {
                            Text(
                                "Order on 3strands Website",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    NotificationService.getInstance(context)
                                        .scheduleTestNotification(sale.title, "${sale.discountPercent}%")
                                }
                                showReminderDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(ThemeDimens.CornerRadius),
                            border = androidx.compose.foundation.BorderStroke(2.dp, ThemeColors.Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeColors.Primary)
                        ) {
                            Text(
                                "Send Me a Reminder",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Reminder Set!") },
            text = { Text("You'll get a notification about this deal in a few seconds.") },
            confirmButton = {
                TextButton(onClick = { showReminderDialog = false }) {
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
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ThemeColors.CardBackground)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ThemeColors.Primary
        )
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = ThemeColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.TextPrimary
        )
    }
}
