package com.example.data.network

import android.net.Uri
import com.example.domain.model.AspectRatio
import com.example.domain.model.StockMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class StockMediaClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Pre-curated, verified high-resolution media pool for instant fallback without rate limits
    private val curatedStockMedia = mapOf(
        "technology" to listOf(
            StockMediaItem("tech_1", "image", "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1280", "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1920", "DeepMind", 1920, 1080, 7, listOf("ai", "neural", "network")),
            StockMediaItem("tech_2", "image", "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=1280", "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=1920", "RoboTech", 1920, 1080, 8, listOf("robot", "automation", "factory")),
            StockMediaItem("tech_3", "image", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1280", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1920", "Microchip", 1920, 1080, 6, listOf("processor", "hardware", "chip")),
            StockMediaItem("tech_4", "image", "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1280", "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1920", "Matrix", 1920, 1080, 7, listOf("code", "matrix", "software"))
        ),
        "space" to listOf(
            StockMediaItem("space_1", "image", "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=1280", "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=1920", "NASA Rover", 1920, 1080, 8, listOf("mars", "rover", "space")),
            StockMediaItem("space_2", "image", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1280", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1920", "Earth Orbit", 1920, 1080, 7, listOf("earth", "space", "satellite")),
            StockMediaItem("space_3", "image", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1280", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1920", "Galaxy", 1920, 1080, 8, listOf("stars", "galaxy", "nebula"))
        ),
        "medical" to listOf(
            StockMediaItem("med_1", "image", "https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=1280", "https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=1920", "Lab Bio", 1920, 1080, 6, listOf("dna", "lab", "medicine")),
            StockMediaItem("med_2", "image", "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=1280", "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=1920", "Health Bio", 1920, 1080, 7, listOf("healthcare", "doctor", "research"))
        ),
        "business" to listOf(
            StockMediaItem("biz_1", "image", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1280", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1920", "Skyscraper", 1920, 1080, 7, listOf("corporate", "skyscrapers", "business")),
            StockMediaItem("biz_2", "image", "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=1280", "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=1920", "Market Chart", 1920, 1080, 6, listOf("finance", "trading", "stock"))
        ),
        "nature" to listOf(
            StockMediaItem("nat_1", "image", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1280", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1920", "Yosemite Valley", 1920, 1080, 8, listOf("nature", "mountains", "waterfall")),
            StockMediaItem("nat_2", "image", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1280", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1920", "Misty Forest", 1920, 1080, 7, listOf("forest", "fog", "trees"))
        ),
        "urban" to listOf(
            StockMediaItem("urb_1", "image", "https://images.unsplash.com/photo-1508873696983-2df5293cb32f?w=1280", "https://images.unsplash.com/photo-1508873696983-2df5293cb32f?w=1920", "Tokyo Night", 1920, 1080, 7, listOf("city", "night", "lights")),
            StockMediaItem("urb_2", "image", "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?w=1280", "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?w=1920", "Chicago Skyline", 1920, 1080, 7, listOf("architecture", "urban", "skyline"))
        ),
        "cinematic" to listOf(
            StockMediaItem("cine_1", "image", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1280", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1920", "Cinematic Sunset", 1920, 1080, 7, listOf("sunset", "dramatic", "atmosphere")),
            StockMediaItem("cine_2", "image", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1280", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1920", "Northern Lights", 1920, 1080, 8, listOf("aurora", "sky", "epic"))
        )
    )

    suspend fun searchStockMedia(
        query: String,
        category: String,
        aspectRatio: AspectRatio,
        pexelsApiKey: String = "",
        pixabayApiKey: String = ""
    ): List<StockMediaItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<StockMediaItem>()

        // 1. Try Pexels if key provided
        if (pexelsApiKey.isNotBlank()) {
            try {
                val orientation = if (aspectRatio == AspectRatio.RATIO_9_16) "portrait" else "landscape"
                val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                val url = "https://api.pexels.com/v1/search?query=$encodedQuery&per_page=6&orientation=$orientation"
                val req = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", pexelsApiKey)
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        val photos = json.optJSONArray("photos")
                        if (photos != null) {
                            for (i in 0 until photos.length()) {
                                val p = photos.getJSONObject(i)
                                val src = p.getJSONObject("src")
                                results.add(
                                    StockMediaItem(
                                        id = "pexels_${p.optLong("id")}",
                                        type = "image",
                                        previewUrl = src.optString("medium"),
                                        downloadUrl = src.optString("large2x", src.optString("original")),
                                        photographer = p.optString("photographer", "Pexels Creator"),
                                        width = p.optInt("width", 1920),
                                        height = p.optInt("height", 1080),
                                        tags = listOf(query)
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore and proceed
            }
        }

        // 2. Try Pixabay if key provided and needed
        if (results.isEmpty() && pixabayApiKey.isNotBlank()) {
            try {
                val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                val orientation = if (aspectRatio == AspectRatio.RATIO_9_16) "vertical" else "horizontal"
                val url = "https://pixabay.com/api/?key=$pixabayApiKey&q=$encodedQuery&image_type=photo&orientation=$orientation&per_page=6"
                val req = Request.Builder().url(url).build()
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        val hits = json.optJSONArray("hits")
                        if (hits != null) {
                            for (i in 0 until hits.length()) {
                                val h = hits.getJSONObject(i)
                                results.add(
                                    StockMediaItem(
                                        id = "pixabay_${h.optLong("id")}",
                                        type = "image",
                                        previewUrl = h.optString("webformatURL"),
                                        downloadUrl = h.optString("largeImageURL"),
                                        photographer = h.optString("user", "Pixabay Creator"),
                                        width = h.optInt("imageWidth", 1920),
                                        height = h.optInt("imageHeight", 1080),
                                        tags = h.optString("tags").split(", ")
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore and fallback
            }
        }

        // 3. Guaranteed High-Quality Curated Media Fallback matching category or query keywords
        if (results.isEmpty()) {
            val matchedCategory = curatedStockMedia[category.lowercase()]
                ?: curatedStockMedia.entries.firstOrNull { entry ->
                    query.lowercase().contains(entry.key)
                }?.value
                ?: curatedStockMedia["cinematic"]!!

            results.addAll(matchedCategory)
        }

        return@withContext results
    }

    /**
     * Free AI Image generation URL via Pollinations (Zero API key required, high quality Flux/SDXL models)
     */
    fun buildPollinationsImageUrl(
        prompt: String,
        aspectRatio: AspectRatio,
        seed: Long = System.currentTimeMillis()
    ): String {
        val (w, h) = when (aspectRatio) {
            AspectRatio.RATIO_16_9 -> 1280 to 720
            AspectRatio.RATIO_9_16 -> 720 to 1280
            AspectRatio.RATIO_1_1 -> 1024 to 1024
            AspectRatio.RATIO_4_5 -> 800 to 1000
        }
        val safePrompt = URLEncoder.encode(prompt.take(200), StandardCharsets.UTF_8.toString())
        return "https://image.pollinations.ai/prompt/$safePrompt?width=$w&height=$h&seed=$seed&nologo=true"
    }
}
