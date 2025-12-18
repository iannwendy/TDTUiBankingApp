package com.example.tdtumobilebanking.core.common

sealed class ResultState<out T> {
    data object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val throwable: Throwable) : ResultState<Nothing>()
}

