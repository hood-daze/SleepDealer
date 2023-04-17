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

import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Implementation of a tasks repository with static access to the data for easy testing.
 */
class FakeSleepRepository : SleepRepository {

    private var shouldThrowError = false

    private val _savedTasks = MutableStateFlow(LinkedHashMap<String, Sleep>())
    val savedTasks: StateFlow<LinkedHashMap<String, Sleep>> = _savedTasks.asStateFlow()

    private val observableTasks: Flow<List<Sleep>> = savedTasks.map {
        if (shouldThrowError) {
            throw Exception("Test exception")
        } else {
            it.values.toList()
        }
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override suspend fun refresh() {
        // Tasks already refreshed
    }

    override suspend fun refreshTask(taskId: String) {
        refresh()
    }

    override suspend fun createTask(title: String, description: String): String {
        val taskId = generateTaskId()
        Sleep(title = title, description = description, id = taskId).also {
            saveTask(it)
        }
        return taskId
    }

    override fun getSleepsStream(): Flow<List<Sleep>> = observableTasks

    override fun getTaskStream(taskId: String): Flow<Sleep?> {
        return observableTasks.map { tasks ->
            return@map tasks.firstOrNull { it.id == taskId }
        }
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Sleep? {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return savedTasks.value[taskId]
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Sleep> {
        if (shouldThrowError) {
            throw Exception("Test exception")
        }
        return observableTasks.first()
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val updatedSleep = _savedTasks.value[taskId]?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Sleep (id $taskId) not found")

        saveTask(updatedSleep)
    }

    private fun saveTask(sleep: Sleep) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Sleep>(tasks)
            newTasks[sleep.id] = sleep
            newTasks
        }
    }

    override suspend fun completeTask(taskId: String) {
        _savedTasks.value[taskId]?.let {
            saveTask(it.copy(isCompleted = true))
        }
    }

    override suspend fun activateTask(taskId: String) {
        _savedTasks.value[taskId]?.let {
            saveTask(it.copy(isCompleted = false))
        }
    }

    override suspend fun clearCompletedTasks() {
        _savedTasks.update { tasks ->
            tasks.filterValues {
                !it.isCompleted
            } as LinkedHashMap<String, Sleep>
        }
    }

    override suspend fun deleteTask(taskId: String) {
        _savedTasks.update { tasks ->
            val newTasks = LinkedHashMap<String, Sleep>(tasks)
            newTasks.remove(taskId)
            newTasks
        }
    }

    override suspend fun deleteAllTasks() {
        _savedTasks.update {
            LinkedHashMap()
        }
    }

    private fun generateTaskId() = UUID.randomUUID().toString()

    @VisibleForTesting
    fun addTasks(vararg sleeps: Sleep) {
        _savedTasks.update { oldTasks ->
            val newTasks = LinkedHashMap<String, Sleep>(oldTasks)
            for (task in sleeps) {
                newTasks[task.id] = task
            }
            newTasks
        }
    }
}
