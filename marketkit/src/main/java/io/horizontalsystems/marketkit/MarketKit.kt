package io.horizontalsystems.marketkit

import android.annotation.SuppressLint
import android.content.Context
import android.os.storage.StorageManager
import io.horizontalsystems.marketkit.chart.HsChartRequestHelper
import io.horizontalsystems.marketkit.customcurrency.CustomCurrenciesManager
import io.horizontalsystems.marketkit.customcurrency.CustomCurrencyProvider
import io.horizontalsystems.marketkit.customcurrency.CustomCurrencyStorage
import io.horizontalsystems.marketkit.customcurrency.convertValuesToCustomCurrency
import io.horizontalsystems.marketkit.managers.*
import io.horizontalsystems.marketkit.models.*
import io.horizontalsystems.marketkit.providers.*
import io.horizontalsystems.marketkit.storage.*
import io.horizontalsystems.marketkit.syncers.CoinSyncer
import io.horizontalsystems.marketkit.syncers.ExchangeSyncer
import io.horizontalsystems.marketkit.syncers.HsDataSyncer
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class MarketKit(
    private val nftManager: NftManager,
    private val marketOverviewManager: MarketOverviewManager,
    private val coinManager: CoinManager,
    private val coinSyncer: CoinSyncer,
    private val coinPriceManager: CoinPriceManager,
    private val coinHistoricalPriceManager: CoinHistoricalPriceManager,
    private val coinPriceSyncManager: CoinPriceSyncManager,
    private val postManager: PostManager,
    private val exchangeSyncer: ExchangeSyncer,
    private val globalMarketInfoManager: GlobalMarketInfoManager,
    private val hsProvider: HsProvider,
    private val hsDataSyncer: HsDataSyncer,
    private val customCurrenciesManager: CustomCurrenciesManager
) {
    // Coins

    val fullCoinsUpdatedObservable: Observable<Unit>
        get() = coinSyncer.fullCoinsUpdatedObservable

    fun fullCoins(filter: String, limit: Int = 20): List<FullCoin> {
        return coinManager.fullCoins(filter, limit)
    }

    fun fullCoins(coinUids: List<String>): List<FullCoin> {
        return coinManager.fullCoins(coinUids)
    }

    fun allCoins(): List<Coin> = coinManager.allCoins()

    fun token(query: TokenQuery): Token? =
        coinManager.token(query)

    fun tokens(queries: List<TokenQuery>): List<Token> =
        coinManager.tokens(queries)

    fun tokens(reference: String): List<Token> =
        coinManager.tokens(reference)

    fun tokens(blockchainType: BlockchainType, filter: String, limit: Int = 20): List<Token> =
        coinManager.tokens(blockchainType, filter, limit)

    fun blockchains(uids: List<String>): List<Blockchain> =
        coinManager.blockchains(uids)

    fun blockchain(uid: String): Blockchain? =
        coinManager.blockchain(uid)

    fun marketInfosSingle(top: Int, currencyCode: String, defi: Boolean = false): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(top, currencyCode, defi)
    }

    fun advancedMarketInfosSingle(top: Int = 250, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.advancedMarketInfosSingle(top, currencyCode)
    }

    fun marketInfosSingle(coinUids: List<String>, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(coinUids, currencyCode)
    }

    fun marketInfosSingle(categoryUid: String, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.marketInfosSingle(categoryUid, currencyCode)
    }

    fun marketInfoOverviewSingle(
        coinUid: String,
        currencyCode: String,
        language: String
    ): Single<MarketInfoOverview> {
        return coinManager.marketInfoOverviewSingle(coinUid, currencyCode, language)
    }

    fun marketInfoDetailsSingle(coinUid: String, currencyCode: String): Single<MarketInfoDetails> {
        return hsProvider.getMarketInfoDetails(coinUid, currencyCode).map {
            MarketInfoDetails(it)
        }
    }

    fun marketInfoTvlSingle(
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)
    }

    fun marketInfoGlobalTvlSingle(
        chain: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<ChartPoint>> {
        return hsProvider.marketInfoGlobalTvlSingle(chain, currencyCode, timePeriod)
    }

    fun defiMarketInfosSingle(currencyCode: String): Single<List<DefiMarketInfo>> {
        return coinManager.defiMarketInfosSingle(currencyCode)
    }

    // Categories

    fun coinCategoriesSingle(currencyCode: String): Single<List<CoinCategory>> =
        hsProvider.getCoinCategories(currencyCode)

    fun coinCategoryMarketPointsSingle(categoryUid: String, interval: HsTimePeriod, currencyCode: String) =
        hsProvider.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode)

    fun sync() {
        hsDataSyncer.sync()
        exchangeSyncer.sync()
    }

    // Coin Prices

    @SuppressLint("CheckResult")
    fun refreshCoinPrices(currencyCode: String) {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        if(customCurrency == null){
            coinPriceSyncManager.refresh(currencyCode)
        }else{
            coinPriceSyncManager.refresh("USD")
        }

    }


    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        return if(customCurrency == null)
            coinPriceManager.coinPrice(coinUid, currencyCode)
        else{
            val coinPriceInDollars = coinPriceManager.coinPrice(coinUid, "USD")
            coinPriceInDollars?.convertValuesToCustomCurrency(customCurrency)
        }
    }

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        return if(customCurrency == null)
            coinPriceManager.coinPriceMap(coinUids, currencyCode)
        else{
            coinPriceManager.coinPriceMap(coinUids, "USD").mapValues  { (key, value) ->
                value.convertValuesToCustomCurrency(customCurrency)
            }
        }
    }

    fun coinPriceObservable(coinUid: String, currencyCode: String): Observable<CoinPrice> {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        return if(customCurrency == null){
            coinPriceSyncManager.coinPriceObservable(coinUid, currencyCode)
        }else{
            coinPriceSyncManager.coinPriceObservable(coinUid, "USD")
                .map { it.convertValuesToCustomCurrency(customCurrency) }
        }
    }

    fun coinPriceMapObservable(
        coinUids: List<String>,
        currencyCode: String
    ): Observable<Map<String, CoinPrice>> {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        return if(customCurrency == null){
            coinPriceSyncManager.coinPriceMapObservable(coinUids, currencyCode)
        }else {
            coinPriceSyncManager.coinPriceMapObservable(coinUids, "USD").map { coinPriceMap ->
                coinPriceMap.mapValues { (_, coinPrice) ->
                    coinPrice.convertValuesToCustomCurrency(customCurrency)
                }
            }
        }
    }

    // Coin Historical Price

    fun coinHistoricalPriceSingle(
        coinUid: String,
        currencyCode: String,
        timestamp: Long
    ): Single<BigDecimal> {
        val customCurrency = customCurrenciesManager.fetchCustomCurrency(currencyCode)

        return if(customCurrency == null){
            coinHistoricalPriceManager.coinHistoricalPriceSingle(coinUid, currencyCode, timestamp)
        } else{
            coinHistoricalPriceManager.coinHistoricalPriceSingle(coinUid, "USD", timestamp)
                .timeout(5, TimeUnit.SECONDS)
                .map { priceUSD ->
                    priceUSD.multiply(customCurrency.currencyUnitsPerDollar)
                }
        }
    }

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long): BigDecimal? {
        val customCurrency = try {
            customCurrenciesManager.fetchCustomCurrency(currencyCode)
        } catch (e: Exception) {
            null
        }

        return if (customCurrency == null) {
            coinHistoricalPriceManager.coinHistoricalPrice(coinUid, currencyCode, timestamp)
        } else {
            val priceUSD = coinHistoricalPriceManager.coinHistoricalPrice(coinUid, "USD", timestamp)
            priceUSD?.multiply(customCurrency.currencyUnitsPerDollar)
        }
    }

    // Posts

    fun postsSingle(): Single<List<Post>> {
        return postManager.postsSingle()
    }

    // Market Tickers

    fun marketTickersSingle(coinUid: String): Single<List<MarketTicker>> {
        return coinManager.marketTickersSingle(coinUid)
    }

    // Details

    fun tokenHoldersSingle(
        authToken: String,
        coinUid: String,
        blockchainUid: String
    ): Single<TokenHolders> {
        return hsProvider.tokenHoldersSingle(authToken, coinUid, blockchainUid)
    }

    fun treasuriesSingle(coinUid: String, currencyCode: String): Single<List<CoinTreasury>> {
        return hsProvider.coinTreasuriesSingle(coinUid, currencyCode)
    }

    fun investmentsSingle(coinUid: String): Single<List<CoinInvestment>> {
        return hsProvider.investmentsSingle(coinUid)
    }

    fun coinReportsSingle(coinUid: String): Single<List<CoinReport>> {
        return hsProvider.coinReportsSingle(coinUid)
    }

    fun auditReportsSingle(addresses: List<String>): Single<List<Auditor>> {
        return coinManager.auditReportsSingle(addresses)
    }

    // Pro Data

    fun cexVolumesSingle(coinUid: String, currencyCode: String, timePeriod: HsTimePeriod): Single<List<ChartPoint>> {
        val periodType = HsPeriodType.ByPeriod(timePeriod)
        val currentTime = Date().time / 1000
        val fromTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
        val interval = HsPointTimePeriod.Day1
        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .map { response ->
                response.mapNotNull { chartCoinPrice ->
                    chartCoinPrice.totalVolume?.let { volume ->
                        ChartPoint(volume, chartCoinPrice.timestamp, null)
                    }
                }
            }
    }

    fun dexLiquiditySingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.VolumePoint>> {
        return hsProvider.dexLiquiditySingle(authToken, coinUid, currencyCode, timePeriod)
    }

    fun dexVolumesSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.VolumePoint>> {
        return hsProvider.dexVolumesSingle(authToken, coinUid, currencyCode, timePeriod)
    }

    fun transactionDataSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod,
        platform: String?
    ): Single<List<Analytics.CountVolumePoint>> {
        return hsProvider.transactionDataSingle(authToken, coinUid, timePeriod, platform)
    }

    fun activeAddressesSingle(
        authToken: String,
        coinUid: String,
        timePeriod: HsTimePeriod
    ): Single<List<Analytics.CountPoint>> {
        return hsProvider.activeAddressesSingle(authToken, coinUid, timePeriod)
    }

    fun analyticsPreviewSingle(coinUid: String, addresses: List<String>): Single<AnalyticsPreview> {
        return hsProvider.analyticsPreviewSingle(coinUid, addresses)
    }

    fun analyticsSingle(
        authToken: String,
        coinUid: String,
        currencyCode: String
    ): Single<Analytics> {
        return hsProvider.analyticsSingle(authToken, coinUid, currencyCode)
    }

    fun cexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "cex_volume", currencyCode)
    }

    fun dexVolumeRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "dex_volume", currencyCode)
    }

    fun dexLiquidityRanksSingle(authToken: String, currencyCode: String): Single<List<RankValue>> {
        return hsProvider.rankValueSingle(authToken, "dex_liquidity", currencyCode)
    }

    fun activeAddressRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "address", currencyCode)
    }

    fun transactionCountsRanksSingle(
        authToken: String,
        currencyCode: String
    ): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "tx_count", currencyCode)
    }

    fun holderRanksSingle(authToken: String, currencyCode: String): Single<List<RankValue>> {
        return hsProvider.rankValueSingle(authToken, "holders", currencyCode)
    }

    fun revenueRanksSingle(authToken: String, currencyCode: String): Single<List<RankMultiValue>> {
        return hsProvider.rankMultiValueSingle(authToken, "revenue", currencyCode)
    }

    // Overview
    fun marketOverviewSingle(currencyCode: String): Single<MarketOverview> =
        marketOverviewManager.marketOverviewSingle(currencyCode)


    fun topMoversSingle(currencyCode: String): Single<TopMovers> =
        coinManager.topMoversSingle(currencyCode)

    // Chart Info

    fun chartPointsSingle(
        coinUid: String,
        currencyCode: String,
        periodType: HsPeriodType
    ): Single<List<ChartPoint>> {
        val interval: HsPointTimePeriod
        var fromTimestamp: Long? = null
        when (periodType) {
            is HsPeriodType.ByPeriod -> {
                val currentTime = Date().time / 1000
                fromTimestamp = HsChartRequestHelper.fromTimestamp(currentTime, periodType)
                interval = HsChartRequestHelper.pointInterval(periodType)
            }
            is HsPeriodType.ByStartTime -> {
                interval = HsChartRequestHelper.pointInterval(periodType)
            }
        }
        return hsProvider.coinPriceChartSingle(coinUid, currencyCode, interval, fromTimestamp)
            .map { response -> response.map { it.chartPoint } }
    }

    fun chartStartTimeSingle(coinUid: String): Single<Long> {
        return hsProvider.coinPriceChartStartTime(coinUid)
    }

    // Global Market Info

    fun globalMarketPointsSingle(currencyCode: String, timePeriod: HsTimePeriod): Single<List<GlobalMarketPoint>> {
        return globalMarketInfoManager.globalMarketInfoSingle(currencyCode, timePeriod)
    }

    fun topPlatformsSingle(currencyCode: String): Single<List<TopPlatform>> {
        return hsProvider.topPlatformsSingle(currencyCode)
            .map { responseList -> responseList.map { it.topPlatform } }
    }

    fun topPlatformMarketCapPointsSingle(chain: String, timePeriod: HsTimePeriod, currencyCode: String): Single<List<TopPlatformMarketCapPoint>> {
        return hsProvider.topPlatformMarketCapPointsSingle(chain, timePeriod, currencyCode)
    }

    fun topPlatformCoinListSingle(chain: String, currencyCode: String): Single<List<MarketInfo>> {
        return coinManager.topPlatformCoinListSingle(chain, currencyCode)
    }

    // NFT

    suspend fun nftTopCollections(): List<NftTopCollection> = nftManager.topCollections()

    // Auth

    fun authGetSignMessage(address: String): Single<String> {
        return hsProvider.authGetSignMessage(address)
    }

    fun authenticate(signature: String, address: String): Single<String> {
        return hsProvider.authenticate(signature, address)
    }

    //Misc

    fun syncInfo() : SyncInfo {
        return coinSyncer.syncInfo()
    }

    companion object {
        fun getInstance(
            context: Context,
            hsApiBaseUrl: String,
            hsApiKey: String,
            cryptoCompareApiKey: String? = null,
            defiYieldApiKey: String? = null
        ): MarketKit {
            // init cache
            (context.getSystemService(Context.STORAGE_SERVICE) as StorageManager?)?.let { storageManager ->
                val cacheDir = context.cacheDir
                val cacheQuotaBytes = storageManager.getCacheQuotaBytes(storageManager.getUuidForPath(cacheDir))

                HSCache.cacheDir = cacheDir
                HSCache.cacheQuotaBytes = cacheQuotaBytes
            }

            val marketDatabase = MarketDatabase.getInstance(context)
            val customCurrenciesStorage = CustomCurrencyStorage(marketDatabase)
            val customCurrenciesProvider = CustomCurrencyProvider.Mock()
            val customCurrenciesManager = CustomCurrenciesManager.Base(customCurrenciesStorage,customCurrenciesProvider)
            val hsProvider = HsProvider(hsApiBaseUrl, hsApiKey,customCurrenciesManager)
            val hsNftProvider = HsNftProvider(hsApiBaseUrl, hsApiKey)
            val coinGeckoProvider = CoinGeckoProvider("https://api.coingecko.com/api/v3/")
            val defiYieldProvider = DefiYieldProvider(defiYieldApiKey)
            val exchangeManager = ExchangeManager(ExchangeStorage(marketDatabase))
            val exchangeSyncer = ExchangeSyncer(exchangeManager, coinGeckoProvider, marketDatabase.syncerStateDao())
            val coinStorage = CoinStorage(marketDatabase)
            val coinManager = CoinManager(coinStorage, hsProvider, coinGeckoProvider, defiYieldProvider, exchangeManager)
            val nftManager = NftManager(coinManager, hsNftProvider)
            val marketOverviewManager = MarketOverviewManager(nftManager, hsProvider)
            val coinSyncer = CoinSyncer(hsProvider, coinStorage, marketDatabase.syncerStateDao())
            val coinPriceManager = CoinPriceManager(CoinPriceStorage(marketDatabase))
            val coinHistoricalPriceManager = CoinHistoricalPriceManager(
                CoinHistoricalPriceStorage(marketDatabase),
                hsProvider,
            )
            val coinPriceSchedulerFactory = CoinPriceSchedulerFactory(coinPriceManager, hsProvider)
            val coinPriceSyncManager = CoinPriceSyncManager(coinPriceSchedulerFactory)
            coinPriceManager.listener = coinPriceSyncManager
            val cryptoCompareProvider = CryptoCompareProvider(cryptoCompareApiKey)
            val postManager = PostManager(cryptoCompareProvider)
            val globalMarketInfoStorage = GlobalMarketInfoStorage(marketDatabase)
            val globalMarketInfoManager = GlobalMarketInfoManager(hsProvider, globalMarketInfoStorage)
            val hsDataSyncer = HsDataSyncer(coinSyncer, hsProvider)

            return MarketKit(
                nftManager,
                marketOverviewManager,
                coinManager,
                coinSyncer,
                coinPriceManager,
                coinHistoricalPriceManager,
                coinPriceSyncManager,
                postManager,
                exchangeSyncer,
                globalMarketInfoManager,
                hsProvider,
                hsDataSyncer,
                customCurrenciesManager
            )
        }
    }

}

//Errors

class NoChartData : Exception()
class NoChartInfo : Exception()

sealed class ProviderError : Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class ReturnedTimestampIsVeryInaccurate : ProviderError()
}

class SyncInfo(
    val coinsTimestamp: String?,
    val blockchainsTimestamp: String?,
    val tokensTimestamp: String?
)
