package com.takuyafukumura.calculator.data.repository

import com.takuyafukumura.calculator.data.dao.CalculationHistoryDao
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

interface CalculationHistoryRepository {
    fun observeHistory(): Flow<List<CalculationHistoryEntity>>

    suspend fun save(
        expression: String,
        result: String,
        createdAt: Long = System.currentTimeMillis(),
    )

    suspend fun delete(id: Long)

    suspend fun deleteAll()
}

class RoomCalculationHistoryRepository(
    private val dao: CalculationHistoryDao,
) : CalculationHistoryRepository {
    override fun observeHistory(): Flow<List<CalculationHistoryEntity>> = dao.observeHistory()

    override suspend fun save(
        expression: String,
        result: String,
        createdAt: Long,
    ) {
        dao.insertAndTrim(
            CalculationHistoryEntity(
                expression = expression,
                result = result,
                createdAt = createdAt,
            ),
            MAX_ITEMS,
        )
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    private companion object {
        const val MAX_ITEMS = 100
    }
}
