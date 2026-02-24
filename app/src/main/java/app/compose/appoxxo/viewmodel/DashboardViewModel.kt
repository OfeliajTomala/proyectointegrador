package app.compose.appoxxo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.repository.ProductRepository
import app.compose.appoxxo.data.util.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalProducts: Int = 0,
    val totalDeletedProducts: Int = 0,
    val totalStock: Int = 0,
    val lowStockCount: Int = 0,
    val lowStockProducts: List<Product> = emptyList(),
    val totalInventoryValue: Double = 0.0,
    val recentProducts: List<Product> = emptyList(),
    val totalMovements: Int = 0,
    val totalDeletedMovements: Int = 0
)

class DashboardViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    init { loadStats() }

    // FIX #6 #8: carga todas las métricas en paralelo para el dashboard completo
    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                coroutineScope {
                    val activeD    = async { repository.getProducts() }
                    val deletedD   = async { repository.getDeletedProducts() }
                    val movD       = async { repository.getAllMovements() }
                    val delMovD    = async { repository.getDeletedMovements() }

                    val active     = activeD.await()
                    val deleted    = deletedD.await()
                    val movements  = movD.await()
                    val deletedMov = delMovD.await()

                    val lowStock = active.filter { it.stock <= 5 }

                    _stats.value = DashboardStats(
                        totalProducts         = active.size,
                        totalDeletedProducts  = deleted.size,
                        totalStock            = active.sumOf { it.stock },
                        lowStockCount         = lowStock.size,
                        lowStockProducts      = lowStock,
                        totalInventoryValue   = active.sumOf { it.price * it.stock },
                        recentProducts        = active.takeLast(5).reversed(),
                        totalMovements        = movements.size,
                        totalDeletedMovements = deletedMov.size
                    )
                }
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al cargar estadísticas")
            }
        }
    }
}