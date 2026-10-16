package com.example.biztrackci.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.data.local.entity.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleWithItems>>

    @Query("SELECT SUM(totalAmount) FROM sales WHERE timestamp >= :startDate AND timestamp <= :endDate")
    fun getTotalSalesBetween(startDate: Long, endDate: Long): Flow<Double?>
}
