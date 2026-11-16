package com.businessempire.game.domain

import com.businessempire.game.domain.content.BankRules
import com.businessempire.game.domain.content.Luxury
import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

@Serializable
data class BankState(
    val savings: Double = 0.0,
    val loan: Double = 0.0,
    /** Seconds since the last repayment while a loan is open. */
    val loanAgeSeconds: Double = 0.0,
    val interestEarned: Double = 0.0,
    val interestPaid: Double = 0.0,
) {
    val overdue: Boolean get() = loan > 0.0 && loanAgeSeconds >= BankRules.OVERDUE_SECONDS
}

/** Savings and loans. */
object BankEngine {

    /** Applies [seconds] of interest. Savings interest counts as earnings. */
    fun advance(state: GameState, seconds: Double): GameState {
        val bank = state.bank
        if (bank.savings <= 0.0 && bank.loan <= 0.0) return state
        val minutes = seconds / 60.0
        val savings = bank.savings * exp(BankRules.SAVINGS_RATE_PER_MINUTE * minutes)
        val earned = savings - bank.savings
        val loan = bank.loan * exp(BankRules.LOAN_RATE_PER_MINUTE * minutes)
        val paid = loan - bank.loan
        val next = state.copy(
            bank = bank.copy(
                savings = savings,
                loan = loan,
                loanAgeSeconds = if (loan > 0.0) bank.loanAgeSeconds + seconds else 0.0,
                interestEarned = bank.interestEarned + earned,
                interestPaid = bank.interestPaid + paid,
            ),
        )
        // Interest lands in savings, not cash, so only the earnings counters move.
        return next.copy(
            runEarnings = next.runEarnings + earned,
            stats = next.stats.copy(lifetimeEarnings = next.stats.lifetimeEarnings + earned),
        )
    }

    fun deposit(state: GameState, amount: Double): GameState {
        val a = min(amount, state.cash)
        if (a <= 0.0) return state
        return state.copy(cash = state.cash - a, bank = state.bank.copy(savings = state.bank.savings + a))
    }

    fun withdraw(state: GameState, amount: Double): GameState {
        val a = min(amount, state.bank.savings)
        if (a <= 0.0) return state
        return state.copy(cash = state.cash + a, bank = state.bank.copy(savings = state.bank.savings - a))
    }

    /** How much more the player may borrow right now. */
    fun availableCredit(state: GameState): Double {
        if (state.bank.overdue) return 0.0
        val netWorth = Economy(state).netWorth
        val limit = max(BankRules.MIN_CREDIT, BankRules.MAX_LOAN_FRACTION * netWorth)
        return max(0.0, limit - state.bank.loan)
    }

    fun borrow(state: GameState, amount: Double): GameState {
        val a = min(amount, availableCredit(state))
        if (a <= 0.0) return state
        return state.copy(cash = state.cash + a, bank = state.bank.copy(loan = state.bank.loan + a))
    }

    /** Any repayment resets the overdue timer; paying it all off closes the loan. */
    fun repay(state: GameState, amount: Double): GameState {
        val a = min(amount, min(state.cash, state.bank.loan))
        if (a <= 0.0) return state
        val remaining = state.bank.loan - a
        val closed = remaining <= 0.01
        return state.copy(
            cash = state.cash - a,
            bank = state.bank.copy(loan = if (closed) 0.0 else remaining, loanAgeSeconds = 0.0),
            stats = if (closed) state.stats.copy(loansRepaid = state.stats.loansRepaid + 1) else state.stats,
        )
    }
}

/** Luxury purchases. */
object LuxuryEngine {
    fun buy(state: GameState, id: String): GameState {
        val def = Luxury.byId[id] ?: return state
        if (id in state.luxury || state.cash < def.price) return state
        return state.copy(cash = state.cash - def.price, luxury = state.luxury + id)
    }

    /** +1% per bonus point, added together. */
    fun bonusPercent(state: GameState): Double = state.luxury.sumOf { Luxury.byId[it]?.bonusPercent ?: 0.0 }

    fun value(state: GameState): Double = state.luxury.sumOf { Luxury.byId[it]?.price ?: 0.0 }
}
