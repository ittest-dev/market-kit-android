package io.horizontalsystems.marketkit.customcurrency

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomCurrencyDao {
    @Query("SELECT * FROM CustomCurrencyEntity WHERE currencyCode=:currencyCode")
    fun getCustomCurrency(currencyCode: String): CustomCurrencyEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currency: List<CustomCurrencyEntity>)
}