package com.takuyafukumura.calculator

import com.takuyafukumura.calculator.data.dao.CalculationHistoryDao
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import com.takuyafukumura.calculator.data.repository.RoomCalculationHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationHistoryRepositoryTest {
    @Test
    fun savingMoreThanLimitRemovesOldestEntry() =
        runBlocking {
            val dao = FakeCalculationHistoryDao()
            val repository = RoomCalculationHistoryRepository(dao)

            repeat(101) { index ->
                repository.save(
                    expression = index.toString(),
                    result = index.toString(),
                    createdAt = index.toLong(),
                )
            }

            assertEquals(100, dao.entries.size)
            assertEquals("100", dao.entries.first().expression)
            assertEquals("1", dao.entries.last().expression)
        }
}

private class FakeCalculationHistoryDao : CalculationHistoryDao {
    private val state = MutableStateFlow<List<CalculationHistoryEntity>>(emptyList())
    val entries: List<CalculationHistoryEntity>
        get() = state.value.sortedWith(compareByDescending<CalculationHistoryEntity> { it.createdAt }.thenByDescending { it.id })

    override fun observeHistory(): Flow<List<CalculationHistoryEntity>> = state

    override suspend fun insert(history: CalculationHistoryEntity) {
        state.value = state.value + history.copy(id = state.value.size.toLong() + 1)
    }

    override suspend fun deleteById(id: Long): Int {
        val before = state.value.size
        state.value = state.value.filterNot { it.id == id }
        return before - state.value.size
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }

    override suspend fun trimToLimit(limit: Int) {
        state.value = entries.take(limit)
    }
}
