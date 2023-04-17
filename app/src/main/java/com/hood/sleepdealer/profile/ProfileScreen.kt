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

package com.hood.sleepdealer.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.hood.sleepdealer.R
import com.hood.sleepdealer.util.StatisticsTopAppBar

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ProfileScreen(
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { StatisticsTopAppBar(openDrawer) }
    ) { paddingValues ->
        StatisticsContent(
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun StatisticsContent(
    modifier: Modifier = Modifier

) {
    val commonModifier = modifier
        .fillMaxWidth()
        .padding(all = dimensionResource(id = R.dimen.horizontal_margin))

    Text(
        text = "プロフィール",
        modifier = commonModifier
    )
}

@Preview
@Composable
fun StatisticsContentPreview() {
    AppCompatTheme {
        Surface {
            StatisticsContent()
        }
    }
}

@Preview
@Composable
fun StatisticsContentEmptyPreview() {
    AppCompatTheme {
        Surface {
            StatisticsContent()
        }
    }
}
