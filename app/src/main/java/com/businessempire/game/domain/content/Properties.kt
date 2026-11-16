package com.businessempire.game.domain.content

/**
 * Real estate. Rent is paid continuously. Value appreciates slowly while owned (capped), and
 * renovations raise the rent.
 *
 * price                 purchase price
 * rentPerMinute         base rent at renovation level 0
 * appreciationPerHour   value growth rate while owned
 */
data class PropertyDef(
    val id: String,
    val price: Double,
    val rentPerMinute: Double,
    val appreciationPerHour: Double,
    /** Photo name (see ART_LIST.md). */
    val imageName: String = "prop_$id",
)

object Properties {
    val all: List<PropertyDef> = listOf(
        PropertyDef("studio_flat", 5_000.0, 60.0, 0.03),
        PropertyDef("corner_shop", 40_000.0, 460.0, 0.03),
        PropertyDef("suburban_house", 250_000.0, 2_800.0, 0.025),
        PropertyDef("city_duplex", 1.5e6, 16_000.0, 0.025),
        PropertyDef("downtown_loft", 9.0e6, 92_000.0, 0.02),
        PropertyDef("retail_strip", 6.0e7, 590_000.0, 0.02),
        PropertyDef("apartment_block", 4.0e8, 3.8e6, 0.02),
        PropertyDef("beachfront_villa", 2.5e9, 2.3e7, 0.015),
        PropertyDef("business_park", 1.8e10, 1.6e8, 0.015),
        PropertyDef("office_tower", 1.2e11, 1.04e9, 0.015),
        PropertyDef("harbor_marina", 9.0e11, 7.6e9, 0.01),
        PropertyDef("private_island_resort", 8.0e12, 6.6e10, 0.01),
    )
    val byId: Map<String, PropertyDef> = all.associateBy { it.id }

    const val MAX_LEVEL = 5
    /** Each renovation level adds this fraction of base rent. */
    const val RENT_PER_LEVEL = 0.5
    /** Renovation to level n+1 costs price × this × (n+1). */
    const val RENOVATION_COST_FACTOR = 0.3
    /** Appreciation stops at this multiple of the purchase price. */
    const val MAX_APPRECIATION = 3.0
    /** Share of renovation spending recovered when selling. */
    const val RENOVATION_RESALE = 0.5
}
