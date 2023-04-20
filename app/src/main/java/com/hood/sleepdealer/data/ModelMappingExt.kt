/*
 * Copyright 2023 The Android Open Source Project
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

import com.hood.sleepdealer.data.source.local.LocalSleep
import com.hood.sleepdealer.data.source.network.NetworkSleep

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Sleep: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - NetworkSleep: Internal model used to represent a sleep from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalSleep: Internal model used to represent a sleep stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

// External to local
fun Sleep.toLocal() = LocalSleep(
    id = id,
    title = title,
    score = score,
    dateTime = dateTime
)

fun List<Sleep>.toLocal() = map(Sleep::toLocal)

// Local to External
fun LocalSleep.toExternal() = Sleep(
    id = id,
    title = title,
    score = score,
    dateTime = dateTime
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalSleep>.toExternal() = map(LocalSleep::toExternal)

// Network to Local
fun NetworkSleep.toLocal() = LocalSleep(
    id = id,
    title = title,
    score = score,
    dateTime = dateTime
)

@JvmName("networkToLocal")
fun List<NetworkSleep>.toLocal() = map(NetworkSleep::toLocal)

// Local to Network
fun LocalSleep.toNetwork() = NetworkSleep(
    id = id,
    title = title,
    score = score,
    dateTime = dateTime
)

fun List<LocalSleep>.toNetwork() = map(LocalSleep::toNetwork)

// External to Network
fun Sleep.toNetwork() = toLocal().toNetwork()

@JvmName("externalToNetwork")
fun List<Sleep>.toNetwork() = map(Sleep::toNetwork)

// Network to External
fun NetworkSleep.toExternal() = toLocal().toExternal()

@JvmName("networkToExternal")
fun List<NetworkSleep>.toExternal() = map(NetworkSleep::toExternal)
