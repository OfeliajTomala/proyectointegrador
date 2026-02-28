package app.compose.appoxxo.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val repository: AuthRepository,
    private val application: Application   // ← Application en lugar de Context
) : ViewModel() {

    // ─── Estado del usuario actual ────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // ─── Estado UI ────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)  // ← Unit no User?
    val uiState: StateFlow<UiState<Unit>> = _uiState

    //false hasta que loadCurrentUser() termine
    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady: StateFlow<Boolean> = _isAuthReady

    init {
        loadCurrentUser()
    }

    // ─── Carga inicial ────────────────────────────────────────────

    private fun loadCurrentUser() {
        viewModelScope.launch {
            // Intenta recargar desde Firestore si Firebase tiene sesión activa
            val user = repository.reloadCurrentUser() ?: repository.getCurrentUser()
            _currentUser.value = user
            _isAuthReady.value = true
        }
    }

    // ─── Sincroniza usuario desde Firestore ───────────────────────
    fun syncUser() {
        viewModelScope.launch {
            try {

                val user = withContext(Dispatchers.IO) {
                    repository.reloadCurrentUser()
                }

                user?.let {
                    _currentUser.value = it
                }

            } catch (e: Exception) {
                android.util.Log.w("AuthViewModel", "syncUser  falló: ${e.message}")
            }
        }
    }

    // ─── Registro ─────────────────────────────────────────────────

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.register(
                email    = email,
                password = password,
                name     = name,
                role     = UserRole.CAJERO
            )
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value     = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }

    // ─── Login ────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value     = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    // ─── Google Sign In ───────────────────────────────────────────

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            if (idToken.isEmpty()) {
                _uiState.value = UiState.Error("Error al iniciar sesión con Google")
                return@launch
            }
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value     = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error con Google"
                )
            }
        }
    }

    // ─── Logout ───────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value     = UiState.Idle
    }

    // ─── Edición de nombre ────────────────────────────────────────

    fun updateName(name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateName(name)
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(name = name)
                _uiState.value     = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar nombre"
                )
            }
        }
    }

    // ─── Edición de correo ────────────────────────────────────────

    fun updateEmail(newEmail: String, currentPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateEmail(newEmail, currentPassword)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar correo"
                )
            }
        }
    }

    // ─── Sincroniza email desde Firebase Auth ─────────────────────

    fun syncEmail() {
        viewModelScope.launch {
            val result = repository.syncEmailFromFirebase()
            if (result.isSuccess) {
                val user = repository.reloadCurrentUser()
                if (user != null) _currentUser.value = user
            }
            // No pongas UiState.Loading aquí para no bloquear la UI
        }
    }

    // ─── Cambiar contraseña ───────────────────────────────────────

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updatePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al cambiar contraseña"
                )
            }
        }
    }

    // ─── Agregar contraseña a cuenta Google ───────────────────────

    fun addPasswordToGoogleAccount(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.addPasswordToGoogleAccount(newPassword)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al agregar contraseña"
                )
            }
        }
    }

    // ─── Imagen de perfil ─────────────────────────────────────────
    // Lee los bytes aquí con Application — no en el Repository

    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val bytes = application.contentResolver  // ← application en lugar de context
                    .openInputStream(uri)?.use { it.readBytes() }
                    ?: throw Exception("No se pudo leer la imagen")

                val result = repository.updateProfileImage(bytes)
                if (result.isSuccess) {
                    val url = result.getOrNull() ?: ""
                    _currentUser.value = _currentUser.value?.copy(photoUrl = url)
                    _uiState.value     = UiState.Success(Unit)
                } else {
                    _uiState.value = UiState.Error(
                        result.exceptionOrNull()?.message ?: "Error al subir imagen"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al subir imagen")
            }
        }
    }

    // ─── Eliminar imagen de perfil ────────────────────────────────

    fun deleteProfileImage() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.deleteProfileImage()
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(photoUrl = "")
                _uiState.value     = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar imagen"
                )
            }
        }
    }

    // ─── Reset estado ─────────────────────────────────────────────

    fun resetState() { _uiState.value = UiState.Idle }
}