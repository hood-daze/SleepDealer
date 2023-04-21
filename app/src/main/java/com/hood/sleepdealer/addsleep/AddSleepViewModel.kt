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

package com.hood.sleepdealer.addsleep

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hood.sleepdealer.R
import com.hood.sleepdealer.SleepDealerDestinationsArgs
import com.hood.sleepdealer.data.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UiState for the Add/Edit screen
 */
data class AddSleepUiState(
    val score: Int = -1,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isSleepSaved: Boolean = false
)

/**
 * ViewModel for the Add/Edit screen.
 */
@HiltViewModel
class AddSleepViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sleepId: String? = savedStateHandle[SleepDealerDestinationsArgs.SLEEP_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable Sleep is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddSleepUiState())
    val uiState: StateFlow<AddSleepUiState> = _uiState.asStateFlow()

    // Called when clicking on fab.
    fun saveTask() {
        if (uiState.value.score == -1) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_task_message)
            }
            return
        }

        if (sleepId == null) {
            createSleep()
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }


    fun updateScore(newScore: Int) {
        _uiState.update {
            it.copy(score = newScore)
        }
    }

    private fun createSleep() = viewModelScope.launch {
        sleepRepository.createSleep(uiState.value.score)
        _uiState.update {
            it.copy(isSleepSaved = true)
        }
    }

}
