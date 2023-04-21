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

package com.hood.sleepdealer.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.hood.sleepdealer.data.source.local.LocalSleep
import com.hood.sleepdealer.data.source.local.SleepDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class SleepDaoTest {

    // using an in-memory database because the information stored here disappears when the
    // process is killed
    private lateinit var database: SleepDatabase

    // Ensure that we use a new database for each test.
    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            SleepDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    @Test
    fun insertSleepAndGetById() = runTest {
        // GIVEN - insert a sleep
        val sleep = LocalSleep(
            score = 10,
            id = "id",
            dateTime = LocalDateTime.of(2023, 4, 21, 15, 30, 0)
        )
        database.sleepDao().upsert(sleep)

        // WHEN - Get the sleep by id from the database
        val loaded = database.sleepDao().getById(sleep.id)

        // THEN - The loaded data contains the expected values
        assertNotNull(loaded as LocalSleep)
        assertEquals(sleep.id, loaded.id)
        assertEquals(sleep.score, loaded.score)
        assertEquals(sleep.dateTime, loaded.dateTime)
    }

    @Test
    fun insertSleepReplacesOnConflict() = runTest {
        // Given that a sleep is inserted
        val sleep = LocalSleep(
            score = 30,
            id = "id",
            dateTime = LocalDateTime.of(2022, 4, 21, 15, 30, 0)
        )
        database.sleepDao().upsert(sleep)

        // When a sleep with the same id is inserted
        val newSleep = LocalSleep(
            score = 10,
            id = "id",
            dateTime = LocalDateTime.of(2023, 4, 21, 15, 30, 0)
        )
        database.sleepDao().upsert(newSleep)

        // THEN - The loaded data contains the expected values
        val loaded = database.sleepDao().getById(sleep.id)
        assertEquals(sleep.id, loaded?.id)
        assertEquals(10, loaded?.score)
        assertEquals(LocalDateTime.of(2023, 4, 21, 15, 30, 0), loaded?.score)
    }

    @Test
    fun insertSleepAndGetSleeps() = runTest {
        // GIVEN - insert a sleep
        val task = LocalSleep(
            score = 10,
            id = "id",
            dateTime = LocalDateTime.now()
        )
        database.sleepDao().upsert(task)

        // WHEN - Get tasks from the database
        val sleeps = database.sleepDao().getAll()

        // THEN - There is only 1 sleep in the database, and contains the expected values
        assertEquals(1, sleeps.size)
        assertEquals(sleeps[0].id, task.id)
        assertEquals(sleeps[0].score, task.score)
        assertEquals(sleeps[0].dateTime, task.dateTime)
    }


    @Test
    fun deleteSleepByIdAndGettingSleeps() = runTest {
        // Given a sleep inserted
        val sleep = LocalSleep(
            score = 10,
            id = "id",
            dateTime = LocalDateTime.now()
        )
        database.sleepDao().upsert(sleep)

        // When deleting a sleep by id
        database.sleepDao().deleteById(sleep.id)

        // THEN - The list is empty
        val sleeps = database.sleepDao().getAll()
        assertEquals(true, sleeps.isEmpty())
    }

    @Test
    fun deleteSleepsAndGettingSleeps() = runTest {
        // Given a sleep inserted
        database.sleepDao().upsert(
            LocalSleep(
                score = 10,
                id = "id",
                dateTime = LocalDateTime.now()
            )
        )

        // When deleting all tasks
        database.sleepDao().deleteAll()

        // THEN - The list is empty
        val sleeps = database.sleepDao().getAll()
        assertEquals(true, sleeps.isEmpty())
    }

}
