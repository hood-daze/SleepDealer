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

package com.hood.sleepdealer.profile

import com.hood.sleepdealer.data.Sleep
import com.hood.sleepdealer.profile.getActiveAndCompletedStats
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Unit tests for [getActiveAndCompletedStats].
 */
class StatisticsUtilsTest {

    @Test
    fun getActiveAndCompletedStats_noCompleted() {
        val sleeps = listOf(
            Sleep(
                id = "id",
                title = "title",
                score = "desc",
                isCompleted = false,
            )
        )
        // When the list of tasks is computed with an active sleep
        val result = getActiveAndCompletedStats(sleeps)

        // Then the percentages are 100 and 0
        assertThat(result.activeTasksPercent, `is`(100f))
        assertThat(result.completedTasksPercent, `is`(0f))
    }

    @Test
    fun getActiveAndCompletedStats_noActive() {
        val sleeps = listOf(
            Sleep(
                id = "id",
                title = "title",
                score = "desc",
                isCompleted = true,
            )
        )
        // When the list of tasks is computed with a completed sleep
        val result = getActiveAndCompletedStats(sleeps)

        // Then the percentages are 0 and 100
        assertThat(result.activeTasksPercent, `is`(0f))
        assertThat(result.completedTasksPercent, `is`(100f))
    }

    @Test
    fun getActiveAndCompletedStats_both() {
        // Given 3 completed tasks and 2 active tasks
        val sleeps = listOf(
            Sleep(id = "1", title = "title", score = "desc", isCompleted = true),
            Sleep(id = "2", title = "title", score = "desc", isCompleted = true),
            Sleep(id = "3", title = "title", score = "desc", isCompleted = true),
            Sleep(id = "4", title = "title", score = "desc", isCompleted = false),
            Sleep(id = "5", title = "title", score = "desc", isCompleted = false),
        )
        // When the list of tasks is computed
        val result = getActiveAndCompletedStats(sleeps)

        // Then the result is 40-60
        assertThat(result.activeTasksPercent, `is`(40f))
        assertThat(result.completedTasksPercent, `is`(60f))
    }

    @Test
    fun getActiveAndCompletedStats_empty() {
        // When there are no tasks
        val result = getActiveAndCompletedStats(emptyList())

        // Both active and completed tasks are 0
        assertThat(result.activeTasksPercent, `is`(0f))
        assertThat(result.completedTasksPercent, `is`(0f))
    }
}
