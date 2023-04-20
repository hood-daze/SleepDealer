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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.hood.sleepdealer.R
import com.hood.sleepdealer.data.Sleep
import com.hood.sleepdealer.util.LoadingContent
import com.hood.sleepdealer.util.SleepDetailTopAppBar
import java.time.LocalDateTime

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SleepDetailScreen(
    onBack: () -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SleepDetailViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        topBar = {
            SleepDetailTopAppBar(onBack = onBack, onDelete = viewModel::deleteTask)
        },
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SleepDetailContent(
            loading = uiState.isLoading,
            empty = uiState.sleep == null && !uiState.isLoading,
            sleep = uiState.sleep,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(scaffoldState, viewModel, userMessage, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if the sleep is deleted and call onDeleteTask
        LaunchedEffect(uiState.isSleepDeleted) {
            if (uiState.isSleepDeleted) {
                onDeleteTask()
            }
        }
    }
}

@Composable
private fun SleepDetailContent(
    loading: Boolean,
    empty: Boolean,
    sleep: Sleep?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenPadding = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.vertical_margin),
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = commonModifier
            )
        },
        onRefresh = onRefresh
    ) {
        Column(commonModifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(screenPadding),

            ) {
                if (sleep != null) {
                    Column {
                        Text(text = sleep.title, style = MaterialTheme.typography.h6)
                        Text(text = sleep.score.toString(), style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun SleepDetailContentPreview() {
    AppCompatTheme {
        Surface {
            SleepDetailContent(
                loading = false,
                empty = false,
                Sleep(
                    title = "Title",
                    score = 30,
                    id = "ID",
                    dateTime = LocalDateTime.now()
                ),
                onRefresh = { }
            )
        }
    }
}



@Preview
@Composable
private fun SleepDetailContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            SleepDetailContent(
                loading = false,
                empty = true,
                Sleep(
                    title = "Title",
                    score = 50,
                    id = "ID",
                    dateTime = LocalDateTime.now()
                ),
                onRefresh = { }
            )
        }
    }
}
