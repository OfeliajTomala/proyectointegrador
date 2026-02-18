package app.compose.appoxxo.data.util

// Estado genérico para manejar Loading, Success y Error
sealed class UiState<out T> {

    object Idle : UiState<Nothing>()

    object Loading : UiState<Nothing>()

    data class Success<T>(val data: T) : UiState<T>()

    data class Error(val message: String) : UiState<Nothing>()
}