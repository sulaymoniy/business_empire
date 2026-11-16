package com.businessempire.game.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Sailing
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import com.businessempire.game.R
import com.businessempire.game.domain.content.Headline
import com.businessempire.game.domain.content.Sector

/** Labels and icons for stocks and properties. */
object InvestText {
    @StringRes
    fun stockName(id: String): Int = when (id) {
        "nimbus_cloud" -> R.string.stock_nimbus_cloud
        "quantis_labs" -> R.string.stock_quantis_labs
        "solace_energy" -> R.string.stock_solace_energy
        "ridgeway_power" -> R.string.stock_ridgeway_power
        "verdant_health" -> R.string.stock_verdant_health
        "brightfen_bio" -> R.string.stock_brightfen_bio
        "ironbridge_capital" -> R.string.stock_ironbridge_capital
        "maple_finch" -> R.string.stock_maple_finch
        "pellucid_apparel" -> R.string.stock_pellucid_apparel
        "cobalt_rail" -> R.string.stock_cobalt_rail
        else -> R.string.unknown
    }

    @StringRes
    fun sectorName(sector: Sector): Int = when (sector) {
        Sector.TECH -> R.string.sector_tech
        Sector.ENERGY -> R.string.sector_energy
        Sector.HEALTH -> R.string.sector_health
        Sector.FINANCE -> R.string.sector_finance
        Sector.CONSUMER -> R.string.sector_consumer
        Sector.INDUSTRIAL -> R.string.sector_industrial
    }

    fun sectorIcon(sector: Sector): ImageVector = when (sector) {
        Sector.TECH -> Icons.Outlined.Memory
        Sector.ENERGY -> Icons.Outlined.ElectricBolt
        Sector.HEALTH -> Icons.Outlined.MedicalServices
        Sector.FINANCE -> Icons.Outlined.AccountBalance
        Sector.CONSUMER -> Icons.Outlined.ShoppingBag
        Sector.INDUSTRIAL -> Icons.Outlined.Factory
    }

    /** Headline template with one %1$s for the company name. */
    @StringRes
    fun headline(h: Headline): Int = when (h) {
        Headline.REGULATORS_PROBE -> R.string.news_regulators_probe
        Headline.MISSES_EARNINGS -> R.string.news_misses_earnings
        Headline.PRODUCT_RECALL -> R.string.news_product_recall
        Headline.EXECUTIVE_EXITS -> R.string.news_executive_exits
        Headline.BEATS_EARNINGS -> R.string.news_beats_earnings
        Headline.MAJOR_CONTRACT -> R.string.news_major_contract
        Headline.BREAKTHROUGH -> R.string.news_breakthrough
        Headline.ANALYST_UPGRADE -> R.string.news_analyst_upgrade
    }

    @StringRes
    fun propertyName(id: String): Int = when (id) {
        "studio_flat" -> R.string.prop_studio_flat
        "corner_shop" -> R.string.prop_corner_shop
        "suburban_house" -> R.string.prop_suburban_house
        "city_duplex" -> R.string.prop_city_duplex
        "downtown_loft" -> R.string.prop_downtown_loft
        "retail_strip" -> R.string.prop_retail_strip
        "apartment_block" -> R.string.prop_apartment_block
        "beachfront_villa" -> R.string.prop_beachfront_villa
        "business_park" -> R.string.prop_business_park
        "office_tower" -> R.string.prop_office_tower
        "harbor_marina" -> R.string.prop_harbor_marina
        "private_island_resort" -> R.string.prop_private_island_resort
        else -> R.string.unknown
    }

    fun propertyIcon(id: String): ImageVector = when (id) {
        "studio_flat" -> Icons.Outlined.MeetingRoom
        "corner_shop" -> Icons.Outlined.Storefront
        "suburban_house" -> Icons.Outlined.House
        "city_duplex" -> Icons.Outlined.Cottage
        "downtown_loft" -> Icons.Outlined.Apartment
        "retail_strip" -> Icons.Outlined.LocalMall
        "apartment_block" -> Icons.Outlined.Domain
        "beachfront_villa" -> Icons.Outlined.BeachAccess
        "business_park" -> Icons.Outlined.BusinessCenter
        "office_tower" -> Icons.Outlined.CorporateFare
        "harbor_marina" -> Icons.Outlined.Sailing
        "private_island_resort" -> Icons.Outlined.Landscape
        else -> Icons.Outlined.House
    }
}
