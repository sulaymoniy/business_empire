package com.businessempire.game

import com.businessempire.game.domain.BankEngine
import com.businessempire.game.domain.BankState
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.LuxuryEngine
import com.businessempire.game.domain.content.BankRules
import com.businessempire.game.domain.content.Luxury
import com.businessempire.game.domain.content.Ranks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp

class BankAndAssetsTest {

    @Test fun loanInterestCompoundsPerMinute() {
        val s = GameState(cash = 0.0, bank = BankState(loan = 1_000.0))
        val after = BankEngine.advance(s, 10 * 60.0)
        val expected = 1_000.0 * exp(BankRules.LOAN_RATE_PER_MINUTE * 10)
        assertEquals(expected, after.bank.loan, 1e-9)
        assertEquals(expected - 1_000.0, after.bank.interestPaid, 1e-9)
        assertEquals(600.0, after.bank.loanAgeSeconds, 0.0)
    }

    @Test fun loanInterestAlsoAccruesOffline() {
        val s = GameState(cash = 0.0, bank = BankState(loan = 1_000.0))
        val (after, _) = GameEngine.applyOffline(s, 3600.0)
        assertEquals(1_000.0 * exp(BankRules.LOAN_RATE_PER_MINUTE * 60), after.bank.loan, 1e-6)
    }

    @Test fun savingsInterestCountsAsEarnings() {
        val s = GameState(cash = 0.0, bank = BankState(savings = 10_000.0))
        val after = BankEngine.advance(s, 60.0)
        val gained = after.bank.savings - 10_000.0
        assertTrue(gained > 0)
        assertEquals(gained, after.stats.lifetimeEarnings, 1e-9)
        assertEquals(0.0, after.cash, 0.0)
    }

    @Test fun borrowingIsLimitedByNetWorth() {
        val s = GameState(cash = 100_000.0)
        val credit = BankEngine.availableCredit(s)
        assertEquals(BankRules.MAX_LOAN_FRACTION * 100_000.0, credit, 1e-6)
        val borrowed = BankEngine.borrow(s, 1e12)
        assertEquals(credit, borrowed.bank.loan, 1e-6)
        assertEquals(100_000.0 + credit, borrowed.cash, 1e-6)
        // Borrowing does not change net worth.
        assertEquals(Economy(s).netWorth, Economy(borrowed).netWorth, 1e-6)
    }

    @Test fun overdueLoanBlocksBorrowingUntilRepayment() {
        var s = GameState(cash = 10_000.0, bank = BankState(loan = 1_000.0, loanAgeSeconds = BankRules.OVERDUE_SECONDS + 1))
        assertTrue(s.bank.overdue)
        assertEquals(0.0, BankEngine.availableCredit(s), 0.0)
        assertEquals(s, BankEngine.borrow(s, 500.0))
        s = BankEngine.repay(s, 100.0)
        assertTrue(!s.bank.overdue)
        assertTrue(BankEngine.availableCredit(s) > 0)
    }

    @Test fun fullRepaymentClosesTheLoan() {
        var s = GameState(cash = 5_000.0, bank = BankState(loan = 1_000.0))
        s = BankEngine.repay(s, 99_999.0)
        assertEquals(0.0, s.bank.loan, 0.0)
        assertEquals(4_000.0, s.cash, 1e-9)
        assertEquals(1, s.stats.loansRepaid)
    }

    @Test fun depositAndWithdrawMoveCash() {
        var s = GameState(cash = 1_000.0)
        s = BankEngine.deposit(s, 600.0)
        assertEquals(400.0, s.cash, 0.0)
        assertEquals(600.0, s.bank.savings, 0.0)
        s = BankEngine.withdraw(s, 1e9)
        assertEquals(1_000.0, s.cash, 0.0)
        assertEquals(0.0, s.bank.savings, 0.0)
    }

    @Test fun luxuryBoostsRevenueAndNetWorth() {
        val item = Luxury.all.first()
        val s = GameState(cash = item.price, businesses = mapOf("lemonade_stand" to com.businessempire.game.domain.BusinessState(owned = 1)))
        val before = Economy(s)
        val after = LuxuryEngine.buy(s, item.id)
        assertTrue(item.id in after.luxury)
        assertEquals(0.0, after.cash, 0.0)
        assertEquals(before.netWorth, Economy(after).netWorth, 1e-6)
        assertEquals(1.0 + item.bonusPercent / 100.0, Economy(after).luxuryMultiplier, 1e-9)
        assertEquals(
            before.revenuePerCycle("lemonade_stand") * (1.0 + item.bonusPercent / 100.0),
            Economy(after).revenuePerCycle("lemonade_stand"),
            1e-9,
        )
    }

    @Test fun ranksFollowNetWorth() {
        assertEquals("street_vendor", Ranks.forNetWorth(0.0).id)
        assertEquals("shop_owner", Ranks.forNetWorth(1_000.0).id)
        assertEquals("mogul", Ranks.forNetWorth(1e30).id)
        assertEquals(null, Ranks.next(1e30))
    }
}
