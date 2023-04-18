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

package com.hood.sleepdealer.addedittask

import androidx.lifecycle.SavedStateHandle
import com.hood.sleepdealer.MainCoroutineRule
import com.hood.sleepdealer.R.string
import com.hood.sleepdealer.SleepDealerDestinationsArgs
import com.hood.sleepdealer.data.FakeSleepRepository
import com.hood.sleepdealer.data.Sleep
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [AddEditSleepViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditSleepViewModelTest {

    // Subject under test
    private lateinit var addEditSleepViewModel: AddEditSleepViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeSleepRepository
    private val sleep = Sleep(title = "Title1", description = "Description1", id = "0")

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeSleepRepository().apply {
            addTasks(sleep)
        }
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() {
        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        val newTitle = "New Sleep Title"
        val newDescription = "Some Sleep Description"
        addEditSleepViewModel.apply {
            updateTitle(newTitle)
            updateDescription(newDescription)
        }
        addEditSleepViewModel.saveTask()

        val newTask = tasksRepository.savedTasks.value.values.first()

        // Then a sleep is saved in the repository and the view updated
        assertThat(newTask.title).isEqualTo(newTitle)
        assertThat(newTask.description).isEqualTo(newDescription)
    }

    @Test
    fun loadTasks_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        // Then progress indicator is shown
        assertThat(addEditSleepViewModel.uiState.value.isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(addEditSleepViewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun loadTasks_taskShown() {
        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        // Add sleep to repository
        tasksRepository.addTasks(sleep)

        // Verify a sleep is loaded
        val uiState = addEditSleepViewModel.uiState.value
        assertThat(uiState.title).isEqualTo(sleep.title)
        assertThat(uiState.description).isEqualTo(sleep.description)
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun saveNewTaskToRepository_emptyTitle_error() {
        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        saveTaskAndAssertUserMessage("", "Some Sleep Description")
    }

    @Test
    fun saveNewTaskToRepository_emptyDescription_error() {
        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        saveTaskAndAssertUserMessage("Title", "")
    }

    @Test
    fun saveNewTaskToRepository_emptyDescriptionEmptyTitle_error() {
        addEditSleepViewModel = AddEditSleepViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.SLEEP_ID_ARG to "0"))
        )

        saveTaskAndAssertUserMessage("", "")
    }

    private fun saveTaskAndAssertUserMessage(title: String, description: String) {
        addEditSleepViewModel.apply {
            updateTitle(title)
            updateDescription(description)
        }

        // When saving an incomplete sleep
        addEditSleepViewModel.saveTask()

        assertThat(
            addEditSleepViewModel.uiState.value.userMessage
        ).isEqualTo(string.empty_task_message)
    }
}
