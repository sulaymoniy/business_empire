package com.businessempire.game.domain

import com.businessempire.game.domain.content.Headline
import com.businessempire.game.domain.content.MarketRules
import com.businessempire.game.domain.content.Properties
import com.businessempire.game.domain.content.PropertyDef
import com.businessempire.game.domain.content.Stocks
import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class MarketState(
    val stocks: Map<String, StockState> = emptyMap(),
    val news: List<NewsItem> = emptyList(),
    /** Seconds accumulated toward the next price update. */
    val timer: Double = 0.0,
)

@Serializable
data class StockState(
    val price: Double,
    /** Long-run trend price the walk is pulled toward. */
    val trend: Double,
    val history: List<Double> = emptyList(),
    val shares: Double = 0.0,
    val avgCost: Double = 0.0,
) {
    val holdingValue: Double get() = shares * price
    val profit: Double get() = (price - avgCost) * shares
}

@Serializable
data class NewsItem(
    val stockId: String,
    val headline: Headline,
    /** Price change caused by the news, e.g. -0.15 for -15%. */
    val change: Double,
    /** Game clock when it happened. */
    val at: Double,
)

@Serializable
data class PropertyState(
    val level: Int = 0,
    val ownedSeconds: Double = 0.0,
    val renovationSpent: Double = 0.0,
)

/** Stock market rules: random walk with drift, mean reversion and news shocks. */
object MarketEngine {

    /** A fresh market with some pre-generated history so charts aren't empty. */
    fun initialMarket(rng: Rng): MarketState {
        var market = MarketState(
            stocks = Stocks.all.associate { it.id to StockState(price = it.startPrice, trend = it.startPrice) },
        )
        repeat(120) { market = step(market, rng, clock = 0.0, allowNews = false).first }
        return market
    }

    /** Advances the market by [dt] seconds. Returns the new market and any news that happened. */
    fun advance(market: MarketState, dt: Double, rng: Rng, clock: Double): Pair<MarketState, List<NewsItem>> {
        var m = market.copy(timer = market.timer + dt)
        val news = mutableListOf<NewsItem>()
        var steps = 0
        while (m.timer >= MarketRules.UPDATE_SECONDS && steps < MAX_STEPS_PER_ADVANCE) {
            val (next, fresh) = step(m.copy(timer = m.timer - MarketRules.UPDATE_SECONDS), rng, clock, allowNews = true)
            m = next
            news += fresh
            steps++
        }
        // Drop backlog we didn't simulate (e.g. a very long frame) instead of looping forever.
        if (steps == MAX_STEPS_PER_ADVANCE) m = m.copy(timer = 0.0)
        return m to news
    }

    fun step(market: MarketState, rng: Rng, clock: Double, allowNews: Boolean): Pair<MarketState, List<NewsItem>> {
        val dtH = MarketRules.UPDATE_SECONDS / 3600.0
        val fresh = mutableListOf<NewsItem>()
        val stocks = market.stocks.mapValues { (id, s) ->
            val def = Stocks.byId[id] ?: return@mapValues s
            val drift = def.sector.drift
            val vol = def.sector.volatility
            val trend = s.trend * exp(drift * dtH)
            val pull = MarketRules.MEAN_REVERSION * (ln(trend) - ln(s.price))
            var price = s.price * exp((drift + pull) * dtH + vol * sqrt(dtH) * rng.nextGaussian())
            if (allowNews && rng.chance(MarketRules.NEWS_CHANCE)) {
                val headline = rng.pick(Headline.entries)
                val size = rng.range(MarketRules.NEWS_MIN_SHOCK, MarketRules.NEWS_MAX_SHOCK)
                val change = if (headline.positive) size else -size
                price *= 1.0 + change
                fresh += NewsItem(id, headline, change, clock)
            }
            price = price.coerceAtLeast(MarketRules.MIN_PRICE)
            s.copy(price = price, trend = trend, history = (s.history + price).takeLast(MarketRules.HISTORY_SIZE))
        }
        val news = if (fresh.isEmpty()) market.news else (fresh.reversed() + market.news).take(MarketRules.NEWS_KEPT)
        return market.copy(stocks = stocks, news = news) to fresh
    }

    /** Applies a one-off price shock (used by random events). */
    fun shock(market: MarketState, stockId: String, change: Double): MarketState {
        val s = market.stocks[stockId] ?: return market
        val price = (s.price * (1.0 + change)).coerceAtLeast(MarketRules.MIN_PRICE)
        return market.copy(stocks = market.stocks + (stockId to s.copy(price = price, history = (s.history + price).takeLast(MarketRules.HISTORY_SIZE))))
    }

    fun portfolioValue(market: MarketState): Double = market.stocks.values.sumOf { it.holdingValue }

    fun maxBuyable(state: GameState, stockId: String): Long {
        val price = state.market.stocks[stockId]?.price ?: return 0
        return floor(state.cash / price).coerceAtMost(MAX_SHARES_PER_TRADE).toLong()
    }

    fun buy(state: GameState, stockId: String, quantity: Long): GameState {
        val s = state.market.stocks[stockId] ?: return state
        if (quantity <= 0) return state
        val cost = s.price * quantity
        if (cost > state.cash) return state
        val shares = s.shares + quantity
        val avg = (s.avgCost * s.shares + cost) / shares
        val market = state.market.copy(stocks = state.market.stocks + (stockId to s.copy(shares = shares, avgCost = avg)))
        return state.copy(
            cash = state.cash - cost,
            market = market,
            stats = state.stats.copy(stockTrades = state.stats.stockTrades + 1),
        )
    }

    fun sell(state: GameState, stockId: String, quantity: Long): GameState {
        val s = state.market.stocks[stockId] ?: return state
        val qty = min(quantity.toDouble(), s.shares)
        if (qty <= 0.0) return state
        val proceeds = s.price * qty
        val profit = (s.price - s.avgCost) * qty
        val remaining = s.shares - qty
        val updated = if (remaining <= 0.0) s.copy(shares = 0.0, avgCost = 0.0) else s.copy(shares = remaining)
        var next = state.copy(
            cash = state.cash + proceeds,
            market = state.market.copy(stocks = state.market.stocks + (stockId to updated)),
            stats = state.stats.copy(
                stockTrades = state.stats.stockTrades + 1,
                profitableSells = state.stats.profitableSells + if (profit > 0) 1 else 0,
            ),
        )
        // Realised gains count as earnings (the proceeds themselves were already cash).
        if (profit > 0) {
            next = next.copy(
                runEarnings = next.runEarnings + profit,
                stats = next.stats.copy(lifetimeEarnings = next.stats.lifetimeEarnings + profit),
            )
        }
        return next
    }

    /** Liquidates nothing: clears holdings and keeps prices (used by prestige). */
    fun clearHoldings(market: MarketState): MarketState =
        market.copy(stocks = market.stocks.mapValues { it.value.copy(shares = 0.0, avgCost = 0.0) })

    private const val MAX_STEPS_PER_ADVANCE = 50
    private const val MAX_SHARES_PER_TRADE = 1e15
}

/** Real estate rules: buy, renovate, collect rent, sell at an appreciated value. */
object PropertyEngine {

    fun rentPerSecond(def: PropertyDef, p: PropertyState): Double =
        def.rentPerMinute * (1.0 + Properties.RENT_PER_LEVEL * p.level) / 60.0

    fun totalRentPerSecond(state: GameState): Double = state.properties.entries.sumOf { (id, p) ->
        Properties.byId[id]?.let { rentPerSecond(it, p) } ?: 0.0
    }

    fun value(def: PropertyDef, p: PropertyState): Double {
        val growth = (1.0 + def.appreciationPerHour).pow(p.ownedSeconds / 3600.0).coerceAtMost(Properties.MAX_APPRECIATION)
        return def.price * growth + p.renovationSpent * Properties.RENOVATION_RESALE
    }

    fun totalValue(state: GameState): Double = state.properties.entries.sumOf { (id, p) ->
        Properties.byId[id]?.let { value(it, p) } ?: 0.0
    }

    fun renovationCost(def: PropertyDef, p: PropertyState): Double? =
        if (p.level >= Properties.MAX_LEVEL) null else def.price * Properties.RENOVATION_COST_FACTOR * (p.level + 1)

    fun buy(state: GameState, id: String): GameState {
        val def = Properties.byId[id] ?: return state
        if (id in state.properties || state.cash < def.price) return state
        return state.copy(cash = state.cash - def.price, properties = state.properties + (id to PropertyState()))
    }

    fun renovate(state: GameState, id: String): GameState {
        val def = Properties.byId[id] ?: return state
        val p = state.properties[id] ?: return state
        val cost = renovationCost(def, p) ?: return state
        if (state.cash < cost) return state
        return state.copy(
            cash = state.cash - cost,
            properties = state.properties + (id to p.copy(level = p.level + 1, renovationSpent = p.renovationSpent + cost)),
        )
    }

    fun sell(state: GameState, id: String): GameState {
        val def = Properties.byId[id] ?: return state
        val p = state.properties[id] ?: return state
        return state.copy(cash = state.cash + value(def, p), properties = state.properties - id)
    }

    /** Collects rent and ages properties by [seconds]. Returns new properties and rent earned. */
    fun advance(state: GameState, seconds: Double): Pair<Map<String, PropertyState>, Double> {
        if (state.properties.isEmpty()) return state.properties to 0.0
        val rent = totalRentPerSecond(state) * seconds
        val aged = state.properties.mapValues { it.value.copy(ownedSeconds = it.value.ownedSeconds + seconds) }
        return aged to rent
    }
}
