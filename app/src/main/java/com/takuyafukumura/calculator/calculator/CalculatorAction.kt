package com.takuyafukumura.calculator.calculator

sealed interface CalculatorAction {
    data class DigitPressed(
        val digit: Int,
    ) : CalculatorAction

    data object DecimalPressed : CalculatorAction

    data class OperatorPressed(
        val operator: Operator,
    ) : CalculatorAction

    data object EqualsPressed : CalculatorAction

    data object AllClearPressed : CalculatorAction

    data object DeletePressed : CalculatorAction

    data object SignTogglePressed : CalculatorAction

    data object PercentPressed : CalculatorAction
}
