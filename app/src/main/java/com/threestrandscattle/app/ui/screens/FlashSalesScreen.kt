package com.threestrandscattle.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.CutType
import com.threestrandscattle.app.models.FlashSale
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.components.FlashSaleCard
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashSalesScreen(
    store: SaleStore,
    onNavigateToSaleDetail: (String) -> Unit
) {
    val sales by store.sales.collectAsState()
    var filterCut by remember { mutableStateOf<CutType?>(null) }

    val activeSales = remember(sales, filterCut) {
        val active = store.activeSales
        if (filterCut != null) active.filter { it.cutType == filterCut }
        else active
    }
    val expiredSales = remember(sales) { store.expiredSales }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flash Sales", color = ThemeColors.Primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeColors.Background)
            )
        },
        containerColor = ThemeColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = ThemeDimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            label = "All",
                            isSelected = filterCut == null,
                            onClick = { filterCut = null }
                        )
                    }
                    items(CutType.entries.toList()) { cut ->
                        FilterChip(
                            label = "${cut.emoji} ${cut.displayName}",
                            isSelected = filterCut == cut,
                            onClick = { filterCut = if (filterCut == cut) null else cut }
                        )
                    }
                }
            }

            if (activeSales.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(activeSales) { sale ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = ThemeDimens.ScreenPadding)
                            .clickable { onNavigateToSaleDetail(sale.id) }
                    ) {
                        FlashSaleCard(sale = sale, isCompact = false)
                    }
                }
            }

            // Past sales
            if (expiredSales.isNotEmpty()) {
                item {
                    Text(
                        "Past Sales",
                        style = ThemeTypography.HeadingFont,
                        color = ThemeColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = ThemeDimens.ScreenPadding)
                    )
                }
                items(expiredSales) { sale ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = ThemeDimens.ScreenPadding)
                            .fillMaxWidth()
                            .alpha(0.5f)
                    ) {
                        FlashSaleCard(sale = sale, isCompact = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isSelected) Color.White else ThemeColors.TextPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) ThemeColors.Primary else ThemeColors.CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.Bolt,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = ThemeColors.TextSecondary.copy(alpha = 0.4f)
        )
        Text(
            "No Active Sales",
            style = ThemeTypography.HeadingFont,
            color = ThemeColors.TextSecondary
        )
        Text(
            "Check back soon or enable notifications\nso you never miss a deal.",
            style = ThemeTypography.BodyFont,
            color = ThemeColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

