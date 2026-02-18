package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState: StateFlow<UiState<User>> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.login(email, password)
            val user = result.getOrNull()
            _uiState.value = if (result.isSuccess && user != null) {
                UiState.Success(user)
            } else {
                UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun register(email: String, password: String, name: String = "") {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.register(email, password, name)
            val user = result.getOrNull()
            _uiState.value = if (result.isSuccess && user != null) {
                UiState.Success(user)
            } else {
                UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al registrarse")
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = UiState.Idle
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
