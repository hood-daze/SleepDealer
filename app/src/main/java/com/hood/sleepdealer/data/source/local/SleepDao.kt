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
interface SleepDao {

    /**
     * Observes list of tasks.
     *
     * @return all tasks.
     */
    @Query("SELECT * FROM sleep")
    fun observeAll(): Flow<List<LocalSleep>>

    /**
     * Observes a single sleep.
     *
     * @param sleepId the sleep id.
     * @return the sleep with taskId.
     */
    @Query("SELECT * FROM sleep WHERE id = :sleepId")
    fun observeById(sleepId: String): Flow<LocalSleep>

    /**
     * Select all tasks from the tasks table.
     *
     * @return all tasks.
     */
    @Query("SELECT * FROM sleep")
    suspend fun getAll(): List<LocalSleep>

    /**
     * Select a sleep by id.
     *
     * @param sleepId the sleep id.
     * @return the sleep with taskId.
     */
    @Query("SELECT * FROM sleep WHERE id = :sleepId")
    suspend fun getById(sleepId: String): LocalSleep?

    /**
     * Insert or update a sleep in the database. If a sleep already exists, replace it.
     *
     * @param task the sleep to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(task: LocalSleep)

    /**
     * Insert or update sleeps in the database. If a sleep already exists, replace it.
     *
     * @param tasks the tasks to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(tasks: List<LocalSleep>)


    /**
     * Delete a sleep by id.
     *
     * @return the number of tasks deleted. This should always be 1.
     */
    @Query("DELETE FROM sleep WHERE id = :sleepId")
    suspend fun deleteById(sleepId: String): Int

    /**
     * Delete all sleeps.
     */
    @Query("DELETE FROM sleep")
    suspend fun deleteAll()

}
