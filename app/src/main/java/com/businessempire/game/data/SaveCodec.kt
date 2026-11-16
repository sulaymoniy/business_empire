package com.businessempire.game.data

import com.businessempire.game.domain.GameState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** The on-disk save: one JSON blob. Bump [SaveCodec.CURRENT_VERSION] and add a migration when the shape changes. */
@Serializable
data class SaveFile(
    val saveVersion: Int,
    val state: GameState,
)

/**
 * Encodes and decodes the save blob. Pure Kotlin so it can be unit tested on the JVM.
 * Decoding runs the JSON through [SaveMigrations] first, so old saves keep working.
 */
object SaveCodec {
    const val CURRENT_VERSION = 2

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
        allowSpecialFloatingPointValues = true
    }

    fun encode(state: GameState): String =
        json.encodeToString(SaveFile.serializer(), SaveFile(CURRENT_VERSION, state))

    /** Returns null if the blob is unreadable (the caller then starts a new game). */
    fun decode(text: String): GameState? = runCatching {
        val raw = json.parseToJsonElement(text).jsonObject
        val version = raw["saveVersion"]?.jsonPrimitive?.int ?: 1
        val migrated = SaveMigrations.migrate(raw, from = version, to = CURRENT_VERSION)
        json.decodeFromJsonElement(SaveFile.serializer(), migrated).state
    }.getOrNull()
}

/** Each step turns version N JSON into version N+1 JSON. */
object SaveMigrations {
    fun migrate(raw: JsonObject, from: Int, to: Int): JsonObject {
        var current = raw
        for (v in from until to) {
            current = when (v) {
                1 -> v1ToV2(current)
                else -> current
            }
        }
        return current
    }

    /**
     * Version 1 (early prototype) stored a flat object:
     * `{"saveVersion":1, "cash":…, "owned":{"lemonade_stand":10}, "managers":["lemonade_stand"],
     *   "upgrades":[…], "lifetimeEarnings":…, "lastSavedAtMs":…}`
     * Version 2 nests everything under "state" and keeps one object per business.
     */
    private fun v1ToV2(v1: JsonObject): JsonObject {
        val owned = v1["owned"]?.jsonObject ?: JsonObject(emptyMap())
        val managers = (v1["managers"] as? kotlinx.serialization.json.JsonArray)
            ?.map { it.jsonPrimitive.content }?.toSet() ?: emptySet()
        val businesses = owned.mapValues { (id, count) ->
            JsonObject(
                mapOf(
                    "owned" to count,
                    "manager" to kotlinx.serialization.json.JsonPrimitive(id in managers),
                    "running" to kotlinx.serialization.json.JsonPrimitive(id in managers),
                ),
            )
        }
        val state = buildMap {
            v1["cash"]?.let { put("cash", it) }
            v1["upgrades"]?.let { put("upgrades", it) }
            v1["lastSavedAtMs"]?.let { put("lastSavedAtMs", it) }
            put("businesses", JsonObject(businesses))
            v1["lifetimeEarnings"]?.let { earned ->
                put("runEarnings", earned)
                put("stats", JsonObject(mapOf("lifetimeEarnings" to earned)))
            }
        }
        return JsonObject(
            mapOf(
                "saveVersion" to kotlinx.serialization.json.JsonPrimitive(2),
                "state" to JsonObject(state),
            ),
        )
    }
}
