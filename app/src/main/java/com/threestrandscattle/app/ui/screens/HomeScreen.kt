package com.threestrandscattle.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.R
import com.threestrandscattle.app.models.InboxItem
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.components.FlashSaleCard
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    store: SaleStore,
    onNavigateToSettings: () -> Unit,
    onNavigateToSaleDetail: (String) -> Unit,
    onNavigateToPopUpSales: () -> Unit
) {
    val sales by store.sales.collectAsState()
    val popUpSales by store.popUpSales.collectAsState()
    val inboxItems by store.inboxItems.collectAsState()
    val activeSales = remember(sales) { store.activeSales }
    val homeNotifications = remember(inboxItems) { store.homeNotifications }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ecclesiastes 4:12", color = ThemeColors.Primary, fontSize = 17.sp) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = ThemeColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeColors.Background
                )
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
            // Hero Banner
            HeroBanner(activeSalesCount = activeSales.size)

            // Notification Banners
            if (homeNotifications.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    homeNotifications.forEach { item ->
                        key(item.id) {
                            NotificationBanner(
                                item = item,
                                onDismiss = { store.dismissFromHome(item.id) }
                            )
                        }
                    }
                }
            }

            // Active Flash Sales
            if (activeSales.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = ThemeColors.BronzeGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Live Flash Sales",
                            style = ThemeTypography.HeadingFont,
                            color = ThemeColors.Primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "${activeSales.size} active",
                            style = ThemeTypography.CaptionFont,
                            color = ThemeColors.TextSecondary
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = ThemeDimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(activeSales) { sale ->
                            Box(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clickable { onNavigateToSaleDetail(sale.id) }
                            ) {
                                FlashSaleCard(sale = sale)
                            }
                        }
                    }
                }
            }

            // Pop-Up Sales
            if (popUpSales.isNotEmpty()) {
                PopUpSalesSection(store = store, onNavigateToPopUpSales = onNavigateToPopUpSales)
            }

            // Values section
            ValuesSection()

            // Quick Links
            QuickLinksSection()

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun HeroBanner(activeSalesCount: Int) {
    Box(
        modifier = Modifier
            .padding(horizontal = ThemeDimens.ScreenPadding)
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.Black, ThemeColors.CardBackground)
                )
            )
            .border(1.dp, ThemeColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.copper_logo),
                contentDescription = "3 Strands Cattle Co.",
                modifier = Modifier.size(132.dp)
            )

            Text(
                text = "Veteran Owned  •  Faith Driven  •  Florida Sourced",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.TextSecondary
            )

            if (activeSalesCount > 0) {
                Text(
                    text = "$activeSalesCount Flash Sale${if (activeSalesCount == 1) "" else "s"} Live Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ThemeColors.BronzeGold)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationBanner(item: InboxItem, onDismiss: () -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < -100) {
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    }
                ) { _, dragAmount ->
                    if (dragAmount < 0) offsetX += dragAmount
                }
            }
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ThemeColors.CardBackground.copy(alpha = 0.95f))
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "3 Strands Cattle Co.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.TextSecondary
                    )
                    Text(
                        item.timeAgo,
                        fontSize = 12.sp,
                        color = ThemeColors.TextSecondary
                    )
                }
                Text(
                    item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.TextPrimary
                )
                Text(
                    item.body,
                    fontSize = 14.sp,
                    color = ThemeColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Dismiss button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Dismiss",
                modifier = Modifier.size(10.dp),
                tint = ThemeColors.TextSecondary
            )
        }
    }
}

@Composable
private fun PopUpSalesSection(store: SaleStore, onNavigateToPopUpSales: () -> Unit) {
    val popUpSales by store.popUpSales.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.PinDrop,
                contentDescription = null,
                tint = ThemeColors.BronzeGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Pop-Up Sales",
                style = ThemeTypography.HeadingFont,
                color = ThemeColors.Primary
            )
        }

        popUpSales.forEach { sale ->
            Card(
                modifier = Modifier
                    .padding(horizontal = ThemeDimens.ScreenPadding)
                    .fillMaxWidth()
                    .clickable { onNavigateToPopUpSales() },
                shape = RoundedCornerShape(ThemeDimens.CornerRadius),
                colors = CardDefaults.cardColors(containerColor = ThemeColors.CardBackground)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ThemeColors.Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = ThemeColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            sale.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeColors.Primary
                        )
                        if (!sale.address.isNullOrEmpty()) {
                            Text(
                                sale.address,
                                style = ThemeTypography.CaptionFont,
                                color = ThemeColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        "Directions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ThemeColors.Bronze)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ValuesSection() {
    Column(
        modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Our Mission",
            style = ThemeTypography.HeadingFont,
            color = ThemeColors.Primary
        )

        Text(
            text = "Our mission is simple: glorify God through honest business, support local farmers, and provide your family with beef you can trust. Every cut is traceable, every relationship is stewarded with integrity, and a portion of every sale supports local food banks and ministries.",
            style = ThemeTypography.BodyFont,
            color = ThemeColors.TextPrimary,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ThemeDimens.CornerRadius))
                .background(ThemeColors.CardBackground)
                .padding(20.dp)
        )
    }
}

@Composable
private fun QuickLinksSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Quick Links",
            style = ThemeTypography.HeadingFont,
            color = ThemeColors.Primary
        )

        QuickLinkButton(
            icon = Icons.Filled.Language,
            title = "Visit Our Website",
            subtitle = "3strandsbeef.com",
            color = ThemeColors.Bronze,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://3strandsbeef.com")))
            }
        )
        QuickLinkButton(
            icon = Icons.Filled.Email,
            title = "Email Us",
            subtitle = "info@3strands.co",
            color = ThemeColors.Primary,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@3strands.co")))
            }
        )
        QuickLinkButton(
            icon = Icons.Filled.Phone,
            title = "Call Us",
            subtitle = "(863) 799-3300",
            color = ThemeColors.Bronze,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:8637993300")))
            }
        )
    }
}

@Composable
private fun QuickLinkButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ThemeDimens.CornerRadius),
        colors = CardDefaults.cardColors(containerColor = ThemeColors.CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.Primary
                )
                Text(
                    subtitle,
                    style = ThemeTypography.CaptionFont,
                    color = ThemeColors.TextSecondary
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = ThemeColors.TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}
