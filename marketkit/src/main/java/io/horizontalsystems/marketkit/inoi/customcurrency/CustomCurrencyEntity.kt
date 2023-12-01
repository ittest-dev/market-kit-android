package io.horizontalsystems.marketkit.inoi.customcurrency

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "CustomCurrencyEntity")
data class CustomCurrencyEntity(
    @PrimaryKey
    val currencyCode: String,
    val telephoneCode: String?,
    val currencyUnitsPerDollar: BigDecimal?,
    val symbol: String?,
    val icon: String?
) {
    fun toData() = CustomCurrency(
        currencyCode = currencyCode,
        telephoneCode = telephoneCode,
        currencyUnitsPerDollar = currencyUnitsPerDollar,
        symbol = symbol,
        icon = icon,
    )
}