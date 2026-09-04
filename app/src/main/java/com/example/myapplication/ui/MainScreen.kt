package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.entity.StringEntity
import com.example.myapplication.ui.components.AddStringForm
import com.example.myapplication.ui.components.DeleteAllConfirmationDialog
import com.example.myapplication.ui.components.DeleteStringConfirmationDialog
import com.example.myapplication.ui.components.GreetingCard
import com.example.myapplication.ui.components.MainOperationStatus
import com.example.myapplication.ui.components.StringListSection
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.MainError
import com.example.myapplication.ui.viewmodel.MainViewModel

/**
 * メイン画面の状態を収集し、画面全体のComposableを組み立てます。
 *
 * 永続的な状態はViewModelから取得し、入力やダイアログの表示状態だけを画面内で管理します。
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operationStatus =
        uiState.operationMessage ?: when (uiState.error) {
            MainError.LOAD_FAILED -> stringResource(R.string.load_error_message)
            MainError.OPERATION_FAILED -> stringResource(R.string.operation_error_message)
            MainError.INPUT_BLANK -> stringResource(R.string.input_blank_error)
            null -> ""
        }

    var inputText by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Int?>(null) }
    var editText by remember { mutableStateOf("") }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<StringEntity?>(null) }

    LaunchedEffect(operationStatus.takeIf { it.isNotEmpty() }) {
        kotlinx.coroutines.delay(5000)
        viewModel.clearOperationStatus()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GreetingCard(name = uiState.greeting)

        MainOperationStatus(
            status = operationStatus,
            error = uiState.error,
            onRetry = viewModel::retryLoading,
        )

        AddStringForm(
            inputText = inputText,
            isOperationInProgress = uiState.isOperationInProgress,
            onInputTextChange = { inputText = it },
            onAdd = { value ->
                viewModel.addString(value)
                inputText = ""
            },
        )

        StringListSection(
            strings = uiState.strings,
            editingId = editingId,
            editText = editText,
            onDeleteAll = { showDeleteAllConfirmation = true },
            onEditStart = { id, currentValue ->
                editingId = id
                editText = currentValue
            },
            onEditSave = { id ->
                viewModel.updateString(id, editText)
                editingId = null
                editText = ""
            },
            onEditCancel = {
                editingId = null
                editText = ""
            },
            onEditTextChange = { editText = it },
            onDelete = { entity -> deleteTarget = entity },
        )

        if (showDeleteAllConfirmation) {
            DeleteAllConfirmationDialog(
                onDismiss = { showDeleteAllConfirmation = false },
                onConfirm = {
                    showDeleteAllConfirmation = false
                    viewModel.deleteAllStrings()
                },
            )
        }

        deleteTarget?.let { target ->
            DeleteStringConfirmationDialog(
                target = target,
                onDismiss = { deleteTarget = null },
                onConfirm = {
                    viewModel.deleteString(target.id)
                    deleteTarget = null
                },
            )
        }
    }
}

/**
 * 挨拶メッセージを表示するComposable関数。
 */
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Text(
        text = stringResource(R.string.greeting_format, name),
        modifier = modifier,
    )
}

/**
 * Greeting Composableのプレビュー用関数。
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("world")
    }
}
