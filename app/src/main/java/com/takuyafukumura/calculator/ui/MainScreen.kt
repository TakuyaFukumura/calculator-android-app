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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import com.takuyafukumura.calculator.ui.viewmodel.HistoryViewModel

@Suppress("FunctionNaming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
    var showHistory by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    var restoreError by remember { mutableStateOf<String?>(null) }

    CalculatorContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::onAction,
        onHistoryClick = {
            restoreError = null
            showHistory = true
        },
    )

    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }) {
            HistorySheet(
                entries = historyState.entries,
                errorMessage = historyState.errorMessage ?: state.historyErrorMessage ?: restoreError,
                onRetry = historyViewModel::retry,
                onDelete = historyViewModel::delete,
                onDeleteAll = { showDeleteAllConfirmation = true },
                onSelect = { entry ->
                    if (viewModel.restoreExpression(entry.expression)) {
                        showHistory = false
                    } else {
                        restoreError = "この履歴は復元できません"
                    }
                },
            )
        }
    }

    if (showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            title = { Text("計算履歴をすべて削除しますか？") },
            text = { Text("この操作は元に戻せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllConfirmation = false
                        historyViewModel.deleteAll()
                    },
                ) {
                    Text("すべて削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirmation = false }) {
                    Text("キャンセル")
                }
            },
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun CalculatorContent(
    modifier: Modifier,
    state: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit,
    onHistoryClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onHistoryClick,
                modifier =
                    Modifier.semantics {
                        contentDescription = "計算履歴を表示"
                    },
            ) {
                Icon(Icons.Default.History, contentDescription = null)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
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
        Keypad(
            onAction = onAction,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun HistorySheet(
    entries: List<CalculationHistoryEntity>,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDelete: (Long) -> Unit,
    onDeleteAll: () -> Unit,
    onSelect: (CalculationHistoryEntity) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("計算履歴", style = MaterialTheme.typography.headlineSmall)
            if (entries.isNotEmpty()) {
                TextButton(onClick = onDeleteAll) {
                    Text("すべて削除")
                }
            }
        }
        if (errorMessage != null) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRetry) {
                        Text("再試行")
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            Text(
                text = "計算履歴はありません",
                modifier = Modifier.padding(vertical = 32.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { entry ->
                    HistoryItem(entry = entry, onDelete = { onDelete(entry.id) }, onSelect = { onSelect(entry) })
                }
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun HistoryItem(
    entry: CalculationHistoryEntity,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
) {
    Card(
        onClick = onSelect,
        modifier =
            Modifier.semantics {
                contentDescription = "履歴 ${entry.expression} ${entry.result}"
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.expression, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "= ${entry.result}",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            IconButton(
                onClick = onDelete,
                modifier =
                    Modifier.semantics {
                        contentDescription = "履歴を削除"
                    },
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

@Suppress("FunctionNaming", "LongMethod")
@Composable
private fun Keypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
