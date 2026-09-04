package com.takuyafukumura.calculator.calculator

enum class Operator(
    val symbol: String,
    val precedence: Int,
) {
    ADD("+", 1),
    SUBTRACT("−", 1),
    MULTIPLY("×", 2),
    DIVIDE("÷", 2),
}
