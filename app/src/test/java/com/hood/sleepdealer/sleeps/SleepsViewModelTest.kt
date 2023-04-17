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

package com.hood.sleepdealer.sleeps

import androidx.lifecycle.SavedStateHandle
import com.hood.sleepdealer.ADD_EDIT_RESULT_OK
import com.hood.sleepdealer.DELETE_RESULT_OK
import com.hood.sleepdealer.EDIT_RESULT_OK
import com.hood.sleepdealer.MainCoroutineRule
import com.hood.sleepdealer.R
import com.hood.sleepdealer.data.FakeSleepRepository
import com.hood.sleepdealer.data.Sleep
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [SleepsViewModel]
 */
@ExperimentalCoroutinesApi
class SleepsViewModelTest {

    // Subject under test
    private lateinit var sleepsViewModel: SleepsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeSleepRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        tasksRepository = FakeSleepRepository()
        val sleep1 = Sleep(id = "1", title = "Title1", description = "Desc1")
        val sleep2 = Sleep(id = "2", title = "Title2", description = "Desc2", isCompleted = true)
        val sleep3 = Sleep(id = "3", title = "Title3", description = "Desc3", isCompleted = true)
        tasksRepository.addTasks(sleep1, sleep2, sleep3)

        sleepsViewModel = SleepsViewModel(tasksRepository, SavedStateHandle())
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        // Given an initialized SleepsViewModel with initialized tasks
        // When loading of Tasks is requested
        sleepsViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger loading of tasks
        sleepsViewModel.refresh()

        // Then progress indicator is shown
        assertThat(sleepsViewModel.uiState.first().isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(sleepsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(sleepsViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized SleepsViewModel with initialized tasks
        // When loading of Tasks is requested
        sleepsViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)

        // Load tasks
        sleepsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(sleepsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(sleepsViewModel.uiState.first().items).hasSize(1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() = runTest {
        // Given an initialized SleepsViewModel with initialized tasks
        // When loading of Tasks is requested
        sleepsViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

        // Load tasks
        sleepsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(sleepsViewModel.uiState.first().isLoading).isFalse()

        // And data correctly loaded
        assertThat(sleepsViewModel.uiState.first().items).hasSize(2)
    }

    @Test
    fun loadTasks_error() = runTest {
        // Make the repository throw errors
        tasksRepository.setShouldThrowError(true)

        // Load tasks
        sleepsViewModel.refresh()

        // Then progress indicator is hidden
        assertThat(sleepsViewModel.uiState.first().isLoading).isFalse()

        // And the list of items is empty
        assertThat(sleepsViewModel.uiState.first().items).isEmpty()
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_tasks_error)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = runTest {
        // When completed tasks are cleared
        sleepsViewModel.clearCompletedTasks()

        // Fetch tasks
        sleepsViewModel.refresh()

        // Fetch tasks
        val allTasks = sleepsViewModel.uiState.first().items
        val completedTasks = allTasks?.filter { it.isCompleted }

        // Verify there are no completed tasks left
        assertThat(completedTasks).isEmpty()

        // Verify active sleep is not cleared
        assertThat(allTasks).hasSize(1)

        // Verify snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.completed_tasks_cleared)
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        sleepsViewModel.showEditResultMessage(EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_saved_task_message)
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        sleepsViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // The snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_added_task_message)
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() = runTest {
        // When the viewmodel receives a result from another destination
        sleepsViewModel.showEditResultMessage(DELETE_RESULT_OK)

        // The snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.successfully_deleted_task_message)
    }

    @Test
    fun completeTask_dataAndSnackbarUpdated() = runTest {
        // With a repository that has an active sleep
        val sleep = Sleep(id = "id", title = "Title", description = "Description")
        tasksRepository.addTasks(sleep)

        // Complete sleep
        sleepsViewModel.completeTask(sleep, true)

        // Verify the sleep is completed
        assertThat(tasksRepository.savedTasks.value[sleep.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_complete)
    }

    @Test
    fun activateTask_dataAndSnackbarUpdated() = runTest {
        // With a repository that has a completed sleep
        val sleep = Sleep(id = "id", title = "Title", description = "Description", isCompleted = true)
        tasksRepository.addTasks(sleep)

        // Activate sleep
        sleepsViewModel.completeTask(sleep, false)

        // Verify the sleep is active
        assertThat(tasksRepository.savedTasks.value[sleep.id]?.isActive).isTrue()

        // The snackbar is updated
        assertThat(sleepsViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_active)
    }
}
