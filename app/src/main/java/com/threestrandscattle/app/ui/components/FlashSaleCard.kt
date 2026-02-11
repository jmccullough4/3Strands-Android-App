package com.threestrandscattle.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.models.FlashSale
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import java.util.Locale

@Composable
fun FlashSaleCard(
    sale: FlashSale,
    isCompact: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(ThemeDimens.CornerRadius))
        .background(ThemeColors.CardBackground)
        .then(
            if (!sale.isExpired) Modifier.border(
                1.dp,
                ThemeColors.BronzeGold.copy(alpha = 0.3f),
                RoundedCornerShape(ThemeDimens.CornerRadius)
            ) else Modifier
        )
        .padding(ThemeDimens.CardPadding)

    Column(
        modifier = if (onClick != null) {
            Modifier.then(cardModifier)
        } else cardModifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with icon and timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ThemeColors.Bronze.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForSale(sale.imageSystemName),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ThemeColors.Bronze
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sale.cutType.displayName,
                    style = ThemeTypography.CaptionFont,
                    color = ThemeColors.TextSecondary
                )
                Text(
                    text = sale.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.Primary,
                    maxLines = if (isCompact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!sale.isExpired) {
                TimerBadge(timeRemaining = sale.timeRemaining)
            }
        }

        // Description (expanded mode only)
        if (!isCompact) {
            Text(
                text = sale.description,
                style = ThemeTypography.BodyFont,
                color = ThemeColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Price row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = sale.formattedSalePrice,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.Bronze
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = sale.formattedOriginalPrice,
                fontSize = 14.sp,
                color = ThemeColors.TextSecondary,
                textDecoration = TextDecoration.LineThrough
            )

            Spacer(modifier = Modifier.weight(1f))

            // Discount badge
            Text(
                text = "${sale.discountPercent}% OFF",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ThemeColors.Primary)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Weight info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Scale,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = ThemeColors.TextSecondary
            )
            Text(
                text = "${String.format(Locale.US, "%.1f", sale.weightLbs)} lbs",
                style = ThemeTypography.CaptionFont,
                color = ThemeColors.TextSecondary
            )
            Text(
                text = "•",
                style = ThemeTypography.CaptionFont,
                color = ThemeColors.TextSecondary
            )
            Text(
                text = sale.pricePerLb,
                style = ThemeTypography.CaptionFont,
                color = ThemeColors.TextSecondary
            )
        }
    }
}

@Composable
fun TimerBadge(timeRemaining: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ThemeColors.BronzeGold.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = ThemeColors.BronzeGold
        )
        Text(
            text = timeRemaining,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ThemeColors.BronzeGold
        )
    }
}

fun getIconForSale(systemName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        systemName.contains("flame") -> Icons.Filled.LocalFireDepartment
        systemName.contains("shipping") || systemName.contains("box") -> Icons.Filled.Inventory2
        systemName.contains("frying") || systemName.contains("pan") -> Icons.Filled.Restaurant
        systemName.contains("thermometer") -> Icons.Filled.Thermostat
        systemName.contains("cart") -> Icons.Filled.ShoppingCart
        else -> Icons.Filled.LocalOffer
    }
}
