package com.businessempire.game.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import com.businessempire.game.R
import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.NumberStyle
import com.businessempire.game.domain.PendingEvent
import com.businessempire.game.domain.content.AchievementDef
import com.businessempire.game.domain.content.AchievementType
import com.businessempire.game.domain.content.EventOutcome
import com.businessempire.game.domain.content.EventType
import com.businessempire.game.domain.content.TutorialStep

/**
 * Text for achievements, events and the tutorial. Takes Resources (not @Composable) so the same
 * strings work in snackbars and dialogs.
 */
object MetaText {
    private fun money(res: Resources, v: Double, style: NumberStyle) =
        res.getString(R.string.money, NumberFormatter.format(v, style))

    fun achievementDescription(res: Resources, def: AchievementDef, style: NumberStyle): String {
        val n = def.target.toInt()
        val r = res
        return when (def.type) {
            AchievementType.OWN_BUSINESS ->
                res.getString(R.string.ach_own, n, res.getString(ContentText.businessName(def.businessId!!)))
            AchievementType.LIFETIME_EARNINGS -> res.getString(R.string.ach_earn, money(res, def.target, style))
            AchievementType.NET_WORTH -> res.getString(R.string.ach_networth, money(res, def.target, style))
            AchievementType.TAPS -> r.getQuantityString(R.plurals.ach_taps, n, n)
            AchievementType.MANAGERS -> r.getQuantityString(R.plurals.ach_managers, n, n)
            AchievementType.PROPERTIES -> r.getQuantityString(R.plurals.ach_properties, n, n)
            AchievementType.LUXURY -> r.getQuantityString(R.plurals.ach_luxury, n, n)
            AchievementType.STOCK_TRADES -> r.getQuantityString(R.plurals.ach_trades, n, n)
            AchievementType.PROFITABLE_SELLS -> r.getQuantityString(R.plurals.ach_profit_sells, n, n)
            AchievementType.PRESTIGES -> r.getQuantityString(R.plurals.ach_prestige, n, n)
            AchievementType.DAILY_STREAK -> r.getQuantityString(R.plurals.ach_streak, n, n)
            AchievementType.LOANS_REPAID -> r.getQuantityString(R.plurals.ach_loans, n, n)
            AchievementType.EVENTS -> r.getQuantityString(R.plurals.ach_events, n, n)
        }
    }

    fun achievementReward(res: Resources, def: AchievementDef): String = when {
        def.rewardPercent > 0 -> res.getString(R.string.ach_reward_percent, NumberFormatter.short(def.rewardPercent))
        else -> res.getString(R.string.ach_reward_cash, NumberFormatter.short(def.rewardCashSeconds / 60.0))
    }

    @StringRes
    fun eventTitle(type: EventType): Int = when (type) {
        EventType.RIVAL_OFFER -> R.string.event_rival_title
        EventType.VIRAL_POST -> R.string.event_viral_title
        EventType.SUPPLY_SHORTAGE -> R.string.event_shortage_title
        EventType.TAX_AUDIT -> R.string.event_audit_title
        EventType.MARKET_TIP -> R.string.event_tip_title
        EventType.CHARITY_GALA -> R.string.event_gala_title
        EventType.STAFF_STRIKE -> R.string.event_strike_title
        EventType.INVESTOR_PITCH -> R.string.event_pitch_title
    }

    fun eventBody(res: Resources, event: PendingEvent, companyName: String, style: NumberStyle): String {
        val subject = when {
            event.businessId != null -> res.getString(ContentText.businessName(event.businessId))
            event.stockId != null -> res.getString(InvestText.stockName(event.stockId))
            else -> companyName
        }
        val body = when (event.type) {
            EventType.RIVAL_OFFER -> R.string.event_rival_body
            EventType.VIRAL_POST -> R.string.event_viral_body
            EventType.SUPPLY_SHORTAGE -> R.string.event_shortage_body
            EventType.TAX_AUDIT -> R.string.event_audit_body
            EventType.MARKET_TIP -> R.string.event_tip_body
            EventType.CHARITY_GALA -> R.string.event_gala_body
            EventType.STAFF_STRIKE -> R.string.event_strike_body
            EventType.INVESTOR_PITCH -> R.string.event_pitch_body
        }
        return res.getString(body, subject, money(res, event.amount, style))
    }

    @StringRes
    fun eventAccept(type: EventType): Int = when (type) {
        EventType.RIVAL_OFFER -> R.string.event_rival_accept
        EventType.VIRAL_POST -> R.string.event_viral_accept
        EventType.SUPPLY_SHORTAGE -> R.string.event_pay
        EventType.TAX_AUDIT -> R.string.event_audit_accept
        EventType.MARKET_TIP -> R.string.event_tip_accept
        EventType.CHARITY_GALA -> R.string.event_gala_accept
        EventType.STAFF_STRIKE -> R.string.event_strike_accept
        EventType.INVESTOR_PITCH -> R.string.event_accept
    }

    @StringRes
    fun eventDecline(type: EventType): Int = when (type) {
        EventType.RIVAL_OFFER -> R.string.event_refuse
        EventType.SUPPLY_SHORTAGE -> R.string.event_shortage_decline
        EventType.TAX_AUDIT -> R.string.event_audit_decline
        EventType.MARKET_TIP -> R.string.event_ignore
        EventType.STAFF_STRIKE -> R.string.event_refuse
        else -> R.string.event_decline
    }

    fun outcome(res: Resources, outcome: EventOutcome, amount: Double, style: NumberStyle): String {
        val m = money(res, amount, style)
        return when (outcome) {
            EventOutcome.RIVAL_SOLD -> res.getString(R.string.outcome_rival_sold, m)
            EventOutcome.RIVAL_MORALE -> res.getString(R.string.outcome_rival_morale)
            EventOutcome.VIRAL_BOOST -> res.getString(R.string.outcome_viral_boost)
            EventOutcome.SHORTAGE_AVOIDED -> res.getString(R.string.outcome_shortage_avoided)
            EventOutcome.SHORTAGE_HIT -> res.getString(R.string.outcome_shortage_hit)
            EventOutcome.AUDIT_SETTLED -> res.getString(R.string.outcome_audit_settled, m)
            EventOutcome.AUDIT_FINED -> res.getString(R.string.outcome_audit_fined, m)
            EventOutcome.AUDIT_CLEARED -> res.getString(R.string.outcome_audit_cleared)
            EventOutcome.TIP_WIN -> res.getString(R.string.outcome_tip_win)
            EventOutcome.TIP_LOSS -> res.getString(R.string.outcome_tip_loss)
            EventOutcome.GALA_BOOST -> res.getString(R.string.outcome_gala_boost)
            EventOutcome.STRIKE_SETTLED -> res.getString(R.string.outcome_strike_settled)
            EventOutcome.STRIKE_HIT -> res.getString(R.string.outcome_strike_hit)
            EventOutcome.PITCH_TAKEN -> res.getString(R.string.outcome_pitch_taken, m)
            EventOutcome.DECLINED -> res.getString(R.string.outcome_declined)
        }
    }

    @StringRes
    fun tutorial(step: TutorialStep): Int = when (step) {
        TutorialStep.BUY_STAND -> R.string.tutorial_buy_stand
        TutorialStep.TAP_STAND -> R.string.tutorial_tap_stand
        TutorialStep.BUY_TEN -> R.string.tutorial_buy_ten
        TutorialStep.HIRE_MANAGER -> R.string.tutorial_hire_manager
        TutorialStep.VISIT_INVEST -> R.string.tutorial_visit_invest
        TutorialStep.DONE -> R.string.unknown
    }
}
