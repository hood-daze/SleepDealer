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

package com.hood.sleepdealer.data.source.network

import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

class SleepNetworkDataSource @Inject constructor() : NetworkDataSource {

    // A mutex is used to ensure that reads and writes are thread-safe.
    private val accessMutex = Mutex()

    private var sleeps = listOf(
        NetworkSleep(
            id = "PISA",
            title = "Build tower in Pisa",
            score = 20,
            dateTime = LocalDateTime.now()
        ),
        NetworkSleep(
            id = "TACOMA",
            title = "Finish bridge in Tacoma",
            score = 30,
            dateTime = LocalDateTime.now()
        )
    )


    override suspend fun loadSleeps(): List<NetworkSleep> = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        return sleeps
    }


    override suspend fun saveSleeps(newSleeps: List<NetworkSleep>) = accessMutex.withLock {
        delay(SERVICE_LATENCY_IN_MILLIS)
        sleeps = newSleeps
    }
}

private const val SERVICE_LATENCY_IN_MILLIS = 2000L
