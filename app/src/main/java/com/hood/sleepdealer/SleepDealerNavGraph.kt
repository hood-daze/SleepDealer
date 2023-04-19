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

import android.app.Activity
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hood.sleepdealer.SleepDealerDestinationsArgs.TITLE_ARG
import com.hood.sleepdealer.SleepDealerDestinationsArgs.USER_MESSAGE_ARG
import com.hood.sleepdealer.addsleep.AddSleepScreen
import com.hood.sleepdealer.profile.ProfileScreen
import com.hood.sleepdealer.sleepdetail.SleepDetailScreen
import com.hood.sleepdealer.sleeps.SleepsScreen
import com.hood.sleepdealer.util.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SleepDealerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = SleepDealerDestinations.SLEEPS_ROUTE,
    navActions: SleepDealerNavigationActions = remember(navController) {
        SleepDealerNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            SleepDealerDestinations.SLEEPS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            AppModalDrawer(drawerState, currentRoute, navActions) {
                SleepsScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onSleepClick = { task -> navActions.navigateToSleepDetail(task.id) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
        composable(SleepDealerDestinations.PROFILE_ROUTE) {
            AppModalDrawer(drawerState, currentRoute, navActions) {
                ProfileScreen(
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
        composable(
            SleepDealerDestinations.ADD_SLEEP_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType }
            )
        ) { entry ->
            AppModalDrawer(drawerState, currentRoute, navActions) {
                AddSleepScreen(
                    topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                    onTaskUpdate = {
                        navActions.navigateToSleeps(ADD_RESULT_OK)
                    },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
        composable(SleepDealerDestinations.SLEEP_DETAIL_ROUTE) {
            SleepDetailScreen(
                onBack = { navController.popBackStack() },
                onDeleteTask = { navActions.navigateToSleeps(DELETE_RESULT_OK) }
            )
        }
    }
}

// Keys for navigation
const val ADD_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2