package com.businessempire.game.domain.content

enum class UpgradeKind { BUSINESS_REVENUE, GLOBAL_REVENUE, OFFLINE_CAP }

/**
 * A one-time cash upgrade.
 * BUSINESS_REVENUE: multiplies [businessId]'s revenue by [factor].
 * GLOBAL_REVENUE:   multiplies all business revenue by [factor].
 * OFFLINE_CAP:      adds [factor] hours to the offline-earnings cap.
 */
data class UpgradeDef(
    val id: String,
    val kind: UpgradeKind,
    val cost: Double,
    val factor: Double,
    val businessId: String? = null,
)

object Upgrades {
    /** Per-business ×3 upgrades cost the business's base cost times these. */
    private val BUSINESS_COST_STEPS = listOf(1e3, 1e5, 1e7, 1e9, 1e11)
    private const val BUSINESS_FACTOR = 3.0

    private val GLOBAL_COSTS = listOf(5e5, 5e7, 5e9, 5e11, 5e13, 5e15, 5e17)
    private const val GLOBAL_FACTOR = 2.0

    private val OFFLINE_COSTS = listOf(5e4, 5e7, 5e10)
    private const val OFFLINE_HOURS = 2.0

    const val BASE_OFFLINE_CAP_HOURS = 4.0

    val all: List<UpgradeDef> = buildList {
        Businesses.all.forEach { b ->
            BUSINESS_COST_STEPS.forEachIndexed { i, step ->
                add(UpgradeDef("${b.id}_x3_$i", UpgradeKind.BUSINESS_REVENUE, b.baseCost * step, BUSINESS_FACTOR, b.id))
            }
        }
        GLOBAL_COSTS.forEachIndexed { i, cost ->
            add(UpgradeDef("global_x2_$i", UpgradeKind.GLOBAL_REVENUE, cost, GLOBAL_FACTOR))
        }
        OFFLINE_COSTS.forEachIndexed { i, cost ->
            add(UpgradeDef("offline_cap_$i", UpgradeKind.OFFLINE_CAP, cost, OFFLINE_HOURS))
        }
    }.sortedBy { it.cost }

    val byId: Map<String, UpgradeDef> = all.associateBy { it.id }
}
