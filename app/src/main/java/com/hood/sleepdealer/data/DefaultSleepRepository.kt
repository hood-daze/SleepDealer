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

import com.hood.sleepdealer.data.source.local.SleepDao
import com.hood.sleepdealer.data.source.network.NetworkDataSource
import com.hood.sleepdealer.di.ApplicationScope
import com.hood.sleepdealer.di.DefaultDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Default implementation of [SleepRepository]. Single entry point for managing tasks' data.
 *
 * @param networkDataSource - The network data source
 * @param localDataSource - The local data source
 * @param dispatcher - The dispatcher to be used for long running or complex operations, such as ID
 * generation or mapping many models.
 * @param scope - The coroutine scope used for deferred jobs where the result isn't important, such
 * as sending data to the network.
 */
@Singleton
class DefaultSleepRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: SleepDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
) : SleepRepository {

    override suspend fun createTask(title: String, description: String): String {
        // ID creation might be a complex operation so it's executed using the supplied
        // coroutine dispatcher
        val taskId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val sleep = Sleep(
            title = title,
            description = description,
            id = taskId,
        )
        localDataSource.upsert(sleep.toLocal())
        saveTasksToNetwork()
        return taskId
    }

    override suspend fun updateSleep(taskId: String, title: String, description: String) {
        val sleep = getSleep(taskId)?.copy(
            title = title,
            description = description
        ) ?: throw Exception("Sleep (id $taskId) not found")

        localDataSource.upsert(sleep.toLocal())
        saveTasksToNetwork()
    }

    override suspend fun getTasks(forceUpdate: Boolean): List<Sleep> {
        if (forceUpdate) {
            refresh()
        }
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override fun getSleepsStream(): Flow<List<Sleep>> {
        return localDataSource.observeAll().map { tasks ->
            withContext(dispatcher) {
                tasks.toExternal()
            }
        }
    }

    override suspend fun refreshTask(taskId: String) {
        refresh()
    }

    override fun getTaskStream(taskId: String): Flow<Sleep?> {
        return localDataSource.observeById(taskId).map { it.toExternal() }
    }

    /**
     * Get a Sleep with the given ID. Will return null if the sleep cannot be found.
     *
     * @param taskId - The ID of the sleep
     * @param forceUpdate - true if the sleep should be updated from the network data source first.
     */
    override suspend fun getSleep(taskId: String, forceUpdate: Boolean): Sleep? {
        if (forceUpdate) {
            refresh()
        }
        return localDataSource.getById(taskId)?.toExternal()
    }

    override suspend fun deleteAllTasks() {
        localDataSource.deleteAll()
        saveTasksToNetwork()
    }

    override suspend fun deleteTask(taskId: String) {
        localDataSource.deleteById(taskId)
        saveTasksToNetwork()
    }

    /**
     * The following methods load tasks from (refresh), and save tasks to, the network.
     *
     * Real apps may want to do a proper sync, rather than the "one-way sync everything" approach
     * below. See https://developer.android.com/topic/architecture/data-layer/offline-first
     * for more efficient and robust synchronisation strategies.
     *
     * Note that the refresh operation is a suspend function (forces callers to wait) and the save
     * operation is not. It returns immediately so callers don't have to wait.
     */

    /**
     * Delete everything in the local data source and replace it with everything from the network
     * data source.
     *
     * `withContext` is used here in case the bulk `toLocal` mapping operation is complex.
     */
    override suspend fun refresh() {
        withContext(dispatcher) {
            val remoteTasks = networkDataSource.loadSleeps()
            localDataSource.deleteAll()
            localDataSource.upsertAll(remoteTasks.toLocal())
        }
    }

    /**
     * Send the tasks from the local data source to the network data source
     *
     * Returns immediately after launching the job. Real apps may want to suspend here until the
     * operation is complete or (better) use WorkManager to schedule this work. Both approaches
     * should provide a mechanism for failures to be communicated back to the user so that
     * they are aware that their data isn't being backed up.
     */
    private fun saveTasksToNetwork() {
        scope.launch {
            try {
                val localTasks = localDataSource.getAll()
                val networkTasks = withContext(dispatcher) {
                    localTasks.toNetwork()
                }
                networkDataSource.saveSleeps(networkTasks)
            } catch (e: Exception) {
                // In a real app you'd handle the exception e.g. by exposing a `networkStatus` flow
                // to an app level UI state holder which could then display a Toast message.
            }
        }
    }
}
