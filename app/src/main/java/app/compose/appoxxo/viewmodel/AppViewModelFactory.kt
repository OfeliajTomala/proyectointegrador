package app.compose.appoxxo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ImageRepository
import app.compose.appoxxo.data.repository.ProductRepository

class AppViewModelFactory(
    private val context: Context,                          // applicationContext
    private val authRepository: AuthRepository    = ServiceLocator.authRepository,
    private val productRepository: ProductRepository = ServiceLocator.productRepository
) : ViewModelProvider.Factory {

    // ImageRepository se crea aquí con applicationContext — nunca en un singleton
    private val imageRepository: ImageRepository by lazy {
        ImageRepository(ServiceLocator.supabaseClient, context)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(authRepository) as T

        modelClass.isAssignableFrom(ProductViewModel::class.java) ->
            ProductViewModel(productRepository, authRepository, imageRepository) as T

        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(productRepository) as T

        modelClass.isAssignableFrom(UserViewModel::class.java) ->
            UserViewModel(authRepository) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}