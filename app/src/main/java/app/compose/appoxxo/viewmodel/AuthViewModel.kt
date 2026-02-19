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

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState: StateFlow<UiState<User>> = _uiState

    // Usuario actual en sesión (disponible tras login)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.login(email, password)

            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        _currentUser.value = user
                        _uiState.value = UiState.Success(user)
                    } else {
                        _uiState.value = UiState.Error("Usuario inválido")
                    }
                },
                onFailure = { exception ->
                    _uiState.value = UiState.Error(
                        exception.message ?: "Error al iniciar sesión"
                    )
                }
            )
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

            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        _currentUser.value = user
                        _uiState.value = UiState.Success(user)
                    } else {
                        _uiState.value = UiState.Error("Error inesperado")
                    }
                },
                onFailure = { exception ->
                    _uiState.value = UiState.Error(
                        exception.message ?: "Error al registrarse"
                    )
                }
            )

        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = UiState.Idle
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}