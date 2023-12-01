package io.horizontalsystems.marketkit.inoi

import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrenciesManager
import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrency
import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrencyProvider
import io.horizontalsystems.marketkit.inoi.customcurrency.CustomCurrencyStorage
import io.horizontalsystems.marketkit.managers.CoinPriceManager
import io.horizontalsystems.marketkit.managers.CoinPriceSyncManager
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.models.CoinPriceResponse
import io.horizontalsystems.marketkit.models.TokenEntity
import io.reactivex.Single
import java.math.BigDecimal

val inoiCoin = Coin(
    uid = "inoi",
    name = "INOI Token",
    code = "INOI",
    marketCapRank = -1,
    coinGeckoId = null
)
val inoiToken = TokenEntity(
    coinUid = "inoi",
    blockchainUid = "binance-smart-chain",
    type = "eip20",
    decimals = 18,
    reference = "0x22FcC36558F0e02aF135045EDB0a43f64511DA59"
)

val inoiCoinTest = Coin(
    uid = "inoiTest",
    name = "INOI Token",
    code = "INOI",
    marketCapRank = -1,
    coinGeckoId = null
)
val inoiTokenTest = TokenEntity(
    coinUid = "inoiTest",
    blockchainUid = "binance-smart-chain",
    type = "eip20",
    decimals = 18,
    reference = "0x0312b9934b10b0c8d05a641fe5381d4303e4e2ee"
)

val coinsList = listOf(inoiCoin, inoiCoinTest)
val tokensList = listOf(inoiToken, inoiTokenTest)

fun List<CoinPrice>.addInoi(currencyCode: String) = this.plus(
    listOf(
        CoinPrice("inoi", currencyCode, BigDecimal(1), null, 0),
        CoinPrice("inoiTest", currencyCode, BigDecimal(1), null, 0)
    )
)

fun <T> withCustomCurrency(
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