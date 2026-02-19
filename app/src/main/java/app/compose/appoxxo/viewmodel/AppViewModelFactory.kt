package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository
import kotlin.jvm.java

// Factory personalizada para poder inyectar repositorios manualmente


class AppViewModelFactory(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository = authRepository) as T

            modelClass.isAssignableFrom(ProductViewModel::class.java) ->
                ProductViewModel(
                    repository = productRepository,
                    authRepository = authRepository
                ) as T

            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository = productRepository) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(repository = authRepository) as T

            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
