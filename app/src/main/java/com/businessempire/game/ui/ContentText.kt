package com.businessempire.game.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalCarWash
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Store
import androidx.compose.ui.graphics.vector.ImageVector
import com.businessempire.game.R

/**
 * Maps content ids (domain layer, plain strings) to string resources and icons (UI layer).
 * Keeping the domain free of R.* keeps it pure Kotlin and unit-testable.
 */
object ContentText {
    @StringRes
    fun businessName(id: String): Int = when (id) {
        "lemonade_stand" -> R.string.biz_lemonade_stand
        "food_truck" -> R.string.biz_food_truck
        "coffee_shop" -> R.string.biz_coffee_shop
        "car_wash" -> R.string.biz_car_wash
        "bakery_chain" -> R.string.biz_bakery_chain
        "fitness_club" -> R.string.biz_fitness_club
        "tech_startup" -> R.string.biz_tech_startup
        "boutique_hotel" -> R.string.biz_boutique_hotel
        "shipping_company" -> R.string.biz_shipping_company
        "regional_airline" -> R.string.biz_regional_airline
        "energy_plant" -> R.string.biz_energy_plant
        "orbital_launch" -> R.string.biz_orbital_launch
        else -> R.string.unknown
    }

    fun businessIcon(id: String): ImageVector = when (id) {
        "lemonade_stand" -> Icons.Outlined.LocalDrink
        "food_truck" -> Icons.Outlined.Fastfood
        "coffee_shop" -> Icons.Outlined.LocalCafe
        "car_wash" -> Icons.Outlined.LocalCarWash
        "bakery_chain" -> Icons.Outlined.BakeryDining
        "fitness_club" -> Icons.Outlined.FitnessCenter
        "tech_startup" -> Icons.Outlined.Computer
        "boutique_hotel" -> Icons.Outlined.Hotel
        "shipping_company" -> Icons.Outlined.DirectionsBoat
        "regional_airline" -> Icons.Outlined.Flight
        "energy_plant" -> Icons.Outlined.Bolt
        "orbital_launch" -> Icons.Outlined.RocketLaunch
        else -> Icons.Outlined.Store
    }
}
