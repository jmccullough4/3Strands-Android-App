package com.threestrandscattle.app.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

// API Flash Sale from dashboard (snake_case JSON)
data class ApiFlashSale(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val cut_type: String = "",
    val original_price: Double = 0.0,
    val sale_price: Double = 0.0,
    val weight_lbs: Double = 0.0,
    val starts_at: String? = null,
    val expires_at: String? = null,
    val image_system_name: String = "",
    val is_active: Boolean = false
) {
    fun toFlashSale(): FlashSale {
        return FlashSale(
            id = id.toString(),
            title = title,
            description = description,
            cutType = CutType.fromString(cut_type),
            originalPrice = original_price,
            salePrice = sale_price,
            weightLbs = weight_lbs,
            startsAt = parseDate(starts_at) ?: Date(),
            expiresAt = parseDate(expires_at) ?: Date(System.currentTimeMillis() + 86400000),
            imageSystemName = image_system_name,
            isActive = is_active
        )
    }
}

// Pop-Up Sale from dashboard
data class PopUpSale(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val address: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val starts_at: String? = null,
    val ends_at: String? = null,
    val is_active: Boolean = false
)

// Announcement from dashboard
data class Announcement(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val created_at: String? = null,
    val is_active: Boolean = false
)

// Event API model from dashboard
data class ApiEvent(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val icon: String? = null,
    val is_active: Boolean = false,
    val is_popup: Boolean? = null
) {
    fun toCattleEvent(): CattleEvent {
        return CattleEvent(
            id = id,
            title = title,
            date = parseDate(start_date) ?: Date(),
            endDate = parseDate(end_date),
            location = location ?: "",
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            icon = icon ?: "calendar.fill",
            isPopup = is_popup ?: false
        )
    }
}

// App-level event model
data class CattleEvent(
    val id: Int,
    val title: String,
    val date: Date,
    val endDate: Date?,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val icon: String,
    val isPopup: Boolean = false
)

// Inbox Item
data class InboxItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val receivedAt: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
) {
    val timeAgo: String
        get() {
            val interval = (System.currentTimeMillis() - receivedAt) / 1000
            val minutes = (interval / 60).toInt()
            val hours = minutes / 60
            val days = hours / 24
            return when {
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                minutes > 0 -> "${minutes}m ago"
                else -> "Just now"
            }
        }
}

// Catalog models (from Square API)
data class CatalogItem(
    val id: String,
    val name: String,
    val description: String?,
    val category: String?,
    var variations: List<CatalogVariation>
) {
    val lowestPrice: Int?
        get() = variations.mapNotNull { it.priceAmount }.minOrNull()

    val hasInventoryTracking: Boolean
        get() = variations.any { it.quantity != null }

    val totalQuantity: Double?
        get() = if (hasInventoryTracking) variations.mapNotNull { it.quantity }.sum() else null

    val isSoldOut: Boolean
        get() = if (hasInventoryTracking) variations.filter { it.quantity != null }.all { it.quantity!! <= 0 } else false

    val isLowStock: Boolean
        get() {
            val qty = totalQuantity ?: return false
            return qty > 0 && qty <= 5
        }

    val formattedPrice: String
        get() {
            val cents = lowestPrice ?: return "Market Price"
            if (cents <= 0) return "Market Price"
            val dollars = cents / 100.0
            return if (variations.size > 1) String.format(Locale.US, "From $%.2f", dollars)
            else String.format(Locale.US, "$%.2f", dollars)
        }
}

data class CatalogVariation(
    val id: String,
    val name: String,
    val priceAmount: Int?,
    val priceCurrency: String?,
    val pricingType: String?,
    var quantity: Double?
) {
    val isSoldOut: Boolean
        get() {
            val qty = quantity ?: return false
            return qty <= 0
        }

    val formattedPrice: String
        get() {
            val cents = priceAmount ?: return "Market Price"
            return String.format(Locale.US, "$%.2f", cents / 100.0)
        }
}

// Date parsing utility
fun parseDate(dateStr: String?): Date? {
    if (dateStr == null) return null
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getDefault() },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply { timeZone = TimeZone.getDefault() },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    )
    for (fmt in formats) {
        try {
            return fmt.parse(dateStr)
        } catch (_: Exception) { }
    }
    return null
}
