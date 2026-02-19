package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.ServiceLocator
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.repository.ProductRepository
import app.compose.appoxxo.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class DashboardStats(
    val totalProducts: Int = 0,
    val totalStock: Int = 0,
    val lowStockCount: Int = 0,
    val lowStockProducts: List<Product> = emptyList(),
    val totalInventoryValue: Double = 0.0,
    val recentProducts: List<Product> = emptyList()
)

class DashboardViewModel(
    private val repository: ProductRepository = ServiceLocator.productRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val products = repository.getProducts()
                val lowStock = products.filter { it.stock <= 5 }
                _stats.value = DashboardStats(
                    totalProducts = products.size,
                    totalStock = products.sumOf { it.stock },
                    lowStockCount = lowStock.size,
                    lowStockProducts = lowStock,
                    totalInventoryValue = products.sumOf { it.price * it.stock },
                    recentProducts = products.takeLast(5).reversed()
                )
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar estadísticas")
            }
        }
    }
}