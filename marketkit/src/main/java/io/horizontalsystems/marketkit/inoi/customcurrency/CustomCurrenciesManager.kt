package io.horizontalsystems.marketkit.inoi.customcurrency

import android.annotation.SuppressLint
import android.util.Log
import java.math.BigDecimal
import kotlin.random.Random

interface CustomCurrenciesManager {
    fun fetchCustomCurrency(currencyCode: String): CustomCurrency?
    fun updateCustomCurrencies()

    @SuppressLint("CheckResult")
    class Base(
        private val customCurrencyStorage: CustomCurrencyStorage,
        private val customCurrencyProvider: CustomCurrencyProvider,
    ) : CustomCurrenciesManager {

        init {
            updateCustomCurrencies()
        }

        override fun fetchCustomCurrency(currencyCode: String): CustomCurrency? {
            return customCurrencyStorage.customCurrency(currencyCode)
        }

        override fun updateCustomCurrencies() {
            customCurrencyProvider.customCurrencies().subscribe({
                it?.let { customCurrencyStorage.save(it) }
            }, {
                Log.e("CustomCurrenciesManager", "Failed to load custom currencies")
            })
        }
    }

  class Mock() : CustomCurrenciesManager {

    private val mockCurrencies = listOf(
      CustomCurrency("+255", "TZS", BigDecimal(2502.0 + Random.nextDouble(-2.0, 2.0)), "TZs", null),
      CustomCurrency("+256", "UGX", BigDecimal(3765.20 + Random.nextDouble(-4.0, 4.0)), "UGX", null),
      CustomCurrency("+257", "BIF", BigDecimal(2839.44 + Random.nextDouble(-2.0, 2.0)), "₣", null),
      CustomCurrency("+258", "MZN", BigDecimal(63.85 + Random.nextDouble(-0.01, 0.01)), "MZN", null)
    )

    override fun fetchCustomCurrency(currencyCode: String): CustomCurrency? =
      mockCurrencies.firstOrNull{ it.currencyCode == currencyCode }
    override fun updateCustomCurrencies() = Unit
  }
}
