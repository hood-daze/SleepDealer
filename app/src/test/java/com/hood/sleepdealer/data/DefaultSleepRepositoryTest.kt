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

package com.hood.sleepdealer.data

import com.hood.sleepdealer.data.source.local.FakeTaskDao
import com.hood.sleepdealer.data.source.network.FakeNetworkDataSource
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultSleepRepositoryTest {

    private val sleep1 = Sleep(id = "1", title = "Title1", description = "Description1")
    private val sleep2 = Sleep(id = "2", title = "Title2", description = "Description2")
    private val sleep3 = Sleep(id = "3", title = "Title3", description = "Description3")

    private val newTaskTitle = "Title new"
    private val newTaskDescription = "Description new"
    private val newSleep = Sleep(id = "new", title = newTaskTitle, description = newTaskDescription)
    private val newTasks = listOf(newSleep)

    private val networkTasks = listOf(sleep1, sleep2).toNetwork()
    private val localTasks = listOf(sleep3.toLocal())

    // Test dependencies
    private lateinit var networkDataSource: FakeNetworkDataSource
    private lateinit var localDataSource: FakeTaskDao

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    // Class under test
    private lateinit var taskRepository: DefaultSleepRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        networkDataSource = FakeNetworkDataSource(networkTasks.toMutableList())
        localDataSource = FakeTaskDao(localTasks)
        // Get a reference to the class under test
        taskRepository = DefaultSleepRepository(
            networkDataSource = networkDataSource,
            localDataSource = localDataSource,
            dispatcher = testDispatcher,
            scope = testScope
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = testScope.runTest {
        networkDataSource.tasks?.clear()
        localDataSource.deleteAll()

        assertThat(taskRepository.getTasks().size).isEqualTo(0)
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Trigger the repository to load tasks from the remote data source
        val initial = taskRepository.getTasks(forceUpdate = true)

        // Change the remote data source
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // Load the tasks again without forcing a refresh
        val second = taskRepository.getTasks()

        // Initial and second should match because we didn't force a refresh (no tasks were loaded
        // from the remote data source)
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = testScope.runTest {
        // When tasks are requested from the tasks repository
        val tasks = taskRepository.getTasks(true)

        // Then tasks are loaded from the remote data source
        assertThat(tasks).isEqualTo(networkTasks.toExternal())
    }

    @Test
    fun saveTask_savesToLocalAndRemote() = testScope.runTest {
        // When a sleep is saved to the tasks repository
        val newTaskId = taskRepository.createTask(newSleep.title, newSleep.description)

        // Then the remote and local sources contain the new sleep
        assertThat(networkDataSource.tasks?.map { it.id }?.contains(newTaskId))
        assertThat(localDataSource.tasks?.map { it.id }?.contains(newTaskId))
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = testScope.runTest {
        // First call returns from REMOTE
        val tasks = taskRepository.getTasks()

        // Set a different list of tasks in REMOTE
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = taskRepository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = taskRepository.getTasks(true)

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks).isEqualTo(newTasks)
    }

    @Test(expected = Exception::class)
    fun getTasks_WithDirtyCache_remoteUnavailable_throwsException() = testScope.runTest {
        // Make remote data source unavailable
        networkDataSource.tasks = null

        // Load tasks forcing remote load
        taskRepository.getTasks(true)

        // Exception should be thrown
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() =
        testScope.runTest {
            // When the remote data source is unavailable
            networkDataSource.tasks = null

            // The repository fetches from the local source
            assertThat(taskRepository.getTasks()).isEqualTo(localTasks.toExternal())
        }

    @Test(expected = Exception::class)
    fun getTasks_WithBothDataSourcesUnavailable_throwsError() = testScope.runTest {
        // When both sources are unavailable
        networkDataSource.tasks = null
        localDataSource.tasks = null

        // The repository throws an error
        taskRepository.getTasks()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = testScope.runTest {
        // Forcing an update will fetch tasks from remote
        val expectedTasks = networkTasks.toExternal()

        val newTasks = taskRepository.getTasks(true)

        assertEquals(expectedTasks, newTasks)
        assertEquals(expectedTasks, localDataSource.tasks?.toExternal())
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a sleep
        val newTaskId = taskRepository.createTask(newSleep.title, newSleep.description)

        // Make sure it's active
        assertThat(taskRepository.getTask(newTaskId)?.isCompleted).isFalse()

        // Mark is as complete
        taskRepository.completeTask(newTaskId)

        // Verify it's now completed
        assertThat(taskRepository.getTask(newTaskId)?.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = testScope.runTest {
        // Save a sleep
        val newTaskId = taskRepository.createTask(newSleep.title, newSleep.description)
        taskRepository.completeTask(newTaskId)

        // Make sure it's completed
        assertThat(taskRepository.getTask(newTaskId)?.isActive).isFalse()

        // Mark is as active
        taskRepository.activateTask(newTaskId)

        // Verify it's now activated
        assertThat(taskRepository.getTask(newTaskId)?.isActive).isTrue()
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = testScope.runTest {
        // Obtain a sleep from the local data source
        localDataSource = FakeTaskDao(mutableListOf(sleep1.toLocal()))
        val initial = taskRepository.getTask(sleep1.id)

        // Change the tasks on the remote
        networkDataSource.tasks = newTasks.toNetwork().toMutableList()

        // Obtain the same sleep again
        val second = taskRepository.getTask(sleep1.id)

        // Initial and second tasks should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTask_forceRefresh() = testScope.runTest {
        // Trigger the repository to load data, which loads from remote and caches
        networkDataSource.tasks = mutableListOf(sleep1.toNetwork())
        val task1FirstTime = taskRepository.getTask(sleep1.id, forceUpdate = true)
        assertThat(task1FirstTime?.id).isEqualTo(sleep1.id)

        // Configure the remote data source to return a different sleep
        networkDataSource.tasks = mutableListOf(sleep2.toNetwork())

        // Force refresh
        val task1SecondTime = taskRepository.getTask(sleep1.id, true)
        val task2SecondTime = taskRepository.getTask(sleep2.id, true)

        // Only sleep2 works because sleep1 does not exist on the remote
        assertThat(task1SecondTime).isNull()
        assertThat(task2SecondTime?.id).isEqualTo(sleep2.id)
    }

    @Test
    fun clearCompletedTasks() = testScope.runTest {
        val completedTask = sleep1.copy(isCompleted = true)
        localDataSource.tasks = listOf(completedTask.toLocal(), sleep2.toLocal())
        taskRepository.clearCompletedTasks()

        val tasks = taskRepository.getTasks(true)

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(sleep2)
        assertThat(tasks).doesNotContain(completedTask)
    }

    @Test
    fun deleteAllTasks() = testScope.runTest {
        val initialTasks = taskRepository.getTasks()

        // Verify tasks are returned
        assertThat(initialTasks.size).isEqualTo(1)

        // Delete all tasks
        taskRepository.deleteAllTasks()

        // Verify tasks are empty now
        val afterDeleteTasks = taskRepository.getTasks()
        assertThat(afterDeleteTasks).isEmpty()
    }

    @Test
    fun deleteSingleTask() = testScope.runTest {
        val initialTasksSize = taskRepository.getTasks(true).size

        // Delete first sleep
        taskRepository.deleteTask(sleep1.id)

        // Fetch data again
        val afterDeleteTasks = taskRepository.getTasks(true)

        // Verify only one sleep was deleted
        assertThat(afterDeleteTasks.size).isEqualTo(initialTasksSize - 1)
        assertThat(afterDeleteTasks).doesNotContain(sleep1)
    }
}
