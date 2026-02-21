package app.compose.appoxxo.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.viewmodel.ProductViewModel
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.ui.components.ProductCard
import app.compose.appoxxo.viewmodel.AuthViewModel

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onViewMovements: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                shape   = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Agregar producto"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState is UiState.Loading && products.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            products.isEmpty() -> {
                AppEmptyState(
                    iconRes  = R.drawable.ic_inventory,
                    title    = "Sin productos aún",
                    subtitle = "Agrega uno con el botón +",
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top    = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp,
                        start  = 16.dp,
                        end    = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product         = product,
                            role            = currentUser?.role,
                            onEdit          = { onEditProduct(product.id) },
                            onDelete        = { viewModel.deleteProduct(product.id) },
                            onViewMovements = { onViewMovements(product.id) }
                        )
                    }
                }
            }
        }
    }
}