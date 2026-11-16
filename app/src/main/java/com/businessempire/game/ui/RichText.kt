package com.businessempire.game.ui

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.businessempire.game.R
import com.businessempire.game.domain.RichPerson

/** Labels for the Rich List: wealth milestones, comparisons and fictional tycoon descriptions. */
object RichText {
    @StringRes
    fun milestoneName(id: String): Int = when (id) {
        "top_half" -> R.string.wm_top_half
        "top_10_percent" -> R.string.wm_top_10_percent
        "average_household" -> R.string.wm_average_household
        "millionaire" -> R.string.wm_millionaire
        "top_1_percent" -> R.string.wm_top_1_percent
        "decamillionaire" -> R.string.wm_decamillionaire
        "ultra_high_net_worth" -> R.string.wm_ultra_high_net_worth
        "centimillionaire" -> R.string.wm_centimillionaire
        "billionaire" -> R.string.wm_billionaire
        "top_100" -> R.string.wm_top_100
        "top_10" -> R.string.wm_top_10
        "richest_person" -> R.string.wm_richest_person
        else -> R.string.unknown
    }

    @PluralsRes
    fun comparison(id: String): Int = when (id) {
        "new_car" -> R.plurals.cmp_new_car
        "average_house" -> R.plurals.cmp_average_house
        "private_jet" -> R.plurals.cmp_private_jet
        "superyacht" -> R.plurals.cmp_superyacht
        "sports_team" -> R.plurals.cmp_sports_team
        else -> R.plurals.cmp_country_budget
    }

    @StringRes
    private fun tycoonDescription(key: String?): Int = when (key) {
        "bakery" -> R.string.tycoon_bakery
        "food_trucks" -> R.string.tycoon_food_trucks
        "coffee" -> R.string.tycoon_coffee
        "car_washes" -> R.string.tycoon_car_washes
        "gyms" -> R.string.tycoon_gyms
        "hotels" -> R.string.tycoon_hotels
        "apps" -> R.string.tycoon_apps
        "shipping" -> R.string.tycoon_shipping
        "property" -> R.string.tycoon_property
        "airline" -> R.string.tycoon_airline
        "energy" -> R.string.tycoon_energy
        "chips" -> R.string.tycoon_chips
        "fund" -> R.string.tycoon_fund
        "robots" -> R.string.tycoon_robots
        "space" -> R.string.tycoon_space
        else -> R.string.tycoon_everything
    }

    fun description(res: Resources, person: RichPerson): String =
        person.description ?: res.getString(tycoonDescription(person.descriptionKey))
}
