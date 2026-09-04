package com.example.myapplication

import com.example.myapplication.data.entity.StringEntity
import com.example.myapplication.data.repository.StringRepository
import com.example.myapplication.di.ErrorLogger
import com.example.myapplication.ui.viewmodel.MainError
import com.example.myapplication.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addString_trimsValueAndUpdatesState() =
        runTest {
            val repository = FakeStringRepository()
            val viewModel = MainViewModel(repository, FakeErrorLogger())

            viewModel.addString("  hello  ")
            advanceUntilIdle()

            assertEquals(listOf(StringEntity(id = 1, value = "hello")), repository.values)
            assertEquals("hello", viewModel.uiState.value.greeting)
            assertEquals("文字列「hello」を追加しました", viewModel.uiState.value.operationMessage)
            assertFalse(viewModel.uiState.value.isOperationInProgress)
        }

    @Test
    fun addString_blankValueIsRejected() =
        runTest {
            val repository = FakeStringRepository()
            val viewModel = MainViewModel(repository, FakeErrorLogger())

            viewModel.addString("   ")
            advanceUntilIdle()

            assertTrue(repository.values.isEmpty())
            assertEquals(MainError.INPUT_BLANK, viewModel.uiState.value.error)
        }

    @Test
    fun updateAndDeleteString_changeRepositoryAndState() =
        runTest {
            val repository = FakeStringRepository()
            val viewModel = MainViewModel(repository, FakeErrorLogger())

            viewModel.addString("first")
            advanceUntilIdle()
            viewModel.updateString(1, "updated")
            advanceUntilIdle()
            viewModel.deleteString(1)
            advanceUntilIdle()

            assertTrue(repository.values.isEmpty())
            assertEquals("world", viewModel.uiState.value.greeting)
            assertEquals("文字列を削除しました", viewModel.uiState.value.operationMessage)
        }

    @Test
    fun repositoryFailureIsExposedAsError() =
        runTest {
            val viewModel = MainViewModel(FakeStringRepository(shouldFail = true), FakeErrorLogger())

            advanceUntilIdle()

            assertEquals(MainError.LOAD_FAILED, viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun addString_rejectsValuesLongerThanAllowedBoundaryAfterTrim() =
        runTest {
            val repository = FakeStringRepository()
            val viewModel = MainViewModel(repository, FakeErrorLogger())

            viewModel.addString("a".repeat(101))
            advanceUntilIdle()

            assertEquals(
                100,
                repository.values
                    .single()
                    .value.length,
            )
        }

    private class FakeErrorLogger : ErrorLogger {
        override fun error(
            tag: String,
            message: String,
            throwable: Throwable,
        ) = Unit
    }
}

private class FakeStringRepository(
    private val shouldFail: Boolean = false,
) : StringRepository {
    private val state = MutableStateFlow<List<StringEntity>>(emptyList())
    val values: MutableList<StringEntity> = mutableListOf()

    override fun getAllStrings(): Flow<List<StringEntity>> =
        if (shouldFail) {
            kotlinx.coroutines.flow.flow { throw IllegalStateException("test") }
        } else {
            state
        }

    override suspend fun getFirstString(): StringEntity? = values.firstOrNull()

    override suspend fun insertString(string: StringEntity) {
        if (shouldFail) error("test")
        val inserted = string.copy(id = values.size + 1)
        values += inserted
        state.value = values.toList()
    }

    override suspend fun updateString(string: StringEntity) {
        values[values.indexOfFirst { it.id == string.id }] = string
        state.value = values.toList()
    }

    override suspend fun deleteString(string: StringEntity) {
        deleteStringById(string.id)
    }

    override suspend fun deleteStringById(id: Int): Int {
        val removed = values.removeIf { it.id == id }
        state.value = values.toList()
        return if (removed) 1 else 0
    }

    override suspend fun deleteAllStrings() {
        values.clear()
        state.value = emptyList()
    }
}
