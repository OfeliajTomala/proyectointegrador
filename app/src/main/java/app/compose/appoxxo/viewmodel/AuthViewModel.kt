package app.compose.appoxxo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User?>>(UiState.Idle)
    val uiState: StateFlow<UiState<User?>> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init { _currentUser.value = repository.getCurrentUser() }

    // ─── Auth ─────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Completa todos los campos"); return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.login(email, password)
            if (result.isSuccess) {
                // Sincroniza el email por si fue verificado y actualizado en Firebase Auth
                repository.syncEmailFromFirebase()
                _currentUser.value = repository.getCurrentUser()
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
            _uiState.value = UiState.Error("Completa todos los campos"); return
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

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                // Sincroniza el email también en login con Google
                repository.syncEmailFromFirebase()
                _currentUser.value = repository.getCurrentUser()
                _uiState.value = UiState.Success(result.getOrNull())
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error con Google"
                )
            }
        }
    }

    // ─── Edición de perfil ────────────────────────────────────────

    fun updateName(name: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateName(name)
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(name = name)
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar nombre"
                )
            }
        }
    }

    fun updateEmail(newEmail: String, currentPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateEmail(newEmail, currentPassword)
            if (result.isSuccess) {
                // NO actualizamos _currentUser aquí — el email cambia
                // solo después de que el usuario verifique el nuevo correo
                // y vuelva a iniciar sesión
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar correo"
                )
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updatePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al cambiar contraseña"
                )
            }
        }
    }

    fun addPasswordToGoogleAccount(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.addPasswordToGoogleAccount(newPassword)
            if (result.isSuccess) {
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al agregar contraseña"
                )
            }
        }
    }

    // ─── Imagen de perfil ─────────────────────────────────────────

    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateProfileImage(uri)
            if (result.isSuccess) {
                val url = result.getOrNull() ?: ""
                _currentUser.value = _currentUser.value?.copy(photoUrl = url)
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al subir imagen"
                )
            }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.deleteProfileImage()
            if (result.isSuccess) {
                _currentUser.value = _currentUser.value?.copy(photoUrl = "")
                _uiState.value = UiState.Success(null)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar imagen"
                )
            }
        }
    }

    // ─── Session ──────────────────────────────────────────────────

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _uiState.value = UiState.Idle
    }

    fun resetState() { _uiState.value = UiState.Idle }
}