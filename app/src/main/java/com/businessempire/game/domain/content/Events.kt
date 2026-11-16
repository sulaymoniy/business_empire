package com.businessempire.game.domain.content

import kotlinx.serialization.Serializable

/** Random choice events. Amounts are set when the event fires (see EventEngine). */
@Serializable
enum class EventType {
    /** A rival offers cash for some of your units of one business. */
    RIVAL_OFFER,
    /** Pay to promote a viral post: big short boost. */
    VIRAL_POST,
    /** Pay to avoid a supply shortage that halves one business. */
    SUPPLY_SHORTAGE,
    /** Settle a tax audit now, or gamble on a bigger fine. */
    TAX_AUDIT,
    /** Invest some cash on a stock tip that may or may not pay off. */
    MARKET_TIP,
    /** Sponsor a charity gala for a reputation boost. */
    CHARITY_GALA,
    /** Pay staff a bonus, or one business stops for a while. */
    STAFF_STRIKE,
    /** Take an investor's cash now in exchange for a temporary profit share. */
    INVESTOR_PITCH,
}

/** What happened after the player chose. Drives the result message. */
@Serializable
enum class EventOutcome {
    RIVAL_SOLD, RIVAL_MORALE, VIRAL_BOOST, SHORTAGE_AVOIDED, SHORTAGE_HIT, AUDIT_SETTLED,
    AUDIT_FINED, AUDIT_CLEARED, TIP_WIN, TIP_LOSS, GALA_BOOST, STRIKE_SETTLED, STRIKE_HIT,
    PITCH_TAKEN, DECLINED,
}

object EventRules {
    /** Seconds of active play between events. */
    const val MIN_COOLDOWN = 180.0
    const val MAX_COOLDOWN = 360.0
    /** Don't start events until the player has been playing this long. */
    const val FIRST_EVENT_AFTER = 300.0

    const val RIVAL_OFFER_SECONDS = 300.0
    const val RIVAL_UNITS_SOLD = 0.10
    const val RIVAL_MIN_OWNED = 10
    const val RIVAL_MORALE_FACTOR = 2.0
    const val RIVAL_MORALE_SECONDS = 180.0

    const val VIRAL_COST_SECONDS = 30.0
    const val VIRAL_FACTOR = 3.0
    const val VIRAL_DURATION = 120.0

    const val SHORTAGE_COST_SECONDS = 60.0
    const val SHORTAGE_FACTOR = 0.5
    const val SHORTAGE_DURATION = 180.0

    const val AUDIT_SETTLE_SHARE = 0.05
    const val AUDIT_FINE_SHARE = 0.15
    const val AUDIT_FINE_CHANCE = 0.5

    const val TIP_INVEST_SHARE = 0.10
    const val TIP_WIN_CHANCE = 0.6
    const val TIP_WIN_CHANGE = 0.20
    const val TIP_LOSS_CHANGE = -0.15

    const val GALA_COST_SHARE = 0.02
    const val GALA_FACTOR = 1.5
    const val GALA_DURATION = 300.0

    const val STRIKE_COST_SECONDS = 45.0
    const val STRIKE_FACTOR = 0.0
    const val STRIKE_DURATION = 60.0

    const val PITCH_CASH_SECONDS = 180.0
    const val PITCH_FACTOR = 0.8
    const val PITCH_DURATION = 300.0

    const val MIN_AMOUNT = 10.0
}

object PrestigeRules {
    /** Net worth needed before the empire can be sold. */
    const val UNLOCK_NET_WORTH = 1e11
    /** Total Legacy Points = floor(sqrt(lifetime earnings / this)). */
    const val POINT_DIVISOR = 1e8
    /** Revenue bonus per Legacy Point. */
    const val BONUS_PER_POINT = 0.02
}

object DailyRules {
    const val MAX_STREAK_BONUS_DAYS = 7
    const val BASE_INCOME_SECONDS = 300.0
    const val STREAK_STEP = 0.25
    const val MIN_REWARD_PER_DAY = 100.0
}

/** First-run coach marks, in order. */
enum class TutorialStep { BUY_STAND, TAP_STAND, BUY_TEN, HIRE_MANAGER, VISIT_INVEST, DONE }
