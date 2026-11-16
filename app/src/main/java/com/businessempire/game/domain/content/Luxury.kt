package com.businessempire.game.domain.content

enum class LuxuryCategory { CAR, HOME, YACHT, JET, ISLAND, ART, EXTRA }

/**
 * Luxury items: bought once, kept forever (even through prestige).
 * [bonusPercent] permanently adds to global revenue (+1 = +1%). Counts toward net worth at [price].
 */
data class LuxuryDef(
    val id: String,
    val category: LuxuryCategory,
    val price: Double,
    val bonusPercent: Double,
    /** Photo name (see ART_LIST.md). */
    val imageName: String = "lux_$id",
)

object Luxury {
    val all: List<LuxuryDef> = listOf(
        LuxuryDef("aurora_roadster", LuxuryCategory.CAR, 5.0e4, 1.0),
        LuxuryDef("strata_gt", LuxuryCategory.CAR, 7.5e5, 2.0),
        LuxuryDef("halberd_hypercar", LuxuryCategory.CAR, 1.2e7, 3.0),
        LuxuryDef("meridian_classic", LuxuryCategory.CAR, 1.8e8, 4.0),
        LuxuryDef("lakeside_cabin", LuxuryCategory.HOME, 4.0e5, 1.0),
        LuxuryDef("hilltop_mansion", LuxuryCategory.HOME, 2.5e7, 2.0),
        LuxuryDef("chateau_belmont", LuxuryCategory.HOME, 9.0e8, 4.0),
        LuxuryDef("sky_garden_estate", LuxuryCategory.HOME, 4.0e10, 6.0),
        LuxuryDef("day_cruiser", LuxuryCategory.YACHT, 2.0e6, 1.0),
        LuxuryDef("coral_mist_yacht", LuxuryCategory.YACHT, 8.0e7, 3.0),
        LuxuryDef("leviathan_superyacht", LuxuryCategory.YACHT, 6.0e9, 5.0),
        LuxuryDef("swift_light_jet", LuxuryCategory.JET, 1.5e7, 2.0),
        LuxuryDef("stratos_long_range", LuxuryCategory.JET, 1.2e9, 4.0),
        LuxuryDef("private_airliner", LuxuryCategory.JET, 9.0e10, 6.0),
        LuxuryDef("palm_cay_island", LuxuryCategory.ISLAND, 8.0e11, 6.0),
        LuxuryDef("coral_atoll", LuxuryCategory.ISLAND, 6.0e13, 10.0),
        LuxuryDef("abstract_in_teal", LuxuryCategory.ART, 1.0e6, 1.0),
        LuxuryDef("marble_muse", LuxuryCategory.ART, 3.0e8, 3.0),
        LuxuryDef("lost_harbor_masterpiece", LuxuryCategory.ART, 2.0e10, 5.0),
        LuxuryDef("ancient_star_map", LuxuryCategory.ART, 2.0e12, 8.0),
        LuxuryDef("racehorse_stable", LuxuryCategory.EXTRA, 5.0e12, 8.0),
        LuxuryDef("lunar_retreat", LuxuryCategory.EXTRA, 5.0e14, 12.0),
    )
    val byId: Map<String, LuxuryDef> = all.associateBy { it.id }
}

/** Rank titles by best-ever net worth. */
data class RankDef(val id: String, val minNetWorth: Double)

object Ranks {
    val all: List<RankDef> = listOf(
        RankDef("street_vendor", 0.0),
        RankDef("shop_owner", 1e3),
        RankDef("local_entrepreneur", 1e5),
        RankDef("business_owner", 1e7),
        RankDef("executive", 1e9),
        RankDef("industrialist", 1e11),
        RankDef("magnate", 1e13),
        RankDef("tycoon", 1e15),
        RankDef("mogul", 1e17),
    )

    fun forNetWorth(netWorth: Double): RankDef = all.last { netWorth >= it.minNetWorth }

    fun next(netWorth: Double): RankDef? = all.firstOrNull { it.minNetWorth > netWorth }
}

object BankRules {
    /** Savings interest per in-game minute, compounding continuously. */
    const val SAVINGS_RATE_PER_MINUTE = 0.0005
    /** Loan interest per in-game minute, compounding continuously. */
    const val LOAN_RATE_PER_MINUTE = 0.002
    /** Total loans may not exceed this share of net worth. */
    const val MAX_LOAN_FRACTION = 0.5
    /** Minimum credit line so new players can try a loan. */
    const val MIN_CREDIT = 1_000.0
    /** A loan with no repayment for this long becomes overdue and blocks new borrowing. */
    const val OVERDUE_SECONDS = 30 * 60.0
}
