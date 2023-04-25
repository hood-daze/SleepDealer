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

package com.hood.sleepdealer.addsleep

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hood.sleepdealer.R
import com.hood.sleepdealer.util.AddSleepTopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AddSleepScreen(
    @StringRes topBarTitle: Int,
    onSleepUpdate: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    viewModel: AddSleepViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = { AddSleepTopAppBar(topBarTitle, openDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveTask) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.cd_save_task))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        AddSleepContent(
            loading = uiState.isLoading,
            score = uiState.score,
            onScoreChanged = viewModel::updateScore,
            modifier = Modifier.padding(paddingValues),
            viewModel = viewModel
        )

        // Check if the sleep is saved and call onTaskUpdate event
        LaunchedEffect(uiState.isSleepSaved) {
            if (uiState.isSleepSaved) {
                onSleepUpdate()
            }
        }

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(scaffoldState, viewModel, userMessage, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
private fun AddSleepContent(
    loading: Boolean,
    score: Int,
    onScoreChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddSleepViewModel
) {
    val tempatureUiState by viewModel.tempatureUiState.collectAsStateWithLifecycle()
    val sensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    if (lightSensor == null) {
       Log.d("errror","no light sensor")
    } else {
        val sensorEventListener = remember {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val c = event.values[0]
                    viewModel.onSensorChanged(c)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }
            }
        }
        DisposableEffect(Unit) {
            sensorManager.registerListener(
                sensorEventListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            onDispose {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
    }

    if (loading) {
        SwipeRefresh(
            // Show the loading spinner—`loading` is `true` in this code path
            state = rememberSwipeRefreshState(true),
            onRefresh = { /* DO NOTHING */ },
            content = { },
        )
    } else {
        Column(
            modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = { onScoreChanged((0..100).random()) },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(stringResource(id = R.string.description_hint))
            }

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.h5
            )

            Button(
                onClick = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("測定開始")
            }
            Text(
                text = tempatureUiState.tempature.toString(),
                style = MaterialTheme.typography.h5
            )
        }
    }
}
