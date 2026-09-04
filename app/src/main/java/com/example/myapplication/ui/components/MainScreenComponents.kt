package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.entity.StringEntity
import com.example.myapplication.ui.viewmodel.MainError

@Composable
fun themeToggle(
    darkTheme: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val toggleDescription = stringResource(R.string.dark_mode_toggle_content_description)

    Row(
        modifier =
            Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = darkTheme,
            onCheckedChange = onToggle,
            modifier =
                Modifier
                    .testTag("darkModeToggle")
                    .semantics {
                        contentDescription = toggleDescription
                    },
        )
    }
}

@Composable
fun GreetingCard(name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.greeting_format, name),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
fun MainOperationStatus(
    status: String,
    error: MainError?,
    onRetry: () -> Unit,
) {
    if (status.isNotEmpty()) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = status,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (error != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                )
                if (error == MainError.LOAD_FAILED) {
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
fun AddStringForm(
    inputText: String,
    isOperationInProgress: Boolean,
    onInputTextChange: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    val addDescription = stringResource(R.string.add)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.add_string_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { onInputTextChange(it.take(100)) },
                    label = { Text(stringResource(R.string.string_input_label)) },
                    supportingText = {
                        Text(stringResource(R.string.character_count, inputText.length))
                    },
                    isError = inputText.isNotEmpty() && inputText.isBlank(),
                    singleLine = true,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag("stringInput"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                if (inputText.isNotBlank() && !isOperationInProgress) {
                                    onAdd(inputText)
                                }
                            },
                        ),
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onAdd(inputText)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isOperationInProgress,
                    modifier = Modifier.testTag("addButton"),
                ) {
                    Text(
                        text = stringResource(R.string.add),
                        modifier =
                            Modifier.semantics {
                                contentDescription = addDescription
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun StringListSection(
    strings: List<StringEntity>,
    editingId: Int?,
    editText: String,
    onDeleteAll: () -> Unit,
    onEditStart: (Int, String) -> Unit,
    onEditSave: (Int) -> Unit,
    onEditCancel: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onDelete: (StringEntity) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.string_list_title, strings.size),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (strings.isNotEmpty()) {
                    TextButton(
                        onClick = onDeleteAll,
                        modifier = Modifier.testTag("deleteAllButton"),
                    ) {
                        Text(stringResource(R.string.delete_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (strings.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_strings_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(strings, key = { it.id }) { stringEntity ->
                        StringListItem(
                            stringEntity = stringEntity,
                            editingId = editingId,
                            editText = editText,
                            onEditStart = onEditStart,
                            onEditSave = onEditSave,
                            onEditCancel = onEditCancel,
                            onEditTextChange = onEditTextChange,
                            onDelete = onDelete,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAllConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_all_confirmation_title)) },
        text = { Text(stringResource(R.string.delete_all_confirmation_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete_all))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
fun DeleteStringConfirmationDialog(
    target: StringEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_confirmation_title)) },
        text = { Text(stringResource(R.string.delete_confirmation_message, target.value)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * 文字列一覧の各アイテムを表示するComposable関数。
 */
@Composable
fun StringListItem(
    stringEntity: StringEntity,
    editingId: Int?,
    editText: String,
    onEditStart: (Int, String) -> Unit,
    onEditSave: (Int) -> Unit,
    onEditCancel: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onDelete: (StringEntity) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        val editDescription = stringResource(R.string.edit_content_description)
        val deleteDescription = stringResource(R.string.delete_content_description)
        if (editingId == stringEntity.id) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { onEditTextChange(it.take(100)) },
                    label = { Text(stringResource(R.string.edit_string_label)) },
                    supportingText = {
                        Text(stringResource(R.string.character_count, editText.length))
                    },
                    isError = editText.isBlank(),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("editInput"),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onEditSave(stringEntity.id) },
                        enabled = editText.isNotBlank(),
                        modifier = Modifier.testTag("saveButton"),
                    ) {
                        Text(stringResource(R.string.save))
                    }

                    OutlinedButton(
                        onClick = onEditCancel,
                        modifier = Modifier.testTag("cancelButton"),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        } else {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringEntity.value,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "ID: ${stringEntity.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { onEditStart(stringEntity.id, stringEntity.value) },
                        modifier = Modifier.testTag("editButton"),
                    ) {
                        Text(
                            text = stringResource(R.string.edit),
                            modifier =
                                Modifier.semantics {
                                    contentDescription = editDescription
                                },
                        )
                    }

                    TextButton(
                        onClick = { onDelete(stringEntity) },
                        modifier = Modifier.testTag("deleteButton"),
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            modifier =
                                Modifier.semantics {
                                    contentDescription = deleteDescription
                                },
                        )
                    }
                }
            }
        }
    }
}
