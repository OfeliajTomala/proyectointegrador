package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User?>>(UiState.Idle)
    val uiState: StateFlow<UiState<User?>> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Restaura el usuario en caché si ya había una sesión activa
        // (por ejemplo al rotar pantalla o volver a abrir la app)
        _currentUser.value = repository.getCurrentUser()
    }

    // ─── Email / Password ────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value = UiState.Success(result.getOrNull())
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
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
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value = UiState.Success(result.getOrNull())
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }

    // ─── Google Sign-In ──────────────────────────────────────────

    // Recibe el idToken extraído por Credential Manager en MainActivity
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value = UiState.Success(result.getOrNull())
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al iniciar sesión con Google"
                )
            }
        }
    }

    // ─── Session ─────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = UiState.Idle
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }

}