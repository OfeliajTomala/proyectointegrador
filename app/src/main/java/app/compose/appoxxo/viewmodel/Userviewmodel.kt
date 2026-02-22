package app.compose.appoxxo.viewmodel

import app.compose.appoxxo.data.util.UiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.getUsers()
            if (result.isSuccess) {
                _users.value = result.getOrDefault(emptyList())
                _uiState.value = UiState.Idle
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al cargar usuarios"
                )
            }
        }
    }

    fun updateRole(uid: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.updateUserRole(uid, role)
            if (result.isSuccess) {
                // Actualiza localmente sin recargar todo
                _users.value = _users.value.map {
                    if (it.uid == uid) it.copy(role = role) else it
                }
                _uiState.value = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar rol"
                )
            }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.deleteUser(uid)
            if (result.isSuccess) {
                // Elimina localmente sin recargar todo
                _users.value = _users.value.filter { it.uid != uid }
                _uiState.value = UiState.Success(Unit)
            } else {
                _uiState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar usuario"
                )
            }
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }
}