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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onViewMovements: (String) -> Unit
) {
    val products         by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val deletedProducts  by viewModel.deletedProducts.collectAsState()
    val uiState          by viewModel.uiState.collectAsState()
    val currentUser      by authViewModel.currentUser.collectAsState()
    val searchQuery      by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showDeleted          by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val snackbarHostState  = remember { SnackbarHostState() }
    val productToDelete    = remember { mutableStateOf<Product?>(null) }
    val productToRestore   = remember { mutableStateOf<Product?>(null) }

    val canEdit = currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.ENCARGADO

    val availableCategories = remember(products) {
        products.map { it.category }.distinct().sortedBy { it.label }
    }

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

            // ── Search & Filter bar (solo pestaña activos) ────────
            if (!showDeleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("Buscar por nombre o código…", fontSize = 14.sp) },
                        leadingIcon   = {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Buscar",
                                modifier           = Modifier.size(20.dp),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon  = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        painter            = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Limpiar búsqueda",
                                        modifier           = Modifier.size(18.dp),
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    if (availableCategories.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded         = showCategoryDropdown,
                            onExpandedChange = { showCategoryDropdown = it },
                            modifier         = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value         = selectedCategory?.label ?: "Todas las categorías",
                                onValueChange = {},
                                readOnly      = true,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                                leadingIcon   = {
                                    Icon(
                                        painter            = painterResource(id = R.drawable.ic_category),
                                        contentDescription = "Categoría",
                                        modifier           = Modifier.size(20.dp),
                                        tint               = if (selectedCategory != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon  = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedCategory != null) {
                                            IconButton(
                                                onClick  = { viewModel.setSelectedCategory(null) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    painter            = painterResource(id = R.drawable.ic_close),
                                                    contentDescription = "Limpiar filtro",
                                                    modifier           = Modifier.size(16.dp),
                                                    tint               = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                                    }
                                },
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (selectedCategory != null)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                            )

                            ExposedDropdownMenu(
                                expanded         = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text    = {
                                        Text(
                                            "Todas las categorías",
                                            fontSize   = 14.sp,
                                            fontWeight = if (selectedCategory == null) FontWeight.SemiBold else FontWeight.Normal,
                                            color      = if (selectedCategory == null)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSelectedCategory(null)
                                        showCategoryDropdown = false
                                    }
                                )
                                HorizontalDivider()
                                availableCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text    = {
                                            Text(
                                                category.label,
                                                fontSize   = 14.sp,
                                                fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal,
                                                color      = if (selectedCategory == category)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSelectedCategory(category)
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                    filteredProducts.isEmpty() -> {
                        AppEmptyState(
                            iconRes  = R.drawable.ic_inventory,
                            title    = if (products.isEmpty()) "Sin productos aún" else "Sin resultados",
                            subtitle = when {
                                products.isEmpty() && canEdit -> "Agrega uno con el botón +"
                                products.isEmpty()            -> "No hay productos registrados"
                                else                          -> "Intenta con otro término o categoría"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                top    = 4.dp,
                                bottom = 88.dp,
                                start  = 16.dp,
                                end    = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                val hasFilters = selectedCategory != null || searchQuery.isNotBlank()
                                Text(
                                    text = if (hasFilters)
                                        "${filteredProducts.size} de ${products.size} producto${if (products.size != 1) "s" else ""}"
                                    else
                                        "${products.size} producto${if (products.size != 1) "s" else ""}",
                                    style    = MaterialTheme.typography.labelLarge,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    fontSize = 12.sp
                                )
                            }
                            items(filteredProducts, key = { it.id }) { product ->
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
                        text     = "Código: ${product.codigo}",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text     = "Stock: ${product.stock}  •  $${"%.2f".format(product.price)}",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (product.deletedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = "Eliminado: ${dateFormatter.format(product.deletedAt.toDate())}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
                if (product.deletedBy.isNotBlank()) {
                    Text(
                        text     = "Por: ${product.deletedBy}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
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