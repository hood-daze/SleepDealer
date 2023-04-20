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

package com.hood.sleepdealer.profile

import com.hood.sleepdealer.MainCoroutineRule
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [ProfileViewModel]
 */
@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    // Subject under test
    private lateinit var profileViewModel: ProfileViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeSleepRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        tasksRepository = FakeSleepRepository()
        profileViewModel = ProfileViewModel(tasksRepository)
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() = runTest {
        // Given an initialized ProfileViewModel with no tasks

        // Then the results are empty
        val uiState = profileViewModel.uiState.first()
        assertThat(uiState.isEmpty).isTrue()
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() = runTest {
        // We initialise the tasks to 3, with one active and two completed
        val sleep1 = Sleep(id = "1", title = "Title1", score = "Desc1")
        val sleep2 = Sleep(id = "2", title = "Title2", score = "Desc2", isCompleted = true)
        val sleep3 = Sleep(id = "3", title = "Title3", score = "Desc3", isCompleted = true)
        val sleep4 = Sleep(id = "4", title = "Title4", score = "Desc4", isCompleted = true)
        tasksRepository.addTasks(sleep1, sleep2, sleep3, sleep4)

        // Then the results are not empty
        val uiState = profileViewModel.uiState.first()
        assertThat(uiState.isEmpty).isFalse()
        assertThat(uiState.activeTasksPercent).isEqualTo(25f)
        assertThat(uiState.completedTasksPercent).isEqualTo(75f)
        assertThat(uiState.isLoading).isEqualTo(false)
    }

    @Test
    fun loadTasks_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            profileViewModel.uiState.collect {
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
