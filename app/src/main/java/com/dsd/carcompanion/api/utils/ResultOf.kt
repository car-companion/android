package com.dsd.carcompanion.api.utils

sealed class ResultOf<out T> {
    data object Idle : ResultOf<Nothing>()
    data object Loading : ResultOf<Nothing>()
    data class Success<out T>(val data: T, val code: Int? = null) : ResultOf<T>()
    data class Error(val message: String, val code: Int? = null) : ResultOf<Nothing>()
}