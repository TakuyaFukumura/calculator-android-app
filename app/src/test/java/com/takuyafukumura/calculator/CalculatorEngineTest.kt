package com.takuyafukumura.calculator

import com.takuyafukumura.calculator.calculator.CalculatorEngine
import com.takuyafukumura.calculator.calculator.Operator
import com.takuyafukumura.calculator.calculator.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {
    private val engine = CalculatorEngine()

    @Test
    fun evaluatesWithOperatorPrecedence() {
        val result =
            engine.evaluate(
                listOf(
                    Token.Number(BigDecimal("2")),
                    Token.OperatorToken(Operator.ADD),
                    Token.Number(BigDecimal("3")),
                    Token.OperatorToken(Operator.MULTIPLY),
                    Token.Number(BigDecimal("4")),
                ),
            )

        assertEquals(BigDecimal("14"), result.getOrThrow())
    }

    @Test
    fun divisionByZeroReturnsFailure() {
        val result =
            engine.evaluate(
                listOf(
                    Token.Number(BigDecimal("5")),
                    Token.OperatorToken(Operator.DIVIDE),
                    Token.Number(BigDecimal.ZERO),
                ),
            )

        assertTrue(result.isFailure)
    }
}
