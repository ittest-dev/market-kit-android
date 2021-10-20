package io.horizontalsystems.marketkit

import android.content.Context
import io.horizontalsystems.marketkit.chart.ChartManager
import io.horizontalsystems.marketkit.chart.ChartSchedulerFactory
import io.horizontalsystems.marketkit.chart.ChartSyncManager
import io.horizontalsystems.marketkit.managers.*
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.*
import io.horizontalsystems.marketkit.storage.*
import io.horizontalsystems.marketkit.syncers.CoinCategorySyncer
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.horizontalsystems.marketkit.syncers.ExchangeSyncer
import io.reactivex.Observable
import io.reactivex.Single

class MarketKit(
    private val coinManager: CoinManager,
    private val coinCategoryManager: CoinCategoryManager,
    private val coinSyncer: CoinSyncer,
    private val coinCategorySyncer: CoinCategorySyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager,
    private val chartManager: ChartManager,
    private val exchangeSyncer: ExchangeSyncer,
    private val chartSyncManager: ChartSyncManager,
    private val globalMarketInfoManager: GlobalMarketInfoManager
) {
    // Coins

    val fullCoinsUpdatedObservable: Observable<Unit>
        get() = coinManager.fullCoinsUpdatedObservable


    fun fullCoins(filter: String, limit: Int = 20): List<FullCoin> {
        return coinManager.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return coinManager.fullCoins(coinUids)
    }

    fun fullCoinsByCoinTypes(coinTypes: List<CoinType>): List<FullCoin> {
        return coinManager.fullCoinsByCoinTypes(coinTypes)
    }

    fun marketInfosSingle(top: Int = 250): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(top)
    }

    fun advancedMarketInfosSingle(top: Int = 250): Single<List<MarketInfo>> {
        return coinManager.advancedMarketInfosSingle(top)
    }

    fun marketInfosSingle(coinUids: List<String>): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(coinUids)
    }

    fun marketInfosSingle(categoryUid: String): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(categoryUid)
    }

    fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String
    ): Single<MarketInfoOverview> {
        return coinManager.marketInfoOverviewSingle(coinUid, currencyCode, language)
    }

    fun platformCoin(coinType: CoinType): PlatformCoin? {
        return coinManager.platformCoin(coinType)
    }

    fun platformCoins(): List<PlatformCoin> {
        return coinManager.platformCoins()
    }

    fun platformCoins(coinTypes: List<CoinType>): List<PlatformCoin> {
        return coinManager.platformCoins(coinTypes)
    }

    fun platformCoinsByCoinTypeIds(coinTypeIds: List<String>): List<PlatformCoin> {
        return coinManager.platformCoinsByCoinTypeIds(coinTypeIds)
    }

    fun coins(filter: String, limit: Int = 20): List<Coin> {
        return coinManager.coins(filter, limit)
    }

    // Categories

    val coinCategoriesObservable: Observable<List<CoinCategory>>
        get() = coinCategoryManager.coinCategoriesObservable


    fun coinCategories(): List<CoinCategory> {
        return coinCategoryManager.coinCategories()
    }

    fun coinCategory(uid: String): CoinCategory? {
        return coinCategoryManager.coinCategory(uid)
    }

    fun sync() {
        coinSyncer.sync()
        coinCategorySyncer.sync()
        exchangeSyncer.sync()
    }

    // Coin Prices

    fun refreshCoinPrices(currencyCode: String) {
        coinPriceSyncManager.refresh(currencyCode)
    }

    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? {
        return coinPriceManager.coinPrice(coinUid, currencyCode)
    }

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        return coinPriceManager.coinPriceMap(coinUids, currencyCode)
    }

    fun coinPriceObservable(coinUid: String, currencyCode: String): Observable<CoinPrice> {
        return coinPriceSyncManager.coinPriceObservable(coinUid, currencyCode)
    }

    fun coinPriceMapObservable(
        coinUids: List<String>,
        currencyCode: String
    ): Observable<Map<String, CoinPrice>> {
        return coinPriceSyncManager.coinPriceMapObservable(coinUids, currencyCode)
    }

    // Posts

    fun postsSingle(): Single<List<Post>> {
        return postManager.postsSingle()
    }

    // Market Tickers

    fun marketTickersSingle(coinUid: String): Single<List<MarketTicker>> {
        return coinManager.marketTickersSingle(coinUid)
    }

    // Chart Info

    fun chartInfo(coinUid: String, currencyCode: String, chartType: ChartType): ChartInfo? {
        return chartManager.getChartInfo(coinUid, currencyCode, chartType)
    }

    fun getChartInfoAsync(
        coinUid: String,
        currencyCode: String,
        chartType: ChartType
    ): Observable<ChartInfo> {
        return chartSyncManager.chartInfoObservable(coinUid, currencyCode, chartType)
    }

    // Global Market Info

    fun globalMarketPointsSingle(currencyCode: String, timePeriod: TimePeriod): Single<List<GlobalMarketPoint>> {
        return globalMarketInfoManager.globalMarketInfoSingle(currencyCode, timePeriod)
    }

    companion object {
        fun getInstance(
            context: Context,
            hsApiBaseUrl: String,
            hsOldApiBaseUrl: String,
            cryptoCompareApiKey: String? = null
        ): MarketKit {
            val marketDatabase = MarketDatabase.getInstance(context)
            val hsProvider = HsProvider(hsApiBaseUrl, hsOldApiBaseUrl)
            val coinCategoryManager = CoinCategoryManager(CoinCategoryStorage(marketDatabase))
            val coinGeckoProvider = CoinGeckoProvider("https://api.coingecko.com/api/v3/")
            val exchangeManager = ExchangeManager(ExchangeStorage(marketDatabase))
            val exchangeSyncer = ExchangeSyncer(exchangeManager, coinGeckoProvider)
            val coinManager =
                CoinManager(CoinStorage(marketDatabase), hsProvider, coinCategoryManager, coinGeckoProvider, exchangeManager)
            val coinSyncer = CoinSyncer(hsProvider, coinManager)
            val coinCategorySyncer = CoinCategorySyncer(hsProvider, coinCategoryManager)
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider(cryptoCompareApiKey)
            val postManager = PostManager(cryptoCompareProvider)
            val chartManager = ChartManager(coinManager, ChartPointStorage(marketDatabase))
            val chartSchedulerFactory = ChartSchedulerFactory(chartManager, coinGeckoProvider)
            val chartSyncManager = ChartSyncManager(coinManager, chartSchedulerFactory).also {
                chartManager.listener = it
            }
            val globalMarketInfoStorage = GlobalMarketInfoStorage(marketDatabase)
            val globalMarketInfoManager = GlobalMarketInfoManager(hsProvider, globalMarketInfoStorage)

            return MarketKit(
                coinManager,
                coinCategoryManager,
                coinSyncer,
                coinCategorySyncer,
                coinPriceManager,
                coinPriceSyncManager,
                postManager,
                chartManager,
                exchangeSyncer,
                chartSyncManager,
                globalMarketInfoManager,
            )
        }
    }

}

//Errors

class NoChartInfo : Exception()

sealed class ProviderError : Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class NoCoinGeckoId : ProviderError()
}