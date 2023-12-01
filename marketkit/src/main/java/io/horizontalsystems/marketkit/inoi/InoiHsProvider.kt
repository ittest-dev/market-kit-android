package io.horizontalsystems.marketkit.inoi

import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrenciesManager
import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrency
import io.reactivex.Single

fun <T> hsProviderSwitch(
    customCurrenciesManager : CustomCurrenciesManager,
    currencyCode: String,
    defaultAction: (String) -> Single<T>,
    customAction: (CustomCurrency, T) -> T
): Single<T> {
    val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)
    val actionCurrencyCode = customCurrency?.let { "USD" } ?: currencyCode
    return defaultAction(actionCurrencyCode).map { data ->
        customCurrency?.let { customAction(it, data) } ?: data
    }
}