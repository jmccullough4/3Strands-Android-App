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

    companion object {
        val samples: List<FlashSale> = listOf(
            FlashSale(
                id = "sample-1",
                title = "Weekend Ribeye Blowout",
                description = "Premium Florida-raised ribeye steaks, hand-cut and dry-aged 21 days. Perfect marbling for the grill. Limited supply from our latest harvest.",
                cutType = CutType.RIBEYE,
                originalPrice = 54.99,
                salePrice = 38.99,
                weightLbs = 2.0,
                startsAt = Date(),
                expiresAt = Date(System.currentTimeMillis() + 7200000),
                imageSystemName = "flame.fill",
                isActive = true
            ),
            FlashSale(
                id = "sample-2",
                title = "Family Essentials Bundle",
                description = "Our most popular family pack: 2lb ground beef, 2 NY strips, 1 chuck roast, and 1lb stew meat. Feeds a family of 4 for a week.",
                cutType = CutType.BUNDLE,
                originalPrice = 129.99,
                salePrice = 89.99,
                weightLbs = 8.0,
                startsAt = Date(),
                expiresAt = Date(System.currentTimeMillis() + 14400000),
                imageSystemName = "shippingbox.fill",
                isActive = true
            ),
            FlashSale(
                id = "sample-3",
                title = "Grillmaster's NY Strip",
                description = "Thick-cut New York strips, perfect 1.25\" thickness. Sourced from our Florida partner ranches.",
                cutType = CutType.NY_STRIP,
                originalPrice = 44.99,
                salePrice = 32.99,
                weightLbs = 1.5,
                startsAt = Date(),
                expiresAt = Date(System.currentTimeMillis() + 3600000),
                imageSystemName = "frying.pan.fill",
                isActive = true
            ),
            FlashSale(
                id = "sample-4",
                title = "Brisket — Low & Slow Special",
                description = "Whole packer brisket, untrimmed. Perfect for smoking.",
                cutType = CutType.BRISKET,
                originalPrice = 89.99,
                salePrice = 64.99,
                weightLbs = 12.0,
                startsAt = Date(System.currentTimeMillis() - 7200000),
                expiresAt = Date(System.currentTimeMillis() + 1800000),
                imageSystemName = "thermometer.sun.fill",
                isActive = true
            ),
            FlashSale(
                id = "sample-5",
                title = "Ground Beef — Bulk Buy",
                description = "80/20 ground beef in 1lb packs. Stock your freezer.",
                cutType = CutType.GROUND_BEEF,
                originalPrice = 8.99,
                salePrice = 5.99,
                weightLbs = 1.0,
                startsAt = Date(System.currentTimeMillis() - 86400000),
                expiresAt = Date(System.currentTimeMillis() - 3600000),
                imageSystemName = "cart.fill",
                isActive = false
            )
        )
    }
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
