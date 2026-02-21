package app.compose.appoxxo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ImageRepository
import app.compose.appoxxo.data.repository.ProductRepository
import app.compose.appoxxo.data.util.UiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ProductViewModel(
    private val repository: ProductRepository,
    private val authRepository: AuthRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    // Movimientos del producto seleccionado (MovementDetailScreen)
    private val _movements = MutableStateFlow<List<Movement>>(emptyList())
    val movements: StateFlow<List<Movement>> = _movements

    // Todos los movimientos (MovementsScreen global)
    private val _allMovements = MutableStateFlow<List<Movement>>(emptyList())
    val allMovements: StateFlow<List<Movement>> = _allMovements

    // Rango de fechas seleccionado para el filtro global
    private val _dateFrom = MutableStateFlow<Date?>(null)
    val dateFrom: StateFlow<Date?> = _dateFrom

    private val _dateTo = MutableStateFlow<Date?>(null)
    val dateTo: StateFlow<Date?> = _dateTo

    init { loadProducts() }

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

    fun loadAllMovements() {
        viewModelScope.launch {
            try {
                _allMovements.value = repository.getAllMovements()
            } catch (_: Exception) {
                _allMovements.value = emptyList()
            }
        }
    }

    fun setDateFrom(date: Date?) {
        _dateFrom.value = date
    }

    fun setDateTo(date: Date?) {
        _dateTo.value = date
    }

    fun clearDateFilter() {
        _dateFrom.value = null
        _dateTo.value   = null
    }

    fun addProduct(product: Product, imageUri: Uri? = null) {
        val uid = authRepository.getCurrentUserId() ?: ""
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val docId = repository.addProductAndGetId(product.copy(createdBy = uid))
                if (imageUri != null) {
                    val url = imageRepository.uploadProductImage(docId, imageUri)
                    repository.updateProduct(
                        product.copy(id = docId, createdBy = uid, imageUrl = url)
                    )
                }
                loadProducts()
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar producto")
            }
        }
    }

    fun updateProduct(product: Product, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val finalProduct = if (imageUri != null) {
                    val url = imageRepository.uploadProductImage(product.id, imageUri)
                    product.copy(imageUrl = url)
                } else {
                    product
                }
                repository.updateProduct(finalProduct)
                loadProducts()
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar producto")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val imageUrl = _products.value.find { it.id == productId }?.imageUrl ?: ""
                imageRepository.deleteProductImage(imageUrl)
                repository.deleteProduct(productId)
                loadProducts()
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar producto")
            }
        }
    }

    fun registerMovement(productId: String, type: MovementType, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val product  = _products.value.find { it.id == productId }
                    ?: throw Exception("Producto no encontrado")
                val uid      = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""

                val movement = Movement(
                    productId   = productId,
                    productName = product.name,
                    quantity    = quantity,
                    type        = type,
                    date        = Timestamp.now(),
                    userId      = uid,
                    userName    = userName
                )
                repository.registerMovement(movement)
                loadProducts()
                loadMovements(productId)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al registrar movimiento")
            }
        }
    }

    // Historial de un producto específico (MovementDetailScreen)
    fun loadMovements(productId: String) {
        viewModelScope.launch {
            try {
                _movements.value = repository.getMovementsForProduct(productId)
            } catch (_: Exception) {
                _movements.value = emptyList()
            }
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }
}