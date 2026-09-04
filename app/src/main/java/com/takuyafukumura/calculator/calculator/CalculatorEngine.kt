package com.takuyafukumura.calculator.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculatorEngine
    @Inject
    constructor() {
        fun evaluate(tokens: List<Token>): Result<BigDecimal> =
            runCatching {
                if (!isValidExpression(tokens)) {
                    throw CalculatorException.InvalidExpression
                }

                val values = ArrayDeque<BigDecimal>()
                val operators = ArrayDeque<Operator>()
                tokens.forEach { token ->
                    when (token) {
                        is Token.Number -> values.addLast(token.value)
                        is Token.OperatorToken -> {
                            while (operators.isNotEmpty() &&
                                operators.last().precedence >= token.operator.precedence
                            ) {
                                applyOperator(values, operators.removeLast())
                            }
                            operators.addLast(token.operator)
                        }
                    }
                }
                while (operators.isNotEmpty()) {
                    applyOperator(values, operators.removeLast())
                }
                require(values.size == 1)
                values.removeLast()
            }

        private fun applyOperator(
            values: ArrayDeque<BigDecimal>,
            operator: Operator,
        ) {
            require(values.size >= 2)
            val right = values.removeLast()
            val left = values.removeLast()
            val result =
                when (operator) {
                    Operator.ADD -> left + right
                    Operator.SUBTRACT -> left - right
                    Operator.MULTIPLY -> left * right
                    Operator.DIVIDE -> {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw CalculatorException.DivisionByZero
                        }
                        left.divide(right, DIVISION_SCALE, RoundingMode.HALF_UP)
                    }
                }
            values.addLast(result)
        }

        private fun isValidExpression(tokens: List<Token>): Boolean =
            tokens.isNotEmpty() &&
                tokens.first() is Token.Number &&
                tokens.last() is Token.Number &&
                tokens.withIndex().all { (index, token) ->
                    if (index % 2 == 0) token is Token.Number else token is Token.OperatorToken
                }

        private companion object {
            const val DIVISION_SCALE = 12
        }
    }
