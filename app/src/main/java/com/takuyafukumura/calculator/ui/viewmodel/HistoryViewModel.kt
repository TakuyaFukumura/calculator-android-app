package com.takuyafukumura.calculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takuyafukumura.calculator.data.entity.CalculationHistoryEntity
import com.takuyafukumura.calculator.data.repository.CalculationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val entries: List<CalculationHistoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val repository: CalculationHistoryRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HistoryUiState())
        val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

        init {
            observeHistory()
        }

        fun retry() {
            observeHistory()
        }

        fun delete(id: Long) {
            viewModelScope.launch {
                runCatching { repository.delete(id) }
                    .onFailure { setError("計算履歴の削除に失敗しました") }
            }
        }

        fun deleteAll() {
            viewModelScope.launch {
                runCatching { repository.deleteAll() }
                    .onFailure { setError("計算履歴の削除に失敗しました") }
            }
        }

        fun reportError(message: String) {
            setError(message)
        }

        private fun observeHistory() {
            viewModelScope.launch {
                repository
                    .observeHistory()
                    .catch { setError("計算履歴の読み込みに失敗しました") }
                    .collect { entries ->
                        _uiState.value =
                            _uiState.value.copy(
                                entries = entries,
                                isLoading = false,
                                errorMessage = null,
                            )
                    }
            }
        }

        private fun setError(message: String) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
        }
    }
