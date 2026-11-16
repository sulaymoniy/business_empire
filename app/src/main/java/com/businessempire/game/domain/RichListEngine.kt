package com.businessempire.game.domain

import com.businessempire.game.domain.content.FictionalTycoons

/**
 * One person on the Rich List.
 * Real people: name, approximate net worth and a neutral one-line description only. [imageName] names an
 * optional licensed photo (`person_<slug>`, see ART_LIST.md); without it the UI shows initials.
 * Fictional tycoons have a [descriptionKey] and always use initials.
 */
data class RichPerson(
    val name: String,
    val netWorth: Double,
    /** Free text for real people (from rich_list.json). */
    val description: String? = null,
    /** String-resource key for fictional tycoons. */
    val descriptionKey: String? = null,
    val real: Boolean,
    val imageName: String? = null,
)

data class RichList(
    /** Richest first. */
    val people: List<RichPerson>,
    val real: Boolean,
    /** Date the figures were collected, shown in the disclaimer. */
    val asOf: String? = null,
)

/** Where the player stands on the Rich List. */
object RichListEngine {

    val fictional: RichList = RichList(
        people = FictionalTycoons.all
            .map { RichPerson(it.name, it.netWorth, descriptionKey = it.descriptionKey, real = false) }
            .sortedByDescending { it.netWorth },
        real = false,
    )

    /** 1-based position the player would have on the list (1 = richer than everyone). */
    fun playerRank(list: RichList, netWorth: Double): Int = list.people.count { it.netWorth > netWorth } + 1

    /** The poorest person still richer than the player, or null if the player tops the list. */
    fun nextToBeat(list: RichList, netWorth: Double): RichPerson? =
        list.people.filter { it.netWorth > netWorth }.minByOrNull { it.netWorth }

    /** People the player overtook when their net worth went from [before] to [after], richest first. */
    fun passed(list: RichList, before: Double, after: Double): List<RichPerson> =
        list.people.filter { it.netWorth > before && it.netWorth <= after }

    /** Photo name for a real person: "Carlos Slim Helú" -> "person_carlos_slim_helu". */
    fun photoName(name: String): String {
        val ascii = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        return "person_$ascii"
    }

    /** Initials for the avatar circle: "Ada Pemberton" -> "AP". */
    fun initials(name: String): String = name
        .split(' ')
        .filter { it.isNotBlank() && it.first().isLetter() }
        .let { parts -> if (parts.size >= 2) listOf(parts.first(), parts.last()) else parts }
        .joinToString("") { it.first().uppercase() }
        .take(2)
}
