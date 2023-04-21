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

package com.hood.sleepdealer

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.hood.sleepdealer.SleepDealerDestinationsArgs.SLEEP_ID_ARG
import com.hood.sleepdealer.SleepDealerDestinationsArgs.USER_MESSAGE_ARG
import com.hood.sleepdealer.SleepDealerScreens.ADD_SLEEP_SCREEN
import com.hood.sleepdealer.SleepDealerScreens.PROFILE_SCREEN
import com.hood.sleepdealer.SleepDealerScreens.SLEEPS_SCREEN
import com.hood.sleepdealer.SleepDealerScreens.SLEEP_DETAIL_SCREEN

/**
 * Screens used in [SleepDealerDestinations]
 */
private object SleepDealerScreens {
    const val SLEEPS_SCREEN = "sleeps"
    const val PROFILE_SCREEN = "profile"
    const val SLEEP_DETAIL_SCREEN = "sleep"
    const val ADD_SLEEP_SCREEN = "addEditSleep"
}

/**
 * Arguments used in [SleepDealerDestinations] routes
 */
object SleepDealerDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val SLEEP_ID_ARG = "sleepId"
}

/**
 * Destinations used in the [SleepDealerActivity]
 */
object SleepDealerDestinations {
    const val SLEEPS_ROUTE = "$SLEEPS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val PROFILE_ROUTE = PROFILE_SCREEN
    const val SLEEP_DETAIL_ROUTE = "$SLEEP_DETAIL_SCREEN/{$SLEEP_ID_ARG}"
    const val ADD_SLEEP_ROUTE = "$ADD_SLEEP_SCREEN"
}

/**
 * Models the navigation actions in the app.
 */
class SleepDealerNavigationActions(private val navController: NavHostController) {

    fun navigateToSleeps(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            SLEEPS_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToProfile() {
        navController.navigate(SleepDealerDestinations.PROFILE_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateToSleepDetail(sleepId: String) {
        navController.navigate("$SLEEP_DETAIL_SCREEN/$sleepId")
    }

    fun navigateToAddSleep() {
        navController.navigate(
            ADD_SLEEP_SCREEN
        ){
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
