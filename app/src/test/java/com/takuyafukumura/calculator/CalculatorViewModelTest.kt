package com.takuyafukumura.calculator

import com.takuyafukumura.calculator.calculator.CalculatorAction
import com.takuyafukumura.calculator.calculator.CalculatorEngine
import com.takuyafukumura.calculator.calculator.CalculatorMode
import com.takuyafukumura.calculator.calculator.CalculatorViewModel
import com.takuyafukumura.calculator.calculator.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
