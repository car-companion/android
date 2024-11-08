package com.dsd.carcompanion.userRegistrationAndLogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsd.carcompanion.userRegistrationAndLogin.auth.repository.AuthRepository
import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.LoginRequest
import com.dsd.carcompanion.utils.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<ResultOf<Unit>>(ResultOf.Idle)
    val loginState: StateFlow<ResultOf<Unit>> get() = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ResultOf.Loading
            try {
                val result = authRepository.login(LoginRequest(email, password))
                _loginState.value = ResultOf.Success(Unit)
            } catch (exception: Exception) {
                _loginState.value = ResultOf.Error(exception.message ?: "Unknown error")
            }
        }
    }
}