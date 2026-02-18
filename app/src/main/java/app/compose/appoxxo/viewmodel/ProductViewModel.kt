package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository
import app.compose.appoxxo.data.util.UiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository = ServiceLocator.productRepository,
    private val authRepository: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    // Lista de productos
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // Estado de la UI
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    init {
        loadProducts() // Carga inicial de productos
    }

    // Carga todos los productos desde el repositorio
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _products.value = repository.getProducts()
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    //Agrega un producto, asignando createdBy automáticamente
    fun addProduct(product: Product) {
        val uid = authRepository.getCurrentUserId() ?: ""
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.addProduct(product.copy(createdBy = uid))
                loadProducts()
                _uiState.value = UiState.Success(Unit) // ✅ Aquí usamos Unit
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar producto")
            }
        }
    }

    // Actualiza un producto existente
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.updateProduct(product)
                loadProducts()
                _uiState.value = UiState.Success(Unit) // ✅ Unit
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar producto")
            }
        }
    }

    //Elimina un producto por su ID
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.deleteProduct(productId)
                loadProducts()
                _uiState.value = UiState.Success(Unit) // ✅ Unit
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar producto")
            }
        }
    }

    /*
     Registra un movimiento de inventario (entrada/salida)
      Completa automáticamente productName, userId, userName y date
     */
    fun registerMovement(productId: String, type: MovementType, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val product = _products.value.find { it.id == productId }
                    ?: throw Exception("Producto no encontrado")
                val uid = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""

                val movement = Movement(
                    productId = productId,
                    productName = product.name,
                    quantity = quantity,
                    type = type,
                    date = Timestamp.now(),
                    userId = uid,
                    userName = userName
                )

                repository.registerMovement(movement)
                loadProducts()
                _uiState.value = UiState.Success(Unit) // ✅ Unit
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al registrar movimiento")
            }
        }
    }

    //Resetea el estado de la UI a Idle
    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
