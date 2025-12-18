package com.example.tdtumobilebanking.core.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State>(initialState: State) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState

    protected fun setState(reducer: State.() -> State) {
        _uiState.update { current -> current.reducer() }
    }
}

