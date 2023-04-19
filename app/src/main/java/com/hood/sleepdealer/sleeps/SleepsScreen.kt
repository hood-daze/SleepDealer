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

package com.hood.sleepdealer.sleeps

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hood.sleepdealer.R
import com.hood.sleepdealer.data.Sleep
import com.hood.sleepdealer.util.LoadingContent
import com.hood.sleepdealer.util.SleepsTopAppBar
import com.google.accompanist.appcompattheme.AppCompatTheme

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SleepsScreen(
    @StringRes userMessage: Int,
    onSleepClick: (Sleep) -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SleepsViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            SleepsTopAppBar(
                openDrawer = openDrawer
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        val uiState by viewModel.sleepsUiState.collectAsStateWithLifecycle()

        SleepsContent(
            loading = uiState.isLoading,
            sleeps = uiState.items,
            currentFilteringLabel = uiState.sleepsUiInfo.currentFilteringLabel,
            noSleepsLabel = uiState.sleepsUiInfo.noSleepsLabel,
            noSleepsIconRes = uiState.sleepsUiInfo.noSleepIconRes,
            onRefresh = viewModel::refresh,
            onSleepClick = onSleepClick,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { message ->
            val snackbarText = stringResource(message)
            LaunchedEffect(scaffoldState, viewModel, message, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

@Composable
private fun SleepsContent(
    loading: Boolean,
    sleeps: List<Sleep>,
    @StringRes currentFilteringLabel: Int,
    @StringRes noSleepsLabel: Int,
    @DrawableRes noSleepsIconRes: Int,
    onRefresh: () -> Unit,
    onSleepClick: (Sleep) -> Unit,
    modifier: Modifier = Modifier
) {
    LoadingContent(
        loading = loading,
        empty = sleeps.isEmpty() && !loading,
        emptyContent = { TasksEmptyContent(noSleepsLabel, noSleepsIconRes, modifier) },
        onRefresh = onRefresh
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
        ) {
            Text(
                text = stringResource(currentFilteringLabel),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_padding),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                ),
                style = MaterialTheme.typography.h6
            )
            LazyColumn {
                items(sleeps) { task ->
                    TaskItem(
                        sleep = task,
                        onTaskClick = onSleepClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    sleep: Sleep,
    onTaskClick: (Sleep) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onTaskClick(sleep) }
    ) {
        Text(
            text = sleep.titleForList,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            )
        )
    }
}

@Composable
private fun TasksEmptyContent(
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = noTasksIconRes),
            contentDescription = stringResource(R.string.no_tasks_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = noTasksLabel))
    }
}

@Preview
@Composable
private fun TasksContentPreview() {
    AppCompatTheme {
        Surface {
            SleepsContent(
                loading = false,
                sleeps = listOf(
                    Sleep(
                        title = "Title 1",
                        description = "Description 1",
                        id = "ID 1"
                    ),
                    Sleep(
                        title = "Title 2",
                        description = "Description 2",
                        id = "ID 2"
                    ),
                    Sleep(
                        title = "Title 3",
                        description = "Description 3",
                        id = "ID 3"
                    ),
                    Sleep(
                        title = "Title 4",
                        description = "Description 4",
                        id = "ID 4"
                    ),
                    Sleep(
                        title = "Title 5",
                        description = "Description 5",
                        id = "ID 5"
                    ),
                ),
                currentFilteringLabel = R.string.label_all,
                noSleepsLabel = R.string.no_sleeps_all,
                noSleepsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onSleepClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun TasksContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            SleepsContent(
                loading = false,
                sleeps = emptyList(),
                currentFilteringLabel = R.string.label_all,
                noSleepsLabel = R.string.no_sleeps_all,
                noSleepsIconRes = R.drawable.logo_no_fill,
                onRefresh = { },
                onSleepClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun TasksEmptyContentPreview() {
    AppCompatTheme {
        Surface {
            TasksEmptyContent(
                noTasksLabel = R.string.no_sleeps_all,
                noTasksIconRes = R.drawable.logo_no_fill
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemPreview() {
    AppCompatTheme {
        Surface {
            TaskItem(
                sleep = Sleep(
                    title = "Title",
                    description = "Description",
                    id = "ID"
                ),
                onTaskClick = { }
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemCompletedPreview() {
    AppCompatTheme {
        Surface {
            TaskItem(
                sleep = Sleep(
                    title = "Title",
                    description = "Description",
                    id = "ID"
                ),
                onTaskClick = { }
            )
        }
    }
}
