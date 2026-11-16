package com.businessempire.game.domain.content

/**
 * Business tiers. All balance numbers live here.
 *
 * baseCost      price of the first unit
 * costGrowth    each extra unit costs this much more (1.07 to 1.15)
 * baseRevenue   cash per cycle per unit, before multipliers
 * cycleSeconds  time for one production cycle, before milestone speed-ups
 * managerCost   one-time cost of the manager that automates this business
 * imageNames    photo per visual stage (see ART_LIST.md); the card switches at [Businesses.STAGE_THRESHOLDS]
 */
data class BusinessDef(
    val id: String,
    val baseCost: Double,
    val costGrowth: Double,
    val baseRevenue: Double,
    val cycleSeconds: Double,
    val managerCost: Double,
    val imageNames: List<String> = (1..3).map { "biz_${id}_$it" },
) {
    /** Photo for the current number of units owned: small start, busier, then the grand version. */
    fun imageName(owned: Int): String {
        val stage = Businesses.STAGE_THRESHOLDS.count { owned >= it }.coerceIn(1, imageNames.size)
        return imageNames[stage - 1]
    }
}

object Businesses {
    val all: List<BusinessDef> = listOf(
        BusinessDef("lemonade_stand", 4.0, 1.07, 1.0, 1.0, 4_000.0),
        BusinessDef("food_truck", 60.0, 1.15, 15.0, 3.0, 18_000.0),
        BusinessDef("coffee_shop", 1_300.0, 1.14, 150.0, 6.0, 390_000.0),
        BusinessDef("car_wash", 29_000.0, 1.13, 1_500.0, 12.0, 8.7e6),
        BusinessDef("bakery_chain", 640_000.0, 1.13, 15_000.0, 24.0, 1.9e8),
        BusinessDef("fitness_club", 1.4e7, 1.12, 150_000.0, 48.0, 4.2e9),
        BusinessDef("tech_startup", 3.1e8, 1.11, 1.5e6, 96.0, 9.3e10),
        BusinessDef("boutique_hotel", 6.8e9, 1.1, 1.5e7, 192.0, 2.0e12),
        BusinessDef("shipping_company", 1.5e11, 1.09, 1.5e8, 384.0, 4.5e13),
        BusinessDef("regional_airline", 3.3e12, 1.09, 1.5e9, 768.0, 9.9e14),
        BusinessDef("energy_plant", 7.2e13, 1.08, 1.5e10, 1536.0, 2.2e16),
        BusinessDef("orbital_launch", 1.6e15, 1.07, 1.8e11, 3600.0, 4.8e17),
    )

    val byId: Map<String, BusinessDef> = all.associateBy { it.id }

    /** Units owned at which a business card switches to its next photo (stage 1, 2, 3). */
    val STAGE_THRESHOLDS = listOf(0, 25, 100)

    fun indexOf(id: String): Int = all.indexOfFirst { it.id == id }

    fun get(id: String): BusinessDef = byId.getValue(id)
}

enum class MilestoneKind { SPEED, REVENUE }

data class Milestone(val owned: Int, val kind: MilestoneKind, val factor: Double = 2.0)

/** Reaching these unit counts permanently boosts that business (until prestige). */
object Milestones {
    val all: List<Milestone> = listOf(
        Milestone(25, MilestoneKind.SPEED),
        Milestone(50, MilestoneKind.SPEED),
        Milestone(100, MilestoneKind.SPEED),
        Milestone(200, MilestoneKind.REVENUE),
        Milestone(300, MilestoneKind.REVENUE),
        Milestone(400, MilestoneKind.REVENUE),
        Milestone(500, MilestoneKind.REVENUE, factor = 3.0),
    )
}
