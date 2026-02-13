package com.threestrandscattle.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.CatalogItem
import com.threestrandscattle.app.models.MenuCategory
import com.threestrandscattle.app.models.MenuSection
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.services.SquareService
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(store: SaleStore) {
    val context = LocalContext.current
    var sections by remember { mutableStateOf<List<MenuSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    suspend fun loadCatalog() {
        isLoading = sections.isEmpty()
        errorMessage = null
        try {
            val allItems = SquareService.getInstance(context).fetchCatalog()
            val productItems = allItems.filter { !MenuCategory.excludedItems.contains(it.name) }

            val grouped = mutableMapOf<MenuCategory, MutableList<CatalogItem>>()
            for (item in productItems) {
                val cat = MenuCategory.categoryFor(item.name) ?: MenuCategory.ADDITIONAL_OFFERINGS
                grouped.getOrPut(cat) { mutableListOf() }.add(item)
            }

            for ((cat, items) in grouped) {
                grouped[cat] = items.sortedBy { MenuCategory.sortOrder(it.name) }.toMutableList()
            }

            sections = MenuCategory.entries.mapNotNull { cat ->
                val items = grouped[cat]
                if (!items.isNullOrEmpty()) MenuSection(cat, items) else null
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load menu"
        }
        isLoading = false
    }

    LaunchedEffect(retryTrigger) {
        loadCatalog()
        // Poll every 30 seconds
        while (true) {
            delay(30_000)
            loadCatalog()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Menu", color = ThemeColors.Primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeColors.Background)
            )
        },
        containerColor = ThemeColors.Background
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ThemeColors.Primary)
                }
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = ThemeColors.BronzeGold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        errorMessage!!,
                        fontSize = 14.sp,
                        color = ThemeColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { retryTrigger++ },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Primary)
                    ) {
                        Text("Try Again")
                    }
                }
            }
            sections.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = ThemeColors.Primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Menu Coming Soon",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Our full menu with prices is on the way.\nIn the meantime, browse our website!",
                        fontSize = 14.sp,
                        color = ThemeColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://3strandsbeef.com")))
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Bronze)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shop on Our Website", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    sections.forEach { section ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(section.category),
                                    contentDescription = null,
                                    tint = ThemeColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    section.category.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = ThemeColors.Primary
                                )
                            }
                        }

                        items(section.items) { item ->
                            MenuItemRow(item = item)
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = ThemeColors.CardBackground
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: CatalogItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.isSoldOut) ThemeColors.TextSecondary else ThemeColors.Primary,
                modifier = Modifier.weight(1f)
            )

            if (item.isSoldOut) {
                Text(
                    "Sold Out",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.Red.copy(alpha = 0.8f)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.isLowStock) {
                        Text(
                            "Low Stock",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFA500),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFA500).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        item.formattedPrice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.Bronze
                    )
                }
            }
        }

        if (item.variations.size > 1) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Options (${item.variations.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.Primary
                )
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ThemeColors.Primary
                )
            }

            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    item.variations.forEach { variation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                variation.name,
                                fontSize = 14.sp,
                                color = if (variation.isSoldOut) ThemeColors.TextSecondary
                                else ThemeColors.TextPrimary
                            )
                            if (variation.isSoldOut) {
                                Text(
                                    "Sold Out",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Red.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    variation.formattedPrice,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ThemeColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: MenuCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        MenuCategory.PREMIUM_STEAKS -> Icons.Filled.LocalFireDepartment
        MenuCategory.ROASTS -> Icons.Filled.Restaurant
        MenuCategory.ADDITIONAL_OFFERINGS -> Icons.Filled.Eco
        MenuCategory.SPECIALTY_OFFAL -> Icons.Filled.Favorite
        MenuCategory.FARM_FRESH_EGGS -> Icons.Filled.SetMeal
    }
}
