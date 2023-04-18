/*
 * Copyright 2022 The Android Open Source Project
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

package com.hood.sleepdealer.sleepdetail

import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.hood.sleepdealer.HiltTestActivity
import com.hood.sleepdealer.data.SleepRepository
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the Sleep Details screen.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class TaskDetailScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var repository: SleepRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun activeTaskDetails_DisplayedInUi() = runTest {
        // GIVEN - Add active (incomplete) sleep to the DB
        val activeSleepId = repository.createSleep(
            title = "Active Sleep",
            description = "AndroidX Rocks"
        )

        // WHEN - Details screen is opened
        setContent(activeSleepId)

        // THEN - Sleep details are displayed on the screen
        // make sure that the title/description are both shown and correct
        composeTestRule.onNodeWithText("Active Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("AndroidX Rocks").assertIsDisplayed()
        // and make sure the "active" checkbox is shown unchecked
        composeTestRule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun completedTaskDetails_DisplayedInUi() = runTest {
        // GIVEN - Add completed sleep to the DB
        val completedSleepId = repository.createSleep("Completed Sleep", "AndroidX Rocks")
        repository.completeTask(completedSleepId)

        // WHEN - Details screen is opened
        setContent(completedSleepId)

        // THEN - Sleep details are displayed on the screen
        // make sure that the title/description are both shown and correct
        composeTestRule.onNodeWithText("Completed Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("AndroidX Rocks").assertIsDisplayed()
        // and make sure the "active" checkbox is shown unchecked
        composeTestRule.onNode(isToggleable()).assertIsOn()
    }

    private fun setContent(activeTaskId: String) {
        composeTestRule.setContent {
            AppCompatTheme {
                Surface {
                    SleepDetailScreen(
                        viewModel = SleepDetailViewModel(
                            repository,
                            SavedStateHandle(mapOf("taskId" to activeTaskId))
                        ),
                        onEditTask = { /*TODO*/ },
                        onBack = { },
                        onDeleteTask = { },
                    )
                }
            }
        }
    }
}
