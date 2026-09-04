package com.takuyafukumura.calculator.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY createdAt DESC, id DESC")
    fun observeHistory(): Flow<List<CalculationHistoryEntity>>

    @Insert
    suspend fun insert(history: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()

    @Query(
        """
        DELETE FROM calculation_history
        WHERE id NOT IN (
            SELECT id FROM calculation_history
            ORDER BY createdAt DESC, id DESC
            LIMIT :limit
        )
        """,
    )
    suspend fun trimToLimit(limit: Int)

    @Transaction
    suspend fun insertAndTrim(
        history: CalculationHistoryEntity,
        limit: Int,
    ) {
        insert(history)
        trimToLimit(limit)
    }
}
