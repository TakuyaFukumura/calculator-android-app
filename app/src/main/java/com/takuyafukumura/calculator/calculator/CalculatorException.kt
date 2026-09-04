package com.takuyafukumura.calculator.calculator

sealed class CalculatorException : Exception() {
    data object DivisionByZero : CalculatorException()

    data object InvalidExpression : CalculatorException()
}
