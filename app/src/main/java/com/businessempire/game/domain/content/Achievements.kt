package com.businessempire.game.domain.content

enum class AchievementType {
    OWN_BUSINESS, LIFETIME_EARNINGS, NET_WORTH, TAPS, MANAGERS, PROPERTIES, LUXURY,
    STOCK_TRADES, PROFITABLE_SELLS, PRESTIGES, DAILY_STREAK, LOANS_REPAID, EVENTS,
}

/**
 * An achievement unlocks once its tracked value reaches [target].
 * Reward: [rewardCashSeconds] seconds of current income as cash (at least [MIN_CASH_REWARD]),
 * and/or a permanent +[rewardPercent]% to all revenue. Achievements survive prestige.
 */
data class AchievementDef(
    val id: String,
    val type: AchievementType,
    val target: Double,
    val businessId: String? = null,
    val rewardCashSeconds: Double = 0.0,
    val rewardPercent: Double = 0.0,
)

object Achievements {
    const val MIN_CASH_REWARD = 100.0

    val all: List<AchievementDef> = buildList {
        Businesses.all.forEach { b ->
            add(AchievementDef("own_25_${b.id}", AchievementType.OWN_BUSINESS, 25.0, b.id, rewardCashSeconds = 120.0))
        }
        listOf(1e3, 1e6, 1e9, 1e12, 1e15).forEachIndexed { i, t ->
            add(AchievementDef("earn_$i", AchievementType.LIFETIME_EARNINGS, t, rewardPercent = 2.0))
        }
        listOf(1e4, 1e7, 1e10, 1e13).forEachIndexed { i, t ->
            add(AchievementDef("networth_$i", AchievementType.NET_WORTH, t, rewardPercent = 2.0))
        }
        add(AchievementDef("taps_100", AchievementType.TAPS, 100.0, rewardCashSeconds = 60.0))
        add(AchievementDef("taps_1000", AchievementType.TAPS, 1_000.0, rewardCashSeconds = 120.0))
        add(AchievementDef("managers_1", AchievementType.MANAGERS, 1.0, rewardPercent = 1.0))
        add(AchievementDef("managers_6", AchievementType.MANAGERS, 6.0, rewardPercent = 2.0))
        add(AchievementDef("managers_12", AchievementType.MANAGERS, 12.0, rewardPercent = 3.0))
        add(AchievementDef("properties_1", AchievementType.PROPERTIES, 1.0, rewardCashSeconds = 120.0))
        add(AchievementDef("properties_6", AchievementType.PROPERTIES, 6.0, rewardPercent = 2.0))
        add(AchievementDef("properties_12", AchievementType.PROPERTIES, 12.0, rewardPercent = 3.0))
        add(AchievementDef("luxury_1", AchievementType.LUXURY, 1.0, rewardPercent = 1.0))
        add(AchievementDef("luxury_5", AchievementType.LUXURY, 5.0, rewardPercent = 2.0))
        add(AchievementDef("luxury_10", AchievementType.LUXURY, 10.0, rewardPercent = 3.0))
        add(AchievementDef("trades_1", AchievementType.STOCK_TRADES, 1.0, rewardCashSeconds = 60.0))
        add(AchievementDef("profit_sells_5", AchievementType.PROFITABLE_SELLS, 5.0, rewardCashSeconds = 120.0))
        add(AchievementDef("prestige_1", AchievementType.PRESTIGES, 1.0, rewardPercent = 5.0))
        add(AchievementDef("prestige_5", AchievementType.PRESTIGES, 5.0, rewardPercent = 10.0))
        add(AchievementDef("streak_7", AchievementType.DAILY_STREAK, 7.0, rewardPercent = 3.0))
        add(AchievementDef("loan_repaid_1", AchievementType.LOANS_REPAID, 1.0, rewardCashSeconds = 60.0))
        add(AchievementDef("events_10", AchievementType.EVENTS, 10.0, rewardPercent = 2.0))
    }
    val byId: Map<String, AchievementDef> = all.associateBy { it.id }
}
