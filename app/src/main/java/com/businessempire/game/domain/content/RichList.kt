package com.businessempire.game.domain.content

/**
 * Real-world wealth reference points for the Rich List screen.
 *
 * VERIFY: every figure here is an approximate public estimate. Check them against a current source
 * (e.g. the UBS Global Wealth Report, the US Federal Reserve Survey of Consumer Finances, Forbes or
 * Bloomberg lists) before publishing, and update them from time to time.
 */

/**
 * A step on the "compare yourself to the world" ladder (no names).
 * [richerThanPercent] drives messages like "You are now richer than 99% of people on Earth"; null = no message.
 */
data class WealthMilestone(
    val id: String,
    val netWorth: Double,
    val richerThanPercent: Double? = null,
)

object WealthMilestones {
    val all: List<WealthMilestone> = listOf(
        // VERIFY: global median wealth per adult is roughly $8-9K.
        WealthMilestone("top_half", 9_000.0, richerThanPercent = 50.0),
        // VERIFY: entry to the global top 10% of adults by wealth is roughly $140K.
        WealthMilestone("top_10_percent", 140_000.0, richerThanPercent = 90.0),
        // VERIFY: US median household net worth is roughly $190K.
        WealthMilestone("average_household", 190_000.0),
        WealthMilestone("millionaire", 1e6),
        // VERIFY: entry to the global top 1% of adults by wealth is roughly $1.1M.
        WealthMilestone("top_1_percent", 1.1e6, richerThanPercent = 99.0),
        WealthMilestone("decamillionaire", 1e7),
        // Common industry definition of "ultra-high-net-worth".
        WealthMilestone("ultra_high_net_worth", 3e7, richerThanPercent = 99.99),
        WealthMilestone("centimillionaire", 1e8),
        WealthMilestone("billionaire", 1e9),
        // VERIFY: the 100th richest person is worth roughly $25B.
        WealthMilestone("top_100", 2.5e10),
        // VERIFY: the 10th richest person is worth roughly $150B.
        WealthMilestone("top_10", 1.5e11),
        // VERIFY: the richest person is worth roughly $450B (it moves a lot).
        WealthMilestone("richest_person", 4.5e11),
    )

    fun current(netWorth: Double): WealthMilestone? = all.lastOrNull { netWorth >= it.netWorth }

    fun next(netWorth: Double): WealthMilestone? = all.firstOrNull { it.netWorth > netWorth }

    fun crossed(before: Double, after: Double): List<WealthMilestone> =
        all.filter { it.netWorth > before && it.netWorth <= after }
}

/** "Your net worth could buy N of these." */
data class Comparison(val id: String, val unitPrice: Double)

object Comparisons {
    val all: List<Comparison> = listOf(
        // VERIFY: typical new car price, roughly $48K.
        Comparison("new_car", 48_000.0),
        // VERIFY: typical US home sale price, roughly $420K.
        Comparison("average_house", 420_000.0),
        // VERIFY: large-cabin private jet, roughly $65M.
        Comparison("private_jet", 6.5e7),
        // VERIFY: large superyacht, roughly $300M.
        Comparison("superyacht", 3e8),
        // VERIFY: value of a top-league pro sports team, roughly $5B.
        Comparison("sports_team", 5e9),
        // VERIFY: yearly government budget of a small country, roughly $10B.
        Comparison("country_budget", 1e10),
    )

    /** Comparisons that give a readable count (at least 1, not absurdly large), most impressive first. */
    fun readable(netWorth: Double, max: Int = 3): List<Pair<Comparison, Long>> = all
        .map { it to (netWorth / it.unitPrice).toLong() }
        .filter { (_, n) -> n in 1..1_000_000 }
        .sortedByDescending { it.first.unitPrice }
        .take(max)
}

/**
 * Built-in fictional tycoons. Used when assets/rich_list.json is missing, broken, or real names are
 * switched off (BuildConfig.USE_REAL_RICH_LIST). Every name here is made up.
 */
object FictionalTycoons {
    data class Tycoon(val id: String, val name: String, val netWorth: Double, val descriptionKey: String)

    val all: List<Tycoon> = listOf(
        Tycoon("ada_pemberton", "Ada Pemberton", 2.0e6, "bakery"),
        Tycoon("rafael_okafor", "Rafael Okafor", 6.5e6, "food_trucks"),
        Tycoon("mina_castellano", "Mina Castellano", 1.8e7, "coffee"),
        Tycoon("hugo_lindqvist", "Hugo Lindqvist", 4.5e7, "car_washes"),
        Tycoon("priya_vantongeren", "Priya Van Tongeren", 1.2e8, "gyms"),
        Tycoon("theo_marchetti", "Theo Marchetti", 3.0e8, "hotels"),
        Tycoon("leilani_brooks", "Leilani Brooks", 7.5e8, "apps"),
        Tycoon("osric_hale", "Osric Hale", 1.6e9, "shipping"),
        Tycoon("noor_al_rashidi", "Noor Al-Rashidi", 4.0e9, "property"),
        Tycoon("benedikt_kraus", "Benedikt Kraus", 9.0e9, "airline"),
        Tycoon("sofia_marquez", "Sofia Márquez", 2.2e10, "energy"),
        Tycoon("kwame_mensah", "Kwame Mensah", 4.8e10, "chips"),
        Tycoon("evelyn_hartwell", "Evelyn Hartwell", 9.5e10, "fund"),
        Tycoon("jun_takeda", "Jun Takeda", 1.7e11, "robots"),
        Tycoon("astrid_volkova", "Astrid Volkova", 2.9e11, "space"),
        Tycoon("cornelius_vane", "Cornelius Vane", 4.6e11, "everything"),
    )
}
