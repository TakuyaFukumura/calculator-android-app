package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.entity.StringEntity
import com.example.myapplication.data.repository.StringRepository
import com.example.myapplication.di.ErrorLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MainError {
    LOAD_FAILED,
    OPERATION_FAILED,
    INPUT_BLANK,
}

data class MainUiState(
    val greeting: String = "world",
    val strings: List<StringEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isOperationInProgress: Boolean = false,
    val operationMessage: String? = null,
    val error: MainError? = null,
)

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val repository: StringRepository,
        private val errorLogger: ErrorLogger,
    ) : ViewModel() {
        private companion object {
            const val TAG = "MainViewModel"
            const val MAX_VALUE_LENGTH = 100
        }

        private val _uiState = MutableStateFlow(MainUiState())
        val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

        init {
            observeStrings()
        }

        private fun observeStrings() {
            viewModelScope.launch {
                try {
                    repository.getAllStrings().collect { strings ->
                        _uiState.value =
                            _uiState.value.copy(
                                strings = strings,
                                greeting = strings.firstOrNull()?.value ?: "world",
                                isLoading = false,
                                error = _uiState.value.error.takeUnless { it == MainError.LOAD_FAILED },
                            )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    errorLogger.error(TAG, "Failed to observe strings", exception)
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = MainError.LOAD_FAILED,
                        )
                }
            }
        }

        fun addString(value: String) {
            val normalizedValue = value.trim().take(MAX_VALUE_LENGTH)
            if (normalizedValue.isEmpty()) {
                setInputError()
                return
            }
            runOperation {
                repository.insertString(StringEntity(value = normalizedValue))
                "文字列「$normalizedValue」を追加しました"
            }
        }

        fun updateString(
            id: Int,
            newValue: String,
        ) {
            val normalizedValue = newValue.trim().take(MAX_VALUE_LENGTH)
            if (normalizedValue.isEmpty()) {
                setInputError()
                return
            }
            runOperation {
                repository.updateString(StringEntity(id = id, value = normalizedValue))
                "文字列を「$normalizedValue」に更新しました"
            }
        }

        fun deleteString(id: Int) {
            runOperation {
                if (repository.deleteStringById(id) > 0) {
                    "文字列を削除しました"
                } else {
                    "削除対象の文字列が見つかりませんでした"
                }
            }
        }

        fun deleteAllStrings() {
            runOperation {
                repository.deleteAllStrings()
                "すべての文字列を削除しました"
            }
        }

        fun clearOperationStatus() {
            _uiState.value = _uiState.value.copy(operationMessage = null, error = null)
        }

        fun retryLoading() {
            if (_uiState.value.isLoading) return
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            observeStrings()
        }

        private fun setInputError() {
            _uiState.value =
                _uiState.value.copy(
                    operationMessage = null,
                    error = MainError.INPUT_BLANK,
                )
        }

        private fun runOperation(operation: suspend () -> String) {
            if (_uiState.value.isOperationInProgress) return
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        isOperationInProgress = true,
                        operationMessage = null,
                        error = null,
                    )
                try {
                    val message = operation()
                    _uiState.value =
                        _uiState.value.copy(
                            isOperationInProgress = false,
                            operationMessage = message,
                        )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    errorLogger.error(TAG, "Database operation failed", exception)
                    _uiState.value =
                        _uiState.value.copy(
                            isOperationInProgress = false,
                            error = MainError.OPERATION_FAILED,
                        )
                }
            }
        }
    }
