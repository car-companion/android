package com.dsd.carcompanion.utils

sealed class ResultOf<out T> {
    data object Idle : ResultOf<Nothing>()
    data object Loading : ResultOf<Nothing>()
    data class Success<out T>(val data: T) : ResultOf<T>()
    data class Error(val message: String) : ResultOf<Nothing>()
}