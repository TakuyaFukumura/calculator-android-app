package com.takuyafukumura.calculator.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import com.takuyafukumura.calculator.data.repository.CalculationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class CalculatorViewModel
    @Inject
    constructor(
        private val engine: CalculatorEngine,
        private val historyRepository: CalculationHistoryRepository,
    ) : ViewModel() {
        constructor(engine: CalculatorEngine) : this(engine, NoOpCalculationHistoryRepository)

        private val _uiState = MutableStateFlow(CalculatorUiState())
        val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

        fun onAction(action: CalculatorAction) {
            when (action) {
                is CalculatorAction.DigitPressed -> pressDigit(action.digit)
                CalculatorAction.DecimalPressed -> pressDecimal()
                is CalculatorAction.OperatorPressed -> pressOperator(action.operator)
                CalculatorAction.EqualsPressed -> calculate()
                CalculatorAction.AllClearPressed -> update(CalculatorUiState())
                CalculatorAction.DeletePressed -> delete()
                CalculatorAction.SignTogglePressed -> toggleSign()
                CalculatorAction.PercentPressed -> percent()
            }
        }

        private fun pressDigit(digit: Int) {
            require(digit in 0..9)
            val state = _uiState.value
            val input =
                if (state.mode != CalculatorMode.INPUT || state.input == "0") {
                    digit.toString()
                } else {
                    state.input + digit
                }
            update(
                state.copy(
                    expression = expressionFor(state.tokens, input),
                    displayValue = input,
                    input = input,
                    mode = CalculatorMode.INPUT,
                    errorMessage = null,
                    tokens =
                        if (state.mode == CalculatorMode.RESULT || state.mode == CalculatorMode.ERROR) {
                            emptyList()
                        } else {
                            state.tokens
                        },
                ),
            )
        }

        private fun pressDecimal() {
            val state = _uiState.value
            if (state.mode == CalculatorMode.RESULT || state.mode == CalculatorMode.ERROR) {
                update(CalculatorUiState(input = "0.", displayValue = "0.", expression = "0."))
            } else if (!state.input.contains('.')) {
                val input = "${state.input}."
                update(state.copy(input = input, displayValue = input, expression = expressionFor(state.tokens, input)))
            }
        }

        private fun pressOperator(operator: Operator) {
            val state = _uiState.value
            val baseTokens =
                if (state.mode == CalculatorMode.RESULT) {
                    listOf(Token.Number(state.displayValue.toBigDecimal()))
                } else {
                    state.tokens
                }
            val tokens = baseTokens.toMutableList()
            val number = state.input.toBigDecimalOrNull()
            if (number != null && shouldAppendCurrentNumber(tokens, state.input)) {
                tokens.add(Token.Number(number))
            }
            if (tokens.lastOrNull() is Token.OperatorToken) {
                tokens[tokens.lastIndex] = Token.OperatorToken(operator)
            } else {
                tokens.add(Token.OperatorToken(operator))
            }
            update(
                state.copy(
                    tokens = tokens,
                    input = "0",
                    displayValue = "0",
                    expression = expressionFor(tokens, "0"),
                    mode = CalculatorMode.INPUT,
                    errorMessage = null,
                ),
            )
        }

        private fun calculate() {
            val state = _uiState.value
            val number = state.input.toBigDecimalOrNull() ?: return
            val tokens = state.tokens + Token.Number(number)
            engine
                .evaluate(tokens)
                .onSuccess { result ->
                    val formatted = CalculatorFormatter.format(result)
                    update(
                        state.copy(
                            expression = expressionFor(tokens, ""),
                            displayValue = formatted,
                            input = formatted,
                            tokens = emptyList(),
                            mode = CalculatorMode.RESULT,
                            errorMessage = null,
                            historyErrorMessage = null,
                        ),
                    )
                    viewModelScope.launch {
                        runCatching {
                            historyRepository.save(
                                expression = expressionFor(tokens, ""),
                                result = formatted,
                            )
                        }.onFailure {
                            _uiState.value =
                                _uiState.value.copy(
                                    historyErrorMessage = "計算履歴の保存に失敗しました",
                                )
                        }
                    }
                }.onFailure { exception ->
                    val message =
                        if (exception is CalculatorException.DivisionByZero) {
                            "0で割ることはできません"
                        } else {
                            "入力を確認してください"
                        }
                    update(state.copy(mode = CalculatorMode.ERROR, errorMessage = message, displayValue = message))
                }
        }

        private fun delete() {
            val state = _uiState.value
            if (state.mode != CalculatorMode.INPUT) {
                update(CalculatorUiState())
                return
            }
            val input = state.input.dropLast(1).ifEmpty { "0" }
            update(state.copy(input = input, displayValue = input, expression = expressionFor(state.tokens, input)))
        }

        private fun toggleSign() {
            val state = _uiState.value
            if (state.mode == CalculatorMode.ERROR) return
            val input =
                when {
                    state.input == "0" -> "0"
                    state.input.startsWith("-") -> state.input.drop(1)
                    else -> "-${state.input}"
                }
            update(state.copy(input = input, displayValue = input, expression = expressionFor(state.tokens, input)))
        }

        private fun percent() {
            val state = _uiState.value
            val value = state.input.toBigDecimalOrNull() ?: return
            val input = CalculatorFormatter.format(value.divide(BigDecimal(100)))
            update(state.copy(input = input, displayValue = input, expression = expressionFor(state.tokens, input)))
        }

        private fun expressionFor(
            tokens: List<Token>,
            input: String,
        ): String =
            buildString {
                tokens.forEach { token ->
                    append(
                        when (token) {
                            is Token.Number -> CalculatorFormatter.format(token.value)
                            is Token.OperatorToken -> " ${token.operator.symbol} "
                        },
                    )
                }
                if (tokens.isEmpty() || input != "0") append(input)
            }.trim()

        private fun update(state: CalculatorUiState) {
            _uiState.value = state
        }

        fun restoreExpression(expression: String): Boolean {
            val parsedTokens = parseExpression(expression) ?: return false
            val lastNumber = parsedTokens.lastOrNull() as? Token.Number ?: return false
            val precedingTokens = parsedTokens.dropLast(1)
            val input = CalculatorFormatter.format(lastNumber.value)
            update(
                CalculatorUiState(
                    expression = expressionFor(precedingTokens, input),
                    displayValue = input,
                    input = input,
                    tokens = precedingTokens,
                ),
            )
            return true
        }

        private fun parseExpression(expression: String): List<Token>? {
            val tokens = mutableListOf<Token>()
            var index = 0
            while (index < expression.length) {
                while (index < expression.length && expression[index].isWhitespace()) index++
                if (index >= expression.length) break

                val startsNegative =
                    expression[index] == '-' &&
                        (tokens.isEmpty() || tokens.last() is Token.OperatorToken)
                if (expression[index].isDigit() || expression[index] == '.' || startsNegative) {
                    val start = index
                    if (startsNegative) index++
                    var hasDigit = false
                    var hasDecimal = false
                    while (index < expression.length) {
                        val character = expression[index]
                        when {
                            character.isDigit() -> {
                                hasDigit = true
                                index++
                            }
                            character == '.' && !hasDecimal -> {
                                hasDecimal = true
                                index++
                            }
                            else -> break
                        }
                    }
                    if (!hasDigit) return null
                    val number = expression.substring(start, index).toBigDecimalOrNull() ?: return null
                    tokens += Token.Number(number)
                } else {
                    val operator =
                        when (expression[index]) {
                            '+' -> Operator.ADD
                            '−' -> Operator.SUBTRACT
                            '×' -> Operator.MULTIPLY
                            '÷' -> Operator.DIVIDE
                            else -> return null
                        }
                    tokens += Token.OperatorToken(operator)
                    index++
                }
            }
            return tokens.takeIf { it.isNotEmpty() && it.last() is Token.Number && it.size % 2 == 1 }
        }

        private fun shouldAppendCurrentNumber(
            tokens: List<Token>,
            input: String,
        ): Boolean = tokens.isEmpty() || (tokens.last() is Token.OperatorToken && input != "0")

        private object NoOpCalculationHistoryRepository : CalculationHistoryRepository {
            override fun observeHistory(): Flow<List<CalculationHistoryEntity>> = emptyFlow()

            override suspend fun save(
                expression: String,
                result: String,
                createdAt: Long,
            ) = Unit

            override suspend fun delete(id: Long) = Unit

            override suspend fun deleteAll() = Unit
        }
    }
