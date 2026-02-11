package com.threestrandscattle.app.services

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.threestrandscattle.app.models.CatalogItem
import com.threestrandscattle.app.models.CatalogVariation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SquareService private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("square_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val baseURL = "https://connect.squareup.com/v2"
    private val apiVersion = "2024-01-18"

    private val defaultToken = "EAAAl23jxhQmIejnibi8LPDjN9LLCkW2JhrrfnknRYoq_CuY0Kb6jJ0NRu8ucheC"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    var accessToken: String?
        get() = prefs.getString("square_token", defaultToken)
        set(value) = prefs.edit().putString("square_token", value).apply()

    suspend fun fetchCatalog(): List<CatalogItem> = withContext(Dispatchers.IO) {
        val token = accessToken ?: throw ApiException("Square token not configured")

        val allItems = mutableListOf<CatalogItem>()
        val allVariationIds = mutableListOf<String>()
        var cursor: String? = null

        // Step 1: Fetch all catalog items
        do {
            val urlBuilder = StringBuilder("$baseURL/catalog/list?types=ITEM")
            if (cursor != null) urlBuilder.append("&cursor=$cursor")

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header("Square-Version", apiVersion)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw ApiException("Square API error: ${response.code}")

            val body = response.body?.string() ?: "{}"
            val catalogResponse = gson.fromJson(body, SquareCatalogResponse::class.java)

            catalogResponse.objects?.forEach { obj ->
                val itemData = obj.itemData ?: return@forEach
                val variations = itemData.variations?.mapNotNull { variation ->
                    val varData = variation.itemVariationData ?: return@mapNotNull null
                    allVariationIds.add(variation.id)
                    CatalogVariation(
                        id = variation.id,
                        name = varData.name ?: "",
                        priceAmount = varData.priceMoney?.amount,
                        priceCurrency = varData.priceMoney?.currency,
                        pricingType = varData.pricingType,
                        quantity = null
                    )
                } ?: emptyList()

                allItems.add(CatalogItem(
                    id = obj.id,
                    name = itemData.name ?: "",
                    description = itemData.description,
                    category = itemData.categoryId,
                    variations = variations
                ))
            }

            cursor = catalogResponse.cursor
        } while (cursor != null)

        // Step 2: Fetch inventory counts
        val inventoryCounts = fetchInventoryCounts(token, allVariationIds)

        // Step 3: Merge inventory into catalog items
        allItems.map { item ->
            item.copy(variations = item.variations.map { variation ->
                val qty = inventoryCounts[variation.id]
                if (qty != null) variation.copy(quantity = qty) else variation
            })
        }
    }

    private suspend fun fetchInventoryCounts(token: String, catalogObjectIds: List<String>): Map<String, Double> =
        withContext(Dispatchers.IO) {
            if (catalogObjectIds.isEmpty()) return@withContext emptyMap()

            val inventoryCounts = mutableMapOf<String, Double>()
            val batchSize = 100

            for (startIndex in catalogObjectIds.indices step batchSize) {
                val endIndex = minOf(startIndex + batchSize, catalogObjectIds.size)
                val batchIds = catalogObjectIds.subList(startIndex, endIndex)

                val json = gson.toJson(mapOf("catalog_object_ids" to batchIds))
                val requestBody = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("$baseURL/inventory/counts/batch-retrieve")
                    .header("Square-Version", apiVersion)
                    .header("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) continue

                    val body = response.body?.string() ?: "{}"
                    val inventoryResponse = gson.fromJson(body, SquareInventoryResponse::class.java)

                    inventoryResponse.counts?.forEach { count ->
                        val qty = count.quantity?.toDoubleOrNull() ?: 0.0
                        inventoryCounts[count.catalogObjectId] =
                            (inventoryCounts[count.catalogObjectId] ?: 0.0) + qty
                    }
                } catch (e: Exception) {
                    // Continue with next batch
                }
            }

            inventoryCounts
        }

    companion object {
        @Volatile
        private var instance: SquareService? = null

        fun getInstance(context: Context): SquareService {
            return instance ?: synchronized(this) {
                instance ?: SquareService(context.applicationContext).also { instance = it }
            }
        }
    }
}

// Square API Response Models
private data class SquareCatalogResponse(
    val objects: List<SquareCatalogObject>?,
    val cursor: String?
)

private data class SquareCatalogObject(
    val id: String,
    val type: String,
    @SerializedName("item_data") val itemData: SquareItemData?
)

private data class SquareItemData(
    val name: String?,
    val description: String?,
    @SerializedName("category_id") val categoryId: String?,
    val variations: List<SquareVariation>?
)

private data class SquareVariation(
    val id: String,
    @SerializedName("item_variation_data") val itemVariationData: SquareVariationData?
)

private data class SquareVariationData(
    val name: String?,
    @SerializedName("price_money") val priceMoney: SquarePriceMoney?,
    @SerializedName("pricing_type") val pricingType: String?
)

private data class SquarePriceMoney(
    val amount: Int,
    val currency: String
)

private data class SquareInventoryResponse(
    val counts: List<SquareInventoryCount>?
)

private data class SquareInventoryCount(
    @SerializedName("catalog_object_id") val catalogObjectId: String,
    val quantity: String?
)
