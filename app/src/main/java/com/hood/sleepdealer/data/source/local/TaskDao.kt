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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the sleep table.
 */
@Dao
interface TaskDao {

    /**
     * Observes list of tasks.
     *
     * @return all tasks.
     */
    @Query("SELECT * FROM sleep")
    fun observeAll(): Flow<List<LocalTask>>

    /**
     * Observes a single sleep.
     *
     * @param taskId the sleep id.
     * @return the sleep with taskId.
     */
    @Query("SELECT * FROM sleep WHERE id = :taskId")
    fun observeById(taskId: String): Flow<LocalTask>

    /**
     * Select all tasks from the tasks table.
     *
     * @return all tasks.
     */
    @Query("SELECT * FROM sleep")
    suspend fun getAll(): List<LocalTask>

    /**
     * Select a sleep by id.
     *
     * @param taskId the sleep id.
     * @return the sleep with taskId.
     */
    @Query("SELECT * FROM sleep WHERE id = :taskId")
    suspend fun getById(taskId: String): LocalTask?

    /**
     * Insert or update a sleep in the database. If a sleep already exists, replace it.
     *
     * @param task the sleep to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(task: LocalTask)

    /**
     * Insert or update tasks in the database. If a sleep already exists, replace it.
     *
     * @param tasks the tasks to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(tasks: List<LocalTask>)

    /**
     * Update the complete status of a sleep
     *
     * @param taskId id of the sleep
     * @param completed status to be updated
     */
    @Query("UPDATE sleep SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateCompleted(taskId: String, completed: Boolean)

    /**
     * Delete a sleep by id.
     *
     * @return the number of tasks deleted. This should always be 1.
     */
    @Query("DELETE FROM sleep WHERE id = :taskId")
    suspend fun deleteById(taskId: String): Int

    /**
     * Delete all tasks.
     */
    @Query("DELETE FROM sleep")
    suspend fun deleteAll()

    /**
     * Delete all completed tasks from the table.
     *
     * @return the number of tasks deleted.
     */
    @Query("DELETE FROM sleep WHERE isCompleted = 1")
    suspend fun deleteCompleted(): Int
}
