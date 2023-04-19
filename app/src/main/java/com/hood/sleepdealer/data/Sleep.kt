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

import java.time.LocalDateTime

/**
 * Immutable model class for a Sleep.
 *
 * @param title title of the sleep
 * @param description description of the sleep
 * @param isCompleted whether or not this sleep is completed
 * @param id id of the sleep
 *
 * TODO: The constructor of this class should be `internal` but it is used in previews and tests
 *  so that's not possible until those previews/tests are refactored.
 */
data class Sleep(
    val title: String = "",
    val description: String = "",
    val dateTime: LocalDateTime,
    val id: String,
) {

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description


    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}
