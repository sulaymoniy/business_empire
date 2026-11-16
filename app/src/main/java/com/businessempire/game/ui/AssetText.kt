package com.businessempire.game.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Villa
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.businessempire.game.R
import com.businessempire.game.domain.content.LuxuryCategory

/** Labels and icons for luxury items and rank titles. */
object AssetText {
    @StringRes
    fun luxuryName(id: String): Int = when (id) {
        "aurora_roadster" -> R.string.lux_aurora_roadster
        "strata_gt" -> R.string.lux_strata_gt
        "halberd_hypercar" -> R.string.lux_halberd_hypercar
        "meridian_classic" -> R.string.lux_meridian_classic
        "lakeside_cabin" -> R.string.lux_lakeside_cabin
        "hilltop_mansion" -> R.string.lux_hilltop_mansion
        "chateau_belmont" -> R.string.lux_chateau_belmont
        "sky_garden_estate" -> R.string.lux_sky_garden_estate
        "day_cruiser" -> R.string.lux_day_cruiser
        "coral_mist_yacht" -> R.string.lux_coral_mist_yacht
        "leviathan_superyacht" -> R.string.lux_leviathan_superyacht
        "swift_light_jet" -> R.string.lux_swift_light_jet
        "stratos_long_range" -> R.string.lux_stratos_long_range
        "private_airliner" -> R.string.lux_private_airliner
        "palm_cay_island" -> R.string.lux_palm_cay_island
        "coral_atoll" -> R.string.lux_coral_atoll
        "abstract_in_teal" -> R.string.lux_abstract_in_teal
        "marble_muse" -> R.string.lux_marble_muse
        "lost_harbor_masterpiece" -> R.string.lux_lost_harbor_masterpiece
        "ancient_star_map" -> R.string.lux_ancient_star_map
        "racehorse_stable" -> R.string.lux_racehorse_stable
        "lunar_retreat" -> R.string.lux_lunar_retreat
        else -> R.string.unknown
    }

    @StringRes
    fun categoryName(c: LuxuryCategory): Int = when (c) {
        LuxuryCategory.CAR -> R.string.lux_cat_car
        LuxuryCategory.HOME -> R.string.lux_cat_home
        LuxuryCategory.YACHT -> R.string.lux_cat_yacht
        LuxuryCategory.JET -> R.string.lux_cat_jet
        LuxuryCategory.ISLAND -> R.string.lux_cat_island
        LuxuryCategory.ART -> R.string.lux_cat_art
        LuxuryCategory.EXTRA -> R.string.lux_cat_extra
    }

    fun categoryIcon(c: LuxuryCategory): ImageVector = when (c) {
        LuxuryCategory.CAR -> Icons.Outlined.DirectionsCar
        LuxuryCategory.HOME -> Icons.Outlined.Villa
        LuxuryCategory.YACHT -> Icons.Outlined.DirectionsBoat
        LuxuryCategory.JET -> Icons.Outlined.FlightTakeoff
        LuxuryCategory.ISLAND -> Icons.Outlined.BeachAccess
        LuxuryCategory.ART -> Icons.Outlined.Palette
        LuxuryCategory.EXTRA -> Icons.Outlined.WorkspacePremium
    }

    @StringRes
    fun rankName(id: String): Int = when (id) {
        "street_vendor" -> R.string.rank_street_vendor
        "shop_owner" -> R.string.rank_shop_owner
        "local_entrepreneur" -> R.string.rank_local_entrepreneur
        "business_owner" -> R.string.rank_business_owner
        "executive" -> R.string.rank_executive
        "industrialist" -> R.string.rank_industrialist
        "magnate" -> R.string.rank_magnate
        "tycoon" -> R.string.rank_tycoon
        "mogul" -> R.string.rank_mogul
        else -> R.string.unknown
    }
}
