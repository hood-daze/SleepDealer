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

package com.hood.sleepdealer.data.source.local

import kotlinx.coroutines.flow.Flow

class FakeSleepDao(initialTasks: List<LocalSleep>? = emptyList()) : SleepDao {

    private var _tasks: MutableMap<String, LocalSleep>? = null

    var tasks: List<LocalSleep>?
        get() = _tasks?.values?.toList()
        set(newTasks) {
            _tasks = newTasks?.associateBy { it.id }?.toMutableMap()
        }

    init {
        tasks = initialTasks
    }

    override suspend fun getAll() = tasks ?: throw Exception("Sleep list is null")

    override suspend fun getById(sleepId: String): LocalSleep? = _tasks?.get(sleepId)

    override suspend fun upsertAll(sleeps: List<LocalSleep>) {
        _tasks?.putAll(sleeps.associateBy { it.id })
    }

    override suspend fun upsert(sleep: LocalSleep) {
        _tasks?.put(sleep.id, sleep)
    }

    override suspend fun updateCompleted(taskId: String, completed: Boolean) {
        _tasks?.get(taskId)?.let { it.isCompleted = completed }
    }

    override suspend fun deleteAll() {
        _tasks?.clear()
    }

    override suspend fun deleteById(sleepId: String): Int {
        return if (_tasks?.remove(sleepId) == null) {
            0
        } else {
            1
        }
    }

    override suspend fun deleteCompleted(): Int {
        _tasks?.apply {
            val originalSize = size
            entries.removeIf { it.value.isCompleted }
            return originalSize - size
        }
        return 0
    }

    override fun observeAll(): Flow<List<LocalSleep>> {
        TODO("Not implemented")
    }

    override fun observeById(sleepId: String): Flow<LocalSleep> {
        TODO("Not implemented")
    }
}
