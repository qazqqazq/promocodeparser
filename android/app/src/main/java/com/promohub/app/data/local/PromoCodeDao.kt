package com.promohub.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoCodeDao {

    @Query("SELECT * FROM promocodes WHERE isCached = 1 ORDER BY createdAt DESC")
    fun getAllPromoCodes(): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promocodes WHERE service = :service AND isCached = 1")
    fun getPromoCodesByService(service: String): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promocodes WHERE isCached = 1 AND (title LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%')")
    fun searchPromoCodes(query: String): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promocodes WHERE id = :id")
    suspend fun getPromoCodeById(id: Int): PromoCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(promoCodes: List<PromoCodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promoCode: PromoCodeEntity)

    @Update
    suspend fun update(promoCode: PromoCodeEntity)

    @Delete
    suspend fun delete(promoCode: PromoCodeEntity)

    @Query("DELETE FROM promocodes WHERE isCached = 1")
    suspend fun clearCache()
}
