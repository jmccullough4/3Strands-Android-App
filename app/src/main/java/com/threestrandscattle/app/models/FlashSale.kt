package com.threestrandscattle.app.models

import java.util.Date
import java.util.Locale

data class FlashSale(
    val id: String,
    val title: String,
    val description: String,
    val cutType: CutType,
    val originalPrice: Double,
    val salePrice: Double,
    val weightLbs: Double,
    val startsAt: Date,
    val expiresAt: Date,
    val imageSystemName: String,
    val isActive: Boolean
) {
    val discountPercent: Int
        get() {
            if (originalPrice <= 0) return 0
            return ((originalPrice - salePrice) / originalPrice * 100).toInt()
        }

    val timeRemaining: String
        get() {
            val now = Date()
            if (expiresAt.before(now)) return "Expired"
            val interval = (expiresAt.time - now.time) / 1000
            val hours = interval / 3600
            val minutes = (interval % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m left"
            else "${minutes}m left"
        }

    val isExpired: Boolean
        get() = Date().after(expiresAt) || Date() == expiresAt

    val formattedOriginalPrice: String
        get() = String.format(Locale.US, "$%.2f", originalPrice)

    val formattedSalePrice: String
        get() = String.format(Locale.US, "$%.2f", salePrice)

    val pricePerLb: String
        get() = String.format(Locale.US, "$%.2f/lb", salePrice / weightLbs)

}

enum class CutType(val displayName: String, val emoji: String) {
    RIBEYE("Ribeye", "\uD83E\uDD69"),
    NY_STRIP("NY Strip", "\uD83E\uDD69"),
    FILET_MIGNON("Filet Mignon", "\uD83E\uDD69"),
    SIRLOIN("Sirloin", "\uD83E\uDD69"),
    GROUND_BEEF("Ground Beef", "\uD83C\uDF54"),
    BRISKET("Brisket", "\uD83E\uDED5"),
    ROAST("Chuck Roast", "\uD83E\uDED5"),
    T_BONE("T-Bone", "\uD83E\uDD69"),
    BUNDLE("Bundle", "\uD83D\uDCE6"),
    CUSTOM("Custom Box", "\uD83D\uDCE6");

    companion object {
        fun fromString(value: String): CutType {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: CUSTOM
        }
    }
}

data class NotificationPreferences(
    var flashSalesEnabled: Boolean = true,
    var priceDropsEnabled: Boolean = true,
    var newArrivalsEnabled: Boolean = false,
    var weeklyDealsEnabled: Boolean = true,
    var preferredCuts: MutableSet<CutType> = CutType.entries.toMutableSet()
) {
    companion object {
        const val STORAGE_KEY = "notification_preferences"
    }
}
