package io.horizontalsystems.marketkit.inoi.customcurrency

import io.horizontalsystems.marketkit.storage.MarketDatabase

class CustomCurrencyStorage(marketDatabase: MarketDatabase) {

    private val customCurrencyDao = marketDatabase.customCurrencyDao()

    fun customCurrency(currencyCode: String): CustomCurrency? {
        return customCurrencyDao.getCustomCurrency(currencyCode)?.toData()
    }

    fun save(customCurrencies: List<CustomCurrency>) {
        customCurrencyDao.insert(customCurrencies.map{ it.toEntity() })
    }

}