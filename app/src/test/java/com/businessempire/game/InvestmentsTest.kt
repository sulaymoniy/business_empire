package com.businessempire.game

import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.MarketEngine
import com.businessempire.game.domain.PropertyEngine
import com.businessempire.game.domain.Rng
import com.businessempire.game.domain.content.MarketRules
import com.businessempire.game.domain.content.Properties
import com.businessempire.game.domain.content.Stocks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestmentsTest {
    private val stockId = Stocks.all.first().id

    @Test fun marketIsDeterministicForASeed() {
        val a = GameEngine.newGame(seed = 7L, nowMs = 0L)
        val b = GameEngine.newGame(seed = 7L, nowMs = 0L)
        var sa = a
        var sb = b
        repeat(500) {
            sa = GameEngine.tick(sa, 0.5)
            sb = GameEngine.tick(sb, 0.5)
        }
        assertEquals(sa.market, sb.market)
    }

    @Test fun pricesStayPositiveAndHistoryIsBounded() {
        var m = MarketEngine.initialMarket(Rng(99L))
        val rng = Rng(123L)
        repeat(5_000) { m = MarketEngine.step(m, rng, clock = it.toDouble(), allowNews = true).first }
        m.stocks.values.forEach {
            assertTrue(it.price >= MarketRules.MIN_PRICE)
            assertEquals(MarketRules.HISTORY_SIZE, it.history.size)
        }
        assertTrue(m.news.size <= MarketRules.NEWS_KEPT)
        assertTrue("some news should happen over 5000 updates", m.news.isNotEmpty())
    }

    @Test fun buyingTracksAverageCost() {
        var s = GameEngine.newGame(seed = 1L, nowMs = 0L).copy(cash = 1e6)
        s = setPrice(s, 100.0)
        s = MarketEngine.buy(s, stockId, 10)
        s = setPrice(s, 200.0)
        s = MarketEngine.buy(s, stockId, 10)
        val st = s.market.stocks.getValue(stockId)
        assertEquals(20.0, st.shares, 0.0)
        assertEquals(150.0, st.avgCost, 1e-9)
        assertEquals(1e6 - 1000.0 - 2000.0, s.cash, 1e-6)
        assertEquals(1000.0, st.profit, 1e-9)
    }

    @Test fun sellingRealisesProfitAsEarnings() {
        var s = GameEngine.newGame(seed = 1L, nowMs = 0L).copy(cash = 1_000.0)
        s = setPrice(s, 10.0)
        s = MarketEngine.buy(s, stockId, 50)
        s = setPrice(s, 30.0)
        val earningsBefore = s.stats.lifetimeEarnings
        s = MarketEngine.sell(s, stockId, 50)
        assertEquals(1_000.0 - 500.0 + 1_500.0, s.cash, 1e-9)
        assertEquals(earningsBefore + 1_000.0, s.stats.lifetimeEarnings, 1e-9)
        assertEquals(1, s.stats.profitableSells)
        assertEquals(0.0, s.market.stocks.getValue(stockId).shares, 0.0)
    }

    @Test fun cannotOverspendOrOversell() {
        var s = GameEngine.newGame(seed = 1L, nowMs = 0L).copy(cash = 100.0)
        s = setPrice(s, 60.0)
        assertEquals(s, MarketEngine.buy(s, stockId, 2))
        assertEquals(1L, MarketEngine.maxBuyable(s, stockId))
        s = MarketEngine.buy(s, stockId, 1)
        s = MarketEngine.sell(s, stockId, 5)
        assertEquals(0.0, s.market.stocks.getValue(stockId).shares, 0.0)
        assertEquals(100.0, s.cash, 1e-9)
    }

    @Test fun propertyRentRenovationAndSale() {
        val def = Properties.all.first()
        var s = GameState(cash = def.price * 10)
        s = PropertyEngine.buy(s, def.id)
        assertEquals(def.price * 9, s.cash, 1e-6)
        val rent0 = PropertyEngine.totalRentPerSecond(s)
        assertEquals(def.rentPerMinute / 60.0, rent0, 1e-9)

        s = PropertyEngine.renovate(s, def.id)
        assertEquals(1, s.properties.getValue(def.id).level)
        assertEquals(rent0 * (1 + Properties.RENT_PER_LEVEL), PropertyEngine.totalRentPerSecond(s), 1e-9)

        val cashBefore = s.cash
        s = GameEngine.tick(s, 60.0)
        assertTrue(s.cash > cashBefore)

        val value = PropertyEngine.value(def, s.properties.getValue(def.id))
        assertTrue(value > def.price)
        val cashBeforeSale = s.cash
        s = PropertyEngine.sell(s, def.id)
        assertEquals(cashBeforeSale + value, s.cash, 1e-6)
        assertTrue(def.id !in s.properties)
    }

    @Test fun appreciationIsCapped() {
        val def = Properties.all.first()
        val old = com.businessempire.game.domain.PropertyState(ownedSeconds = 1e9)
        assertEquals(def.price * Properties.MAX_APPRECIATION, PropertyEngine.value(def, old), 1e-6)
    }

    private fun setPrice(s: GameState, price: Double): GameState {
        val st = s.market.stocks.getValue(stockId)
        return s.copy(market = s.market.copy(stocks = s.market.stocks + (stockId to st.copy(price = price))))
    }
}
