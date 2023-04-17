package com.hood.sleepdealer.sleeps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hood.sleepdealer.ADD_EDIT_RESULT_OK
import com.hood.sleepdealer.DELETE_RESULT_OK
import com.hood.sleepdealer.EDIT_RESULT_OK
import com.hood.sleepdealer.R
import com.hood.sleepdealer.data.Sleep
import com.hood.sleepdealer.data.SleepRepository
import com.hood.sleepdealer.sleeps.SleepsType.ALL_TASKS
import com.hood.sleepdealer.util.Async
import com.hood.sleepdealer.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UiState for the sleep list screen.
 */
data class SleepsUiState(
    val items: List<Sleep> = emptyList(),
    val isLoading: Boolean = false,
    val uiInfo: UiInfo = UiInfo(),
    val userMessage: Int? = null
)

/**
 * ViewModel for the sleep list screen.
 */
@HiltViewModel
class SleepsViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(TASKS_FILTER_SAVED_STATE_KEY, ALL_TASKS)

    private val _uiInfo = _savedFilterType.map { getUiInfo() }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _sleepsAsync =
        sleepRepository.getSleepsStream()
            .map { Async.Success(it) }
            .catch<Async<List<Sleep>>> { emit(Async.Error(R.string.loading_tasks_error)) }

    val uiState: StateFlow<SleepsUiState> = combine(
        _uiInfo, _isLoading, _userMessage, _sleepsAsync
    ) { uiInfo, isLoading, userMessage, sleepsAsync ->
        when (sleepsAsync) {
            Async.Loading -> {
                SleepsUiState(isLoading = true)
            }
            is Async.Error -> {
                SleepsUiState(userMessage = sleepsAsync.errorMessage)
            }
            is Async.Success -> {
                SleepsUiState(
                    items = sleepsAsync.data,
                    uiInfo = uiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = SleepsUiState(isLoading = true)
        )


    fun clearCompletedTasks() {
        viewModelScope.launch {
            sleepRepository.clearCompletedTasks()
            showSnackbarMessage(R.string.completed_sleeps_cleared)
            refresh()
        }
    }


    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_sleep_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_sleep_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_sleep_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            sleepRepository.refresh()
            _isLoading.value = false
        }
    }

    private fun getUiInfo(): UiInfo =
        UiInfo(
            R.string.label_all, R.string.no_sleeps_all,
            R.drawable.logo_no_fill
        )
}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

data class UiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noSleepsLabel: Int = R.string.no_sleeps_all,
    val noSleepIconRes: Int = R.drawable.logo_no_fill,
)
