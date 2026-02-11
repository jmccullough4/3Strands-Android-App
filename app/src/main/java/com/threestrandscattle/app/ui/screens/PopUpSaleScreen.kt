package com.threestrandscattle.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.PopUpSale
import com.threestrandscattle.app.models.parseDate
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopUpSaleScreen(store: SaleStore, onBack: () -> Unit) {
    val popUpSales by store.popUpSales.collectAsState()
    val isLoading by store.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pop-Up Sales", color = ThemeColors.Primary) },
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
        when {
            isLoading && popUpSales.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ThemeColors.Primary)
                }
            }
            popUpSales.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.PinDrop,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = ThemeColors.Primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Pop-Up Sales Right Now",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Check back soon for our next location!",
                        fontSize = 14.sp,
                        color = ThemeColors.TextSecondary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(popUpSales) { sale ->
                        PopUpSaleRow(sale = sale)
                    }
                }
            }
        }
    }
}

@Composable
private fun PopUpSaleRow(sale: PopUpSale) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ThemeDimens.CornerRadius),
        colors = CardDefaults.cardColors(containerColor = ThemeColors.CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.size(24.dp),
                        tint = ThemeColors.Primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sale.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.Primary
                    )
                    sale.starts_at?.let { dateStr ->
                        val formatted = formatPopUpDate(dateStr)
                        Text(
                            formatted,
                            fontSize = 12.sp,
                            color = ThemeColors.BronzeGold
                        )
                    }
                }
            }

            if (!sale.address.isNullOrEmpty()) {
                Text(
                    sale.address,
                    fontSize = 14.sp,
                    color = ThemeColors.TextSecondary
                )
            }

            if (!sale.description.isNullOrEmpty()) {
                Text(
                    sale.description,
                    fontSize = 12.sp,
                    color = ThemeColors.TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${sale.latitude},${sale.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${sale.latitude},${sale.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val uri = Uri.parse("geo:${sale.latitude},${sale.longitude}?q=${sale.latitude},${sale.longitude}(${Uri.encode(sale.title)})")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Bronze),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun formatPopUpDate(isoString: String): String {
    val date = parseDate(isoString) ?: return isoString
    val format = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
    return format.format(date)
}
