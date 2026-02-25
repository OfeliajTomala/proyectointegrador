package app.compose.appoxxo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.ProductCategory
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ImageRepository
import app.compose.appoxxo.data.repository.ProductRepository
import app.compose.appoxxo.data.util.UiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _deletedProducts = MutableStateFlow<List<Product>>(emptyList())
    val deletedProducts: StateFlow<List<Product>> = _deletedProducts

    private val _movements = MutableStateFlow<List<Movement>>(emptyList())
    val movements: StateFlow<List<Movement>> = _movements

    private val _deletedMovementsForProduct = MutableStateFlow<List<Movement>>(emptyList())
    val deletedMovementsForProduct: StateFlow<List<Movement>> = _deletedMovementsForProduct

    private val _allMovements = MutableStateFlow<List<Movement>>(emptyList())
    val allMovements: StateFlow<List<Movement>> = _allMovements

    private val _deletedMovements = MutableStateFlow<List<Movement>>(emptyList())
    val deletedMovements: StateFlow<List<Movement>> = _deletedMovements

    private val _dateFrom = MutableStateFlow<Date?>(null)
    val dateFrom: StateFlow<Date?> = _dateFrom

    private val _dateTo = MutableStateFlow<Date?>(null)
    val dateTo: StateFlow<Date?> = _dateTo

    // ─── Búsqueda y filtro por categoría ─────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    val selectedCategory: StateFlow<ProductCategory?> = _selectedCategory

    val filteredProducts: StateFlow<List<Product>> =
        combine(_products, _searchQuery, _selectedCategory) { products, query, category ->
            products.filter { product ->
                val matchesSearch = query.isBlank() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.codigo.contains(query, ignoreCase = true)
                val matchesCategory = category == null || product.category == category
                matchesSearch && matchesCategory
            }
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String)            { _searchQuery.value      = query }
    fun setSelectedCategory(c: ProductCategory?) { _selectedCategory.value = c    }

    // ─────────────────────────────────────────────────────────────

    private var activeProductId: String = ""

    init { loadProducts() }

    // ─── Productos ────────────────────────────────────────────────

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _products.value = repository.getProducts()
                _uiState.value  = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    // ─── Agregar producto ─────────────────────────────────────────

    fun addProduct(product: Product, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val uid      = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""

                val docId = repository.addProductAndGetId(
                    product       = product.copy(createdBy = uid),
                    createdByName = userName,
                    createdById   = uid
                )

                if (imageUri != null) {
                    val url = imageRepository.uploadProductImage(docId, imageUri)
                    repository.updateImageUrl(docId, url)
                }

                if (product.stock > 0) {
                    val initialMovement = Movement(
                        productId   = docId,
                        productName = product.name,
                        quantity    = product.stock,
                        type        = MovementType.ENTRADA,
                        date        = Timestamp.now(),
                        userId      = uid,
                        userName    = userName,
                        isDeleted   = false
                    )
                    repository.saveMovementOnly(initialMovement)
                }

                _products.value     = repository.getProducts()
                _allMovements.value = repository.getAllMovements()
                _uiState.value      = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar producto")
            }
        }
    }

    // ─── Actualizar producto ──────────────────────────────────────

    fun updateProduct(product: Product, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val uid      = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""

                if (imageUri != null) {
                    val newUrl = imageRepository.uploadProductImage(product.id, imageUri)
                    val previousUrl = repository.updateProduct(
                        product       = product.copy(imageUrl = newUrl),
                        updatedByName = userName,
                        updatedById   = uid
                    )
                    if (previousUrl.isNotEmpty() && previousUrl != newUrl) {
                        runCatching { imageRepository.deleteProductImage(previousUrl) }
                    }
                } else {
                    repository.updateProduct(
                        product       = product,
                        updatedByName = userName,
                        updatedById   = uid
                    )
                }

                _products.value = repository.getProducts()
                _uiState.value  = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar producto")
            }
        }
    }

    // ─── Soft delete producto ─────────────────────────────────────

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val uid      = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""
                val imageUrl = _products.value.find { it.id == productId }?.imageUrl ?: ""

                _products.value = _products.value.filter { it.id != productId }

                runCatching { imageRepository.deleteProductImage(imageUrl) }

                repository.deleteProduct(
                    productId     = productId,
                    deletedByName = userName,
                    deletedById   = uid
                )

                _products.value        = repository.getProducts()
                _deletedProducts.value = repository.getDeletedProducts()
                _allMovements.value    = repository.getAllMovements()

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _products.value = repository.getProducts()
                _uiState.value  = UiState.Error(e.message ?: "Error al eliminar producto")
            }
        }
    }

    // ─── Productos eliminados ─────────────────────────────────────

    fun loadDeletedProducts() {
        viewModelScope.launch {
            try {
                _deletedProducts.value = repository.getDeletedProducts()
            } catch (_: Exception) {
                _deletedProducts.value = emptyList()
            }
        }
    }

    // ─── Restaurar producto ───────────────────────────────────────

    fun restoreProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val restored = _deletedProducts.value.find { it.id == productId }
                if (restored != null) {
                    _deletedProducts.value = _deletedProducts.value.filter { it.id != productId }
                    _products.value = (_products.value + restored.copy(
                        isDeleted   = false,
                        deletedBy   = "",
                        deletedById = "",
                        deletedAt   = null
                    )).sortedBy { it.name }
                }

                repository.restoreProduct(productId)

                _products.value        = repository.getProducts()
                _deletedProducts.value = repository.getDeletedProducts()

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _products.value        = repository.getProducts()
                _deletedProducts.value = repository.getDeletedProducts()
                _uiState.value = UiState.Error(e.message ?: "Error al restaurar producto")
            }
        }
    }

    // ─── Movimientos globales ─────────────────────────────────────

    fun loadAllMovements() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _allMovements.value = repository.getAllMovements()
                _uiState.value      = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar movimientos")
            }
        }
    }

    fun loadDeletedMovements() {
        viewModelScope.launch {
            try {
                _deletedMovements.value = repository.getDeletedMovements()
            } catch (_: Exception) {
                _deletedMovements.value = emptyList()
            }
        }
    }

    // ─── Movimientos de un producto específico ────────────────────

    fun loadMovements(productId: String) {
        activeProductId = productId
        viewModelScope.launch {
            try {
                _movements.value = repository.getMovementsForProduct(productId)
            } catch (_: Exception) {
                _movements.value = emptyList()
            }
        }
    }

    fun loadDeletedMovementsForProduct(productId: String) {
        viewModelScope.launch {
            try {
                _deletedMovementsForProduct.value =
                    repository.getDeletedMovementsForProduct(productId)
            } catch (_: Exception) {
                _deletedMovementsForProduct.value = emptyList()
            }
        }
    }

    // ─── Registrar movimiento ─────────────────────────────────────

    fun registerMovement(productId: String, type: MovementType, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val product = _products.value.find { it.id == productId }
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
                    userName    = userName,
                    isDeleted   = false
                )

                repository.registerMovement(movement)

                _products.value     = repository.getProducts()
                _movements.value    = repository.getMovementsForProduct(productId)
                _allMovements.value = repository.getAllMovements()

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al registrar movimiento")
            }
        }
    }

    // ─── Soft delete movimiento ───────────────────────────────────

    fun deleteMovement(movementId: String, productId: String = "") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val uid      = authRepository.getCurrentUserId() ?: ""
                val userName = authRepository.getCurrentUserName() ?: ""

                val movimientoEliminado = _allMovements.value.find { it.id == movementId }
                    ?: _movements.value.find { it.id == movementId }

                _allMovements.value = _allMovements.value.filter { it.id != movementId }
                _movements.value    = _movements.value.filter { it.id != movementId }

                val affectedProductId = repository.deleteMovement(
                    movementId    = movementId,
                    deletedByName = userName,
                    deletedById   = uid
                )

                val pid = affectedProductId.ifEmpty {
                    productId.ifEmpty { movimientoEliminado?.productId ?: "" }
                }

                _allMovements.value     = repository.getAllMovements()
                _deletedMovements.value = repository.getDeletedMovements()

                if (pid.isNotEmpty()) {
                    _movements.value                  = repository.getMovementsForProduct(pid)
                    _deletedMovementsForProduct.value = repository.getDeletedMovementsForProduct(pid)
                    _products.value                   = repository.getProducts()
                }

                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _allMovements.value = repository.getAllMovements()
                if (activeProductId.isNotEmpty()) {
                    _movements.value = repository.getMovementsForProduct(activeProductId)
                }
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar movimiento")
            }
        }
    }

    // ─── Filtros de fecha ─────────────────────────────────────────

    fun setDateFrom(date: Date?) { _dateFrom.value = date }
    fun setDateTo(date: Date?)   { _dateTo.value   = date }
    fun clearDateFilter()        { _dateFrom.value = null; _dateTo.value = null }

    fun resetState() { _uiState.value = UiState.Idle }
}