package com.takuyafukumura.calculator.calculator

data class CalculatorUiState(
    val expression: String = "",
    val displayValue: String = "0",
    val input: String = "0",
    val tokens: List<Token> = emptyList(),
    val mode: CalculatorMode = CalculatorMode.INPUT,
    val errorMessage: String? = null,
)
