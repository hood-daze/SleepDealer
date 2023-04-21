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

import kotlinx.coroutines.flow.Flow

/**
 * Interface to the data layer.
 */
interface SleepRepository {

    fun getSleepsStream(): Flow<List<Sleep>>

    suspend fun getSleeps(forceUpdate: Boolean = false): List<Sleep>

    suspend fun refresh()

    fun getSleepStream(sleepId: String): Flow<Sleep?>

    suspend fun getSleep(sleepId: String, forceUpdate: Boolean = false): Sleep?

    suspend fun refreshSleep(sleepId: String)

    suspend fun deleteAllSleeps()

    suspend fun deleteSleep(sleepId: String)

    suspend fun createSleep(score: Int): String

    suspend fun updateSleep(sleepId: String, score: Int)
}
