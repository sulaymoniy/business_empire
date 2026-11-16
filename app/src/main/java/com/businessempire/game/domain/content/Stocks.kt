package com.businessempire.game.domain.content

import kotlinx.serialization.Serializable

/**
 * Sector behaviour for the random walk. Rates are per in-game hour on the log price:
 * [drift] is the long-run trend, [volatility] the random swing size.
 */
enum class Sector(val drift: Double, val volatility: Double) {
    TECH(0.05, 0.32),
    ENERGY(0.03, 0.24),
    HEALTH(0.035, 0.22),
    FINANCE(0.025, 0.16),
    CONSUMER(0.02, 0.11),
    INDUSTRIAL(0.025, 0.14),
}

/** A fictional listed company. Names and tickers are made up for this game. */
data class StockDef(
    val id: String,
    val ticker: String,
    val sector: Sector,
    val startPrice: Double,
    /** Photo name (see ART_LIST.md): a scene for the company's line of business, never a real logo. */
    val imageName: String = "stock_$id",
)

object Stocks {
    val all: List<StockDef> = listOf(
        StockDef("nimbus_cloud", "NBCX", Sector.TECH, 120.0),
        StockDef("quantis_labs", "QTSL", Sector.TECH, 64.0),
        StockDef("solace_energy", "SLCX", Sector.ENERGY, 45.0),
        StockDef("ridgeway_power", "RDGX", Sector.ENERGY, 30.0),
        StockDef("verdant_health", "VDHX", Sector.HEALTH, 88.0),
        StockDef("brightfen_bio", "BFBX", Sector.HEALTH, 22.0),
        StockDef("ironbridge_capital", "IBCX", Sector.FINANCE, 150.0),
        StockDef("maple_finch", "MFFX", Sector.CONSUMER, 54.0),
        StockDef("pellucid_apparel", "PLAX", Sector.CONSUMER, 36.0),
        StockDef("cobalt_rail", "CBRX", Sector.INDUSTRIAL, 72.0),
    )
    val byId: Map<String, StockDef> = all.associateBy { it.id }
}

/** News headline templates. [positive] decides the direction of the price shock. */
@Serializable
enum class Headline(val positive: Boolean) {
    REGULATORS_PROBE(false),
    MISSES_EARNINGS(false),
    PRODUCT_RECALL(false),
    EXECUTIVE_EXITS(false),
    BEATS_EARNINGS(true),
    MAJOR_CONTRACT(true),
    BREAKTHROUGH(true),
    ANALYST_UPGRADE(true),
}

object MarketRules {
    /** Seconds between price updates. */
    const val UPDATE_SECONDS = 3.0
    /** Price points kept per stock for the chart. */
    const val HISTORY_SIZE = 200
    /** Chance per stock per update of a news event (about one headline every 4-5 minutes overall). */
    const val NEWS_CHANCE = 0.0035
    const val NEWS_MIN_SHOCK = 0.05
    const val NEWS_MAX_SHOCK = 0.20
    /** Pull back toward the trend line, per hour. Keeps prices from drifting to zero or infinity. */
    const val MEAN_REVERSION = 0.5
    const val MIN_PRICE = 0.5
    const val NEWS_KEPT = 20
}
