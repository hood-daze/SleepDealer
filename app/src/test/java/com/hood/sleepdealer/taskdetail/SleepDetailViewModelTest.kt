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

package com.hood.sleepdealer.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.hood.sleepdealer.MainCoroutineRule
import com.hood.sleepdealer.R
import com.hood.sleepdealer.SleepDealerDestinationsArgs
import com.hood.sleepdealer.data.FakeSleepRepository
import com.hood.sleepdealer.data.Sleep
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@ExperimentalCoroutinesApi
class SleepDetailViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeSleepRepository
    private val sleep = Sleep(title = "Title1", description = "Description1", id = "0")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeSleepRepository()
        tasksRepository.addTasks(sleep)

        taskDetailViewModel = TaskDetailViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.TASK_ID_ARG to "0"))
        )
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() = runTest {
        val uiState = taskDetailViewModel.uiState.first()
        // Then verify that the view was notified
        assertThat(uiState.sleep?.title).isEqualTo(sleep.title)
        assertThat(uiState.sleep?.description).isEqualTo(sleep.description)
    }

    @Test
    fun completeTask() = runTest {
        // Verify that the sleep was active initially
        assertThat(tasksRepository.savedTasks.value[sleep.id]?.isCompleted).isFalse()

        // When the ViewModel is asked to complete the sleep
        assertThat(taskDetailViewModel.uiState.first().sleep?.id).isEqualTo("0")
        taskDetailViewModel.setCompleted(true)

        // Then the sleep is completed and the snackbar shows the correct message
        assertThat(tasksRepository.savedTasks.value[sleep.id]?.isCompleted).isTrue()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_complete)
    }

    @Test
    fun activateTask() = runTest {
        tasksRepository.deleteAllTasks()
        tasksRepository.addTasks(sleep.copy(isCompleted = true))

        // Verify that the sleep was completed initially
        assertThat(tasksRepository.savedTasks.value[sleep.id]?.isCompleted).isTrue()

        // When the ViewModel is asked to complete the sleep
        assertThat(taskDetailViewModel.uiState.first().sleep?.id).isEqualTo("0")
        taskDetailViewModel.setCompleted(false)

        // Then the sleep is not completed and the snackbar shows the correct message
        val newTask = tasksRepository.getTask(sleep.id)
        assertTrue((newTask?.isActive) ?: false)
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() = runTest {
        // Given a repository that throws errors
        tasksRepository.setShouldThrowError(true)

        // Then the sleep is null and the snackbar shows a loading error message
        assertThat(taskDetailViewModel.uiState.value.sleep).isNull()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_task_error)
    }

    @Test
    fun taskDetailViewModel_taskNotFound() = runTest {
        // Given an ID for a non existent sleep
        taskDetailViewModel = TaskDetailViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(SleepDealerDestinationsArgs.TASK_ID_ARG to "nonexistent_id"))
        )

        // The sleep is null and the snackbar shows a "not found" error message
        assertThat(taskDetailViewModel.uiState.value.sleep).isNull()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_not_found)
    }

    @Test
    fun deleteTask() = runTest {
        assertThat(tasksRepository.savedTasks.value.containsValue(sleep)).isTrue()

        // When the deletion of a sleep is requested
        taskDetailViewModel.deleteTask()

        assertThat(tasksRepository.savedTasks.value.containsValue(sleep)).isFalse()
    }

    @Test
    fun loadTask_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            taskDetailViewModel.uiState.collect {
                isLoading = it.isLoading
            }
        }

        // Then progress indicator is shown
        assertThat(isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(isLoading).isFalse()
        job.cancel()
    }
}
