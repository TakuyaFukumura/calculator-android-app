package com.takuyafukumura.calculator.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takuyafukumura.calculator.calculator.CalculatorAction
import com.takuyafukumura.calculator.calculator.CalculatorMode
import com.takuyafukumura.calculator.calculator.CalculatorUiState
import com.takuyafukumura.calculator.calculator.CalculatorViewModel
import com.takuyafukumura.calculator.calculator.Operator

@Suppress("FunctionNaming")
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CalculatorContent(modifier = modifier, state = state, onAction = viewModel::onAction)
}

@Suppress("FunctionNaming")
@Composable
private fun CalculatorContent(
    modifier: Modifier,
    state: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().navigationBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = state.expression,
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text =
                    if (state.mode == CalculatorMode.ERROR) {
                        state.errorMessage.orEmpty()
                    } else {
                        state.displayValue
                    },
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
                color =
                    if (state.mode == CalculatorMode.ERROR) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
        }
        Keypad(onAction = onAction)
    }
}

@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun Keypad(onAction: (CalculatorAction) -> Unit) {
    val rows =
        listOf(
            listOf(
                Key("AC") {
                    CalculatorAction.AllClearPressed
                },
                Key("DEL") {
                    CalculatorAction.DeletePressed
                },
                Key("%") { CalculatorAction.PercentPressed },
                Key("÷") { CalculatorAction.OperatorPressed(Operator.DIVIDE) },
            ),
            listOf(
                Key("7") {
                    CalculatorAction.DigitPressed(7)
                },
                Key("8") {
                    CalculatorAction.DigitPressed(8)
                },
                Key("9") { CalculatorAction.DigitPressed(9) },
                Key("×") { CalculatorAction.OperatorPressed(Operator.MULTIPLY) },
            ),
            listOf(
                Key("4") {
                    CalculatorAction.DigitPressed(4)
                },
                Key("5") {
                    CalculatorAction.DigitPressed(5)
                },
                Key("6") { CalculatorAction.DigitPressed(6) },
                Key("−") { CalculatorAction.OperatorPressed(Operator.SUBTRACT) },
            ),
            listOf(
                Key("1") {
                    CalculatorAction.DigitPressed(1)
                },
                Key("2") {
                    CalculatorAction.DigitPressed(2)
                },
                Key("3") { CalculatorAction.DigitPressed(3) },
                Key("+") { CalculatorAction.OperatorPressed(Operator.ADD) },
            ),
            listOf(
                Key("±") {
                    CalculatorAction.SignTogglePressed
                },
                Key("0") {
                    CalculatorAction.DigitPressed(0)
                },
                Key(".") { CalculatorAction.DecimalPressed },
                Key("=") { CalculatorAction.EqualsPressed },
            ),
        )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    CalculatorKey(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = { onAction(key.action()) },
                    )
                }
            }
        }
    }
}

private data class Key(
    val label: String,
    val action: () -> CalculatorAction,
)

@Suppress("FunctionNaming")
@Composable
private fun CalculatorKey(
    key: Key,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val buttonModifier =
        modifier.height(56.dp).semantics {
            contentDescription = key.label
        }
    val isOperator = key.label in setOf("÷", "×", "−", "+", "=")
    if (isOperator) {
        Button(onClick = onClick, modifier = buttonModifier) {
            Text(key.label, fontSize = 22.sp)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier) {
            Text(key.label, fontSize = 20.sp)
        }
    }
}
