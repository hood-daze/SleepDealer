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
    fun insertTaskAndGetById() = runTest {
        // GIVEN - insert a sleep
        val task = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = false,
        )
        database.sleepDao().upsert(task)

        // WHEN - Get the sleep by id from the database
        val loaded = database.sleepDao().getById(task.id)

        // THEN - The loaded data contains the expected values
        assertNotNull(loaded as LocalSleep)
        assertEquals(task.id, loaded.id)
        assertEquals(task.title, loaded.title)
        assertEquals(task.score, loaded.score)
        assertEquals(task.isCompleted, loaded.isCompleted)
    }

    @Test
    fun insertTaskReplacesOnConflict() = runTest {
        // Given that a sleep is inserted
        val task = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = false,
        )
        database.sleepDao().upsert(task)

        // When a sleep with the same id is inserted
        val newTask = LocalSleep(
            title = "title2",
            score = "description2",
            isCompleted = true,
            id = task.id
        )
        database.sleepDao().upsert(newTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.sleepDao().getById(task.id)
        assertEquals(task.id, loaded?.id)
        assertEquals("title2", loaded?.title)
        assertEquals("description2", loaded?.score)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun insertTaskAndGetTasks() = runTest {
        // GIVEN - insert a sleep
        val task = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = false,
        )
        database.sleepDao().upsert(task)

        // WHEN - Get tasks from the database
        val tasks = database.sleepDao().getAll()

        // THEN - There is only 1 sleep in the database, and contains the expected values
        assertEquals(1, tasks.size)
        assertEquals(tasks[0].id, task.id)
        assertEquals(tasks[0].title, task.title)
        assertEquals(tasks[0].score, task.score)
        assertEquals(tasks[0].isCompleted, task.isCompleted)
    }

    @Test
    fun updateTaskAndGetById() = runTest {
        // When inserting a sleep
        val originalTask = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = false,
        )

        database.sleepDao().upsert(originalTask)

        // When the sleep is updated
        val updatedTask = LocalSleep(
            title = "new title",
            score = "new description",
            isCompleted = true,
            id = originalTask.id
        )
        database.sleepDao().upsert(updatedTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.sleepDao().getById(originalTask.id)
        assertEquals(originalTask.id, loaded?.id)
        assertEquals("new title", loaded?.title)
        assertEquals("new description", loaded?.score)
        assertEquals(true, loaded?.isCompleted)
    }

    @Test
    fun updateCompletedAndGetById() = runTest {
        // When inserting a sleep
        val task = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = true
        )
        database.sleepDao().upsert(task)

        // When the sleep is updated
        database.sleepDao().updateCompleted(task.id, false)

        // THEN - The loaded data contains the expected values
        val loaded = database.sleepDao().getById(task.id)
        assertEquals(task.id, loaded?.id)
        assertEquals(task.title, loaded?.title)
        assertEquals(task.score, loaded?.score)
        assertEquals(false, loaded?.isCompleted)
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() = runTest {
        // Given a sleep inserted
        val task = LocalSleep(
            title = "title",
            score = "description",
            id = "id",
            isCompleted = false,
        )
        database.sleepDao().upsert(task)

        // When deleting a sleep by id
        database.sleepDao().deleteById(task.id)

        // THEN - The list is empty
        val tasks = database.sleepDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }

    @Test
    fun deleteTasksAndGettingTasks() = runTest {
        // Given a sleep inserted
        database.sleepDao().upsert(
            LocalSleep(
                title = "title",
                score = "description",
                id = "id",
                isCompleted = false,
            )
        )

        // When deleting all tasks
        database.sleepDao().deleteAll()

        // THEN - The list is empty
        val tasks = database.sleepDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }

    @Test
    fun deleteCompletedTasksAndGettingTasks() = runTest {
        // Given a completed sleep inserted
        database.sleepDao().upsert(
            LocalSleep(title = "completed", score = "sleep", id = "id", isCompleted = true)
        )

        // When deleting completed tasks
        database.sleepDao().deleteCompleted()

        // THEN - The list is empty
        val tasks = database.sleepDao().getAll()
        assertEquals(true, tasks.isEmpty())
    }
}
