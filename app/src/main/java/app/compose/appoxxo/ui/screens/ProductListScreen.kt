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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppConfirmDialog
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.ui.components.ProductCard
import app.compose.appoxxo.viewmodel.AuthViewModel
import app.compose.appoxxo.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onViewMovements: (String) -> Unit
) {
    val products        by viewModel.products.collectAsState()
    val deletedProducts by viewModel.deletedProducts.collectAsState()
    val uiState         by viewModel.uiState.collectAsState()
    val currentUser     by authViewModel.currentUser.collectAsState()

    var showDeleted      by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val productToDelete   = remember { mutableStateOf<Product?>(null) }
    val productToRestore  = remember { mutableStateOf<Product?>(null) }

    val canEdit = currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.ENCARGADO

    LaunchedEffect(showDeleted) {
        if (showDeleted) viewModel.loadDeletedProducts()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            is UiState.Success -> {
                if (showDeleted) viewModel.loadDeletedProducts()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    productToDelete.value?.let { product ->
        AppConfirmDialog(
            title        = "Eliminar producto",
            message      = "¿Estás seguro que deseas eliminar \"${product.name}\"?",
            confirmLabel = "Eliminar",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm    = { viewModel.deleteProduct(product.id); productToDelete.value = null },
            onDismiss    = { productToDelete.value = null }
        )
    }

    productToRestore.value?.let { product ->
        AppConfirmDialog(
            title        = "Restaurar producto",
            message      = "¿Deseas restaurar \"${product.name}\"? Volverá a estar activo con su stock e historial.",
            confirmLabel = "Restaurar",
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm    = { viewModel.restoreProduct(product.id); productToRestore.value = null },
            onDismiss    = { productToRestore.value = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (canEdit && !showDeleted) {
                FloatingActionButton(
                    onClick        = onAddProduct,
                    shape          = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    modifier       = Modifier.size(58.dp)
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Agregar producto",
                        modifier           = Modifier.size(26.dp)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // ── Tabs ──────────────────────────────────────────────
            PrimaryTabRow(
                selectedTabIndex = if (showDeleted) 1 else 0,
                containerColor   = MaterialTheme.colorScheme.surface,
                contentColor     = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = !showDeleted,
                    onClick  = { showDeleted = false },
                    text     = { Text("Activos", fontSize = 14.sp) }
                )
                if (canEdit) {
                    Tab(
                        selected = showDeleted,
                        onClick  = { showDeleted = true },
                        text     = { Text("Eliminados", fontSize = 14.sp) }
                    )
                }
            }

            // ── Productos activos ─────────────────────────────────
            if (!showDeleted) {
                when {
                    uiState is UiState.Loading && products.isEmpty() -> {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    products.isEmpty() -> {
                        AppEmptyState(
                            iconRes  = R.drawable.ic_inventory,
                            title    = "Sin productos aún",
                            subtitle = if (canEdit) "Agrega uno con el botón +"
                            else "No hay productos registrados",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                top    = 10.dp,
                                bottom = 88.dp,
                                start  = 16.dp,
                                end    = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "${products.size} producto${if (products.size != 1) "s" else ""}",
                                    style    = MaterialTheme.typography.labelLarge,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    fontSize = 12.sp
                                )
                            }
                            items(products, key = { it.id }) { product ->
                                ProductCard(
                                    product         = product,
                                    role            = currentUser?.role,
                                    onEdit          = { onEditProduct(product.id) },
                                    onDelete        = { productToDelete.value = product },
                                    onViewMovements = { onViewMovements(product.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Productos eliminados ───────────────────────────
                when {
                    uiState is UiState.Loading && deletedProducts.isEmpty() -> {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    deletedProducts.isEmpty() -> {
                        AppEmptyState(
                            iconRes  = R.drawable.ic_inventory,
                            title    = "Sin productos eliminados",
                            subtitle = "Los productos eliminados aparecerán aquí",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                top    = 10.dp,
                                bottom = 24.dp,
                                start  = 16.dp,
                                end    = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "${deletedProducts.size} producto${if (deletedProducts.size != 1) "s" else ""} eliminados",
                                    style    = MaterialTheme.typography.labelLarge,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    fontSize = 12.sp
                                )
                            }
                            items(deletedProducts, key = { it.id }) { product ->
                                DeletedProductCard(
                                    product   = product,
                                    onRestore = { productToRestore.value = product }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletedProductCard(
    product: Product,
    onRestore: () -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontSize   = 15.sp
                )
                if (product.codigo.isNotBlank()) {
                    Text(
                        text  = "Código: ${product.codigo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text  = "Stock: ${product.stock}  •  $${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (product.deletedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = "Eliminado: ${dateFormatter.format(product.deletedAt.toDate())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
                if (product.deletedBy.isNotBlank()) {
                    Text(
                        text  = "Por: ${product.deletedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick  = onRestore,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_restart),
                    contentDescription = "Restaurar producto",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    }
}