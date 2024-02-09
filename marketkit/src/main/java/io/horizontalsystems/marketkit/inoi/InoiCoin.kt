package io.horizontalsystems.marketkit.inoi

import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.CoinPrice
import io.horizontalsystems.marketkit.models.TokenEntity
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
    reference = "0x492c1BDABEFefa6Ef5fD3D670dC7d68a96C5d33D"
)

val coinsList = listOf(inoiCoin, inoiCoinTest)
val tokensList = listOf(inoiToken, inoiTokenTest)

fun List<CoinPrice>.addInoi(currencyCode: String) = this.plus(
    listOf(
        CoinPrice("inoi", currencyCode, BigDecimal(1), null, 0),
        CoinPrice("inoiTest", currencyCode, BigDecimal(1), null, 0)
    )
)