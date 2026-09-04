package com.takuyafukumura.calculator.calculator

import java.math.BigDecimal

object CalculatorFormatter {
    fun format(value: BigDecimal): String {
        val normalized = value.stripTrailingZeros()
        return if (normalized.compareTo(BigDecimal.ZERO) == 0) {
            "0"
        } else {
            normalized.toPlainString()
        }
    }
}
