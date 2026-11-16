package com.businessempire.game.data

import android.content.Context
import com.businessempire.game.domain.RichList
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.RichPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Shape of app/src/main/assets/rich_list.json. */
@Serializable
data class RichListFile(
    val asOf: String,
    val note: String? = null,
    val people: List<RichListEntry>,
)

@Serializable
data class RichListEntry(
    val name: String,
    val netWorthUsd: Double,
    val description: String,
)

/** Parses rich_list.json. Pure Kotlin so it can be tested on the JVM. */
object RichListCodec {
    private val json = Json { ignoreUnknownKeys = true }

    /** Returns null if the text is not a usable list. */
    fun parse(text: String): RichList? = runCatching {
        val file = json.decodeFromString(RichListFile.serializer(), text)
        val people = file.people
            .filter { it.name.isNotBlank() && it.netWorthUsd > 0 && it.netWorthUsd.isFinite() }
            .map {
                RichPerson(
                    it.name.trim(),
                    it.netWorthUsd,
                    description = it.description.trim(),
                    real = true,
                    imageName = RichListEngine.photoName(it.name.trim()),
                )
            }
            .sortedByDescending { it.netWorth }
        if (people.isEmpty()) null else RichList(people, real = true, asOf = file.asOf)
    }.getOrNull()
}

/**
 * Loads the Rich List. Real names come from assets/rich_list.json; if the file is missing or broken,
 * or [useRealNames] is false, the built-in fictional tycoons are used instead.
 */
class RichListRepository(private val context: Context, private val useRealNames: Boolean) {
    suspend fun load(): RichList = withContext(Dispatchers.IO) {
        if (!useRealNames) return@withContext RichListEngine.fictional
        val text = runCatching {
            context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        }.getOrNull()
        text?.let(RichListCodec::parse) ?: RichListEngine.fictional
    }

    companion object {
        const val ASSET_NAME = "rich_list.json"
    }
}
