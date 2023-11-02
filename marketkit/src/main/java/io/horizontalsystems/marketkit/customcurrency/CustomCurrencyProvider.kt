package io.horizontalsystems.marketkit.customcurrency

import io.reactivex.Single
import java.math.BigDecimal
import kotlin.random.Random

interface CustomCurrencyProvider {
    fun customCurrencies(): Single<List<CustomCurrency>?>

    class Mock() : CustomCurrencyProvider{
        private val mockCurrencies = listOf(
            CustomCurrency("+255", "TZS", BigDecimal(2502.0 + Random.nextDouble(-2.0, 2.0)), "TZs", null),
            CustomCurrency("+256", "UGX", BigDecimal(3765.20 + Random.nextDouble(-4.0, 4.0)), "UGX", null),
            CustomCurrency("+257", "BIF", BigDecimal(2839.44 + Random.nextDouble(-2.0, 2.0)), "₣", null),
            CustomCurrency("+258", "MZN", BigDecimal(63.85 + Random.nextDouble(-0.01, 0.01)), "MZN", null)
        )
        override fun customCurrencies(): Single<List<CustomCurrency>?> = Single.just(mockCurrencies)
    }
}