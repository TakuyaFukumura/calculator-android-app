package com.takuyafukumura.calculator

import com.takuyafukumura.calculator.calculator.CalculatorAction
import com.takuyafukumura.calculator.calculator.CalculatorEngine
import com.takuyafukumura.calculator.calculator.CalculatorMode
import com.takuyafukumura.calculator.calculator.CalculatorViewModel
import com.takuyafukumura.calculator.calculator.Operator
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import com.takuyafukumura.calculator.data.repository.CalculationHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {
    @Test
    fun calculatesExpressionAndStartsNewInputAfterResult() {
        val viewModel = CalculatorViewModel(CalculatorEngine())

        viewModel.onAction(CalculatorAction.DigitPressed(2))
        viewModel.onAction(CalculatorAction.OperatorPressed(Operator.ADD))
        viewModel.onAction(CalculatorAction.DigitPressed(3))
        viewModel.onAction(CalculatorAction.EqualsPressed)

        assertEquals("5", viewModel.uiState.value.displayValue)
        assertEquals(CalculatorMode.RESULT, viewModel.uiState.value.mode)

        viewModel.onAction(CalculatorAction.DigitPressed(7))

        assertEquals("7", viewModel.uiState.value.displayValue)
        assertEquals(CalculatorMode.INPUT, viewModel.uiState.value.mode)
    }

    @Test
    fun divisionByZeroShowsRecoverableError() {
        val viewModel = CalculatorViewModel(CalculatorEngine())

        viewModel.onAction(CalculatorAction.DigitPressed(5))
        viewModel.onAction(CalculatorAction.OperatorPressed(Operator.DIVIDE))
        viewModel.onAction(CalculatorAction.DigitPressed(0))
        viewModel.onAction(CalculatorAction.EqualsPressed)

        assertEquals(CalculatorMode.ERROR, viewModel.uiState.value.mode)
        assertEquals("0で割ることはできません", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun successfulCalculationIsSavedToHistory() =
        runTest {
            val repository = FakeCalculationHistoryRepository()
            val viewModel = CalculatorViewModel(CalculatorEngine(), repository)

            viewModel.onAction(CalculatorAction.DigitPressed(2))
            viewModel.onAction(CalculatorAction.OperatorPressed(Operator.ADD))
            viewModel.onAction(CalculatorAction.DigitPressed(3))
            viewModel.onAction(CalculatorAction.EqualsPressed)
            advanceUntilIdle()

            assertEquals("2 + 3", repository.entries.single().expression)
            assertEquals("5", repository.entries.single().result)
        }

    @Test
    fun failedCalculationIsNotSavedToHistory() =
        runTest {
            val repository = FakeCalculationHistoryRepository()
            val viewModel = CalculatorViewModel(CalculatorEngine(), repository)

            viewModel.onAction(CalculatorAction.DigitPressed(5))
            viewModel.onAction(CalculatorAction.OperatorPressed(Operator.DIVIDE))
            viewModel.onAction(CalculatorAction.DigitPressed(0))
            viewModel.onAction(CalculatorAction.EqualsPressed)
            advanceUntilIdle()

            assertEquals(emptyList<CalculationHistoryEntity>(), repository.entries)
        }

    @Test
    fun restoresSavedExpressionAsEditableInput() {
        val viewModel = CalculatorViewModel(CalculatorEngine())

        assertTrue(viewModel.restoreExpression("12 + 3 × 4"))
        viewModel.onAction(CalculatorAction.DeletePressed)
        viewModel.onAction(CalculatorAction.DigitPressed(5))

        assertEquals("12 + 3 × 5", viewModel.uiState.value.expression)
    }
}

private class FakeCalculationHistoryRepository : CalculationHistoryRepository {
    private val state = MutableStateFlow<List<CalculationHistoryEntity>>(emptyList())
    val entries: List<CalculationHistoryEntity>
        get() = state.value

    override fun observeHistory(): Flow<List<CalculationHistoryEntity>> = state

    override suspend fun save(
        expression: String,
        result: String,
        createdAt: Long,
    ) {
        state.value =
            state.value +
            CalculationHistoryEntity(
                id = state.value.size.toLong() + 1,
                expression = expression,
                result = result,
                createdAt = createdAt,
            )
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}
