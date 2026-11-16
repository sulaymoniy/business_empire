package com.businessempire.game.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One bundled photo's attribution (assets/image_credits.json). */
@Serializable
data class ImageCredit(
    val imageName: String,
    val title: String,
    val author: String,
    val sourceUrl: String,
    val licence: String,
    val licenceUrl: String = "",
    /** What was changed, e.g. "cropped, resized". CC BY-SA crops stay under CC BY-SA. */
    val modified: String = "",
)

@Serializable
data class ImageCreditsFile(val photos: List<ImageCredit> = emptyList())

object ImageCreditsCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<ImageCredit> = runCatching {
        json.decodeFromString(ImageCreditsFile.serializer(), text).photos
    }.getOrDefault(emptyList())
}

class ImageCreditsRepository(private val context: Context) {
    suspend fun load(): List<ImageCredit> = withContext(Dispatchers.IO) {
        runCatching { context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() } }
            .getOrNull()
            ?.let(ImageCreditsCodec::parse)
            .orEmpty()
            .sortedBy { it.imageName }
    }

    companion object {
        const val ASSET_NAME = "image_credits.json"
    }
}
