package app.compose.appoxxo.data

import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository


// Inyección de dependencias sencilla sin Hilt/Koin
object ServiceLocator {
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val productRepository: ProductRepository by lazy { ProductRepository() }
}