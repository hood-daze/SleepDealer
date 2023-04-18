/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hood.sleepdealer.sleepdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hood.sleepdealer.R
import com.hood.sleepdealer.SleepDealerDestinationsArgs
import com.hood.sleepdealer.data.Sleep
import com.hood.sleepdealer.data.SleepRepository
import com.hood.sleepdealer.util.Async
import com.hood.sleepdealer.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the Details screen.
 */
data class SleepDetailUiState(
    val sleep: Sleep? = null,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isSleepDeleted: Boolean = false
)

/**
 * ViewModel for the Details screen.
 */
@HiltViewModel
class SleepDetailViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sleepId: String = savedStateHandle[SleepDealerDestinationsArgs.SLEEP_ID_ARG]!!

    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isTaskDeleted = MutableStateFlow(false)
    private val _taskAsync = sleepRepository.getTaskStream(sleepId)
        .map { handleTask(it) }
        .catch { emit(Async.Error(R.string.loading_task_error)) }

    val uiState: StateFlow<SleepDetailUiState> = combine(
        _userMessage, _isLoading, _isTaskDeleted, _taskAsync
    ) { userMessage, isLoading, isSleepDeleted, taskAsync ->
        when (taskAsync) {
            Async.Loading -> {
                SleepDetailUiState(isLoading = true)
            }
            is Async.Error -> {
                SleepDetailUiState(
                    userMessage = taskAsync.errorMessage,
                    isSleepDeleted = isSleepDeleted
                )
            }
            is Async.Success -> {
                SleepDetailUiState(
                    sleep = taskAsync.data,
                    isLoading = isLoading,
                    userMessage = userMessage,
                    isSleepDeleted = isSleepDeleted
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = SleepDetailUiState(isLoading = true)
        )

    fun deleteTask() = viewModelScope.launch {
        sleepRepository.deleteTask(sleepId)
        _isTaskDeleted.value = true
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            sleepRepository.refreshTask(sleepId)
            _isLoading.value = false
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    private fun handleTask(sleep: Sleep?): Async<Sleep?> {
        if (sleep == null) {
            return Async.Error(R.string.task_not_found)
        }
        return Async.Success(sleep)
    }
}
