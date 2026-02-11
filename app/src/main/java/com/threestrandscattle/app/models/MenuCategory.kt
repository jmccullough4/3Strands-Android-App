package com.threestrandscattle.app.models

enum class MenuCategory(val displayName: String, val icon: String) {
    PREMIUM_STEAKS("Premium Steaks", "flame"),
    ROASTS("Roasts", "oven"),
    ADDITIONAL_OFFERINGS("Additional Offerings", "leaf"),
    SPECIALTY_OFFAL("Specialty & Offal", "heart"),
    FARM_FRESH_EGGS("Farm Fresh Eggs", "egg");

    companion object {
        val itemCategories: List<Pair<String, MenuCategory>> = listOf(
            // Premium Steaks
            "Filet Mignon" to PREMIUM_STEAKS,
            "Ribeye Steak" to PREMIUM_STEAKS,
            "Sirloin Cap - Picanha" to PREMIUM_STEAKS,
            "NY Strip Steak" to PREMIUM_STEAKS,
            "Sirloin Flap Steak" to PREMIUM_STEAKS,
            "Sirloin Tip Steak" to PREMIUM_STEAKS,
            "Inside Skirt Steak" to PREMIUM_STEAKS,
            "Outside Skirt Steak" to PREMIUM_STEAKS,
            "Flank Steak" to PREMIUM_STEAKS,
            "Flat Iron Steak" to PREMIUM_STEAKS,
            "Chuck Eye Steak" to PREMIUM_STEAKS,
            "Petite Sirloin Steak" to PREMIUM_STEAKS,
            "Denver Steak" to PREMIUM_STEAKS,
            // Roasts
            "Tri Tip Roast" to ROASTS,
            "Sirloin Tip Roast" to ROASTS,
            "Eye Round Roast" to ROASTS,
            "Rump Roast - Beef" to ROASTS,
            "Chuck Roast" to ROASTS,
            // Additional Offerings
            "Oxtails - Beef" to ADDITIONAL_OFFERINGS,
            "Short Rib Bone In - Beef" to ADDITIONAL_OFFERINGS,
            "Brisket" to ADDITIONAL_OFFERINGS,
            "Ground Beef" to ADDITIONAL_OFFERINGS,
            "London Broil" to ADDITIONAL_OFFERINGS,
            "Stew Meat - Beef" to ADDITIONAL_OFFERINGS,
            "Osso Bucco - Cross Cut Shank" to ADDITIONAL_OFFERINGS,
            "Beef Belly" to ADDITIONAL_OFFERINGS,
            // Specialty & Offal
            "Beef Heart" to SPECIALTY_OFFAL,
            "Heart - Beef" to SPECIALTY_OFFAL,
            "Beef Liver" to SPECIALTY_OFFAL,
            "Liver - Beef" to SPECIALTY_OFFAL,
            "Beef Tongue" to SPECIALTY_OFFAL,
            "Marrow Bones Split - Beef" to SPECIALTY_OFFAL,
            "Soup Bones - Beef" to SPECIALTY_OFFAL,
            // Farm Fresh Eggs
            "Eggs Half Dozen" to FARM_FRESH_EGGS,
            "Eggs Dozen" to FARM_FRESH_EGGS
        )

        val excludedItems: Set<String> = setOf(
            "Beef Home Delivery",
            "Beef Pickup",
            "Market Appearance",
            "Shipping"
        )

        fun categoryFor(itemName: String): MenuCategory? {
            return itemCategories.firstOrNull { it.first == itemName }?.second
        }

        fun sortOrder(itemName: String): Int {
            return itemCategories.indexOfFirst { it.first == itemName }.let { if (it == -1) 999 else it }
        }
    }
}

data class MenuSection(
    val category: MenuCategory,
    val items: List<CatalogItem>
)
