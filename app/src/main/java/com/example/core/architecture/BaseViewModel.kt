package com.example.core.architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Standardized BaseViewModel enforcing unidirectional data flow (UDF)
 * - StateFlow<S> for observable immutable state
 * - SharedFlow<E> for transient side-effects / one-off events
 * - handleIntent(A) for processing actions/intents
 */
abstract class BaseViewModel<S : UiState, A : UiAction, E : UiEvent>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<E>(replay = 0, extraBufferCapacity = 16)
    val uiEvent: SharedFlow<E> = _uiEvent.asSharedFlow()

    /**
     * Entry point for processing user actions and intents
     */
    abstract fun handleIntent(action: A)

    /**
     * Atomically mutates the current UI state using Kotlin copy semantics
     */
    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)
    }

    /**
     * Emits a transient one-off event (e.g. snackbar message, navigation signal, haptic cue)
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}
