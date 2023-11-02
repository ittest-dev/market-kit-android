/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package io.horizontalsystems.marketkit.customcurrency

import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.providers.ChartCoinPriceResponse
import io.horizontalsystems.marketkit.providers.HistoricalCoinPriceResponse
import java.math.BigDecimal

data class CustomCurrency(
    val currencyCode: String,
    val telephoneCode: String?,
    val currencyUnitsPerDollar: BigDecimal?,
    val symbol: String?,
    val icon: String?
) {
    fun toEntity() = CustomCurrencyEntity(
        currencyCode = currencyCode,
        telephoneCode = telephoneCode,
        currencyUnitsPerDollar = currencyUnitsPerDollar,
        symbol = symbol,
        icon = icon,
    )
}

fun CoinPrice.convertValuesToCustomCurrency(customCurrency: CustomCurrency): CoinPrice = this.copy(
    currencyCode = customCurrency.currencyCode
        ?: throw IllegalStateException("currencyCode is null"),
    value = this.value.multiply(customCurrency.currencyUnitsPerDollar)
)

fun HistoricalCoinPriceResponse.convertValuesToCustomCurrency(customCurrency: CustomCurrency): HistoricalCoinPriceResponse =
    this.copy(
        price = this.price.multiply(customCurrency.currencyUnitsPerDollar)
    )

fun ChartCoinPriceResponse.convertValuesToCustomCurrency(customCurrency: CustomCurrency): ChartCoinPriceResponse =
    this.copy(
        price = this.price.multiply(customCurrency.currencyUnitsPerDollar),
        totalVolume = this.totalVolume?.multiply(customCurrency.currencyUnitsPerDollar)
    )
