package com.takuyafukumura.calculator.calculator

import java.math.BigDecimal

sealed interface Token {
    data class Number(
        val value: BigDecimal,
    ) : Token

    data class OperatorToken(
        val operator: Operator,
    ) : Token
}
