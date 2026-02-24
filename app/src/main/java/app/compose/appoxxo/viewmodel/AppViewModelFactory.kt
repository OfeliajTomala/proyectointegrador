package app.compose.appoxxo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.repository.ImageRepository

class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext        = context.applicationContext
    private val application       = appContext as Application  // ← cast seguro
    private val authRepository    = ServiceLocator.authRepository
    private val productRepository = ServiceLocator.productRepository
    private val imageRepository   by lazy {
        ImageRepository(ServiceLocator.supabaseClient, appContext)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(authRepository, application) as T   // ← application
        modelClass.isAssignableFrom(ProductViewModel::class.java) ->
            ProductViewModel(productRepository, authRepository, imageRepository) as T
        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(productRepository) as T
        modelClass.isAssignableFrom(UserViewModel::class.java) ->
            UserViewModel(authRepository) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}