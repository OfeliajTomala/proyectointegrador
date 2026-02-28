package app.compose.appoxxo.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.ProductCategory
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.viewmodel.ProductViewModel

// ─── AddMovementScreen ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovementScreen(
    viewModel: ProductViewModel,
    onMovementSaved: () -> Unit
) {
    val products  by viewModel.products.collectAsState()
    val uiState   by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Estado local ──────────────────────────────────────────────
    var selectedCategory     by remember { mutableStateOf<ProductCategory?>(null) }
    var categoryExpanded     by remember { mutableStateOf(false) }

    var selectedProduct      by remember { mutableStateOf<Product?>(null) }
    var productExpanded      by remember { mutableStateOf(false) }

    var movementType         by remember { mutableStateOf(MovementType.ENTRADA) }
    var quantity             by remember { mutableStateOf("") }
    var quantityError        by remember { mutableStateOf<String?>(null) }

    // ─── Productos filtrados según categoría elegida ───────────────
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == null) products
        else products.filter { it.category == selectedCategory }
    }

    // Limpiar producto seleccionado si ya no aparece en la lista filtrada
    LaunchedEffect(filteredProducts) {
        if (selectedProduct != null && selectedProduct !in filteredProducts) {
            selectedProduct = null
            quantity        = ""
            quantityError   = null
        }
    }

    // ─── Reaccionar al resultado ───────────────────────────────────
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                viewModel.resetState()
                onMovementSaved()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Sección: Seleccionar producto ──────────────────────
            Text(
                text       = "Seleccionar producto",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize   = 12.sp
            )

            // ── Dropdown 1: Categoría ──────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value         = selectedCategory?.label ?: "Todas las categorías",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Categoría") },
                    trailingIcon  = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    leadingIcon = {
                        Icon(
                            painter            = painterResource(R.drawable.ic_shopping_cart),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded         = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    // Opción "Todas"
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            selectedCategory = null
                            categoryExpanded = false
                        },
                        leadingIcon = {
                            if (selectedCategory == null) {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                    ProductCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text    = { Text(category.label) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            leadingIcon = {
                                if (selectedCategory == category) {
                                    Icon(
                                        painter            = painterResource(R.drawable.ic_check_circle),
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // ── Dropdown 2: Producto ───────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = productExpanded,
                onExpandedChange = {
                    if (filteredProducts.isNotEmpty()) productExpanded = it
                }
            ) {
                OutlinedTextField(
                    value         = selectedProduct?.name
                        ?: if (filteredProducts.isEmpty()) "Sin productos en esta categoría"
                        else "Selecciona un producto",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Producto") },
                    trailingIcon  = {
                        if (filteredProducts.isNotEmpty()) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded)
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter            = painterResource(R.drawable.ic_product),
                            contentDescription = null,
                            tint               = if (filteredProducts.isEmpty())
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    },
                    isError  = filteredProducts.isEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                if (filteredProducts.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded         = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        filteredProducts.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text       = product.name,
                                            fontWeight = FontWeight.Medium,
                                            maxLines   = 1,
                                            overflow   = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text      = stockLabel(product.stock),
                                            fontSize  = 11.sp,
                                            color     = stockColor(product.stock)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = product
                                    productExpanded = false
                                    quantity        = ""
                                    quantityError   = null
                                },
                                leadingIcon = {
                                    if (selectedProduct?.id == product.id) {
                                        Icon(
                                            painter            = painterResource(R.drawable.ic_check_circle),
                                            contentDescription = null,
                                            tint               = MaterialTheme.colorScheme.primary,
                                            modifier           = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ── Card resumen del producto seleccionado (animada) ───
            AnimatedVisibility(
                visible = selectedProduct != null,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                selectedProduct?.let { product ->
                    ProductSummaryCard(product = product)
                }
            }

            // ── Sección de movimiento (animada) ────────────────────
            AnimatedVisibility(
                visible = selectedProduct != null,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                selectedProduct?.let { product ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        HorizontalDivider(
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text       = "Tipo de movimiento",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize   = 12.sp
                        )

                        // Selector ENTRADA / SALIDA
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MovementType.entries.forEach { type ->
                                val isSelected = movementType == type
                                val (bgColor, textColor, iconRes) = when (type) {
                                    MovementType.ENTRADA if isSelected ->
                                        Triple(Color(0xFF2E7D32), Color.White, R.drawable.ic_arrow_downward)

                                    MovementType.SALIDA if isSelected ->
                                        Triple(Color(0xFFC62828), Color.White, R.drawable.ic_arrow_upward)

                                    else -> Triple(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        if (type == MovementType.ENTRADA) R.drawable.ic_arrow_downward
                                        else R.drawable.ic_arrow_upward
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .clickable { movementType = type }
                                        .padding(vertical = 14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter            = painterResource(iconRes),
                                        contentDescription = null,
                                        tint               = textColor,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text       = type.name.lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        color      = textColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 14.sp
                                    )
                                }
                            }
                        }

                        // Campo de cantidad
                        Column {
                            OutlinedTextField(
                                value         = quantity,
                                onValueChange = { quantity = it; quantityError = null },
                                label         = { Text("Cantidad") },
                                modifier      = Modifier.fillMaxWidth(),
                                isError       = quantityError != null,
                                singleLine    = true,
                                shape         = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon   = {
                                    Icon(
                                        painter            = painterResource(R.drawable.ic_inventory),
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    errorBorderColor     = MaterialTheme.colorScheme.error,
                                    focusedLabelColor    = MaterialTheme.colorScheme.primary
                                )
                            )
                            AnimatedVisibility(
                                visible = quantityError != null,
                                enter   = expandVertically(),
                                exit    = shrinkVertically()
                            ) {
                                Text(
                                    text     = quantityError ?: "",
                                    color    = MaterialTheme.colorScheme.error,
                                    style    = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }

                        // Botón registrar
                        AppButton(
                            text      = "Registrar ${movementType.name.lowercase()
                                .replaceFirstChar { it.uppercase() }}",
                            onClick   = {
                                val qty = quantity.toIntOrNull()
                                when {
                                    qty == null || qty <= 0 ->
                                        quantityError = "Ingresa una cantidad válida"
                                    movementType == MovementType.SALIDA && qty > product.stock ->
                                        quantityError = "Stock insuficiente (disponible: ${product.stock})"
                                    else -> viewModel.registerMovement(
                                        productId = product.id,
                                        type      = movementType,
                                        quantity  = qty
                                    )
                                }
                            },
                            modifier       = Modifier.fillMaxWidth(),
                            enabled        = quantity.isNotBlank() && uiState !is UiState.Loading,
                            isLoading      = uiState is UiState.Loading,
                            containerColor = if (movementType == MovementType.ENTRADA)
                                Color(0xFF2E7D32)
                            else Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

// ─── ProductSummaryCard ────────────────────────────────────────────────────────

@Composable
private fun ProductSummaryCard(product: Product) {
    Surface(
        shape          = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(stockColor(product.stock).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = product.stock.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = stockColor(product.stock)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text  = product.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "Stock actual: ${stockLabel(product.stock)}",
                    fontSize   = 11.sp,
                    color      = stockColor(product.stock),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Helpers ───────────────────────────────────────────────────────────────────

private fun stockLabel(stock: Int): String = when {
    stock == 0 -> "Sin stock"
    stock <= 5 -> "$stock unidades (bajo)"
    else       -> "$stock unidades"
}

@Composable
private fun stockColor(stock: Int): Color = when {
    stock == 0 -> Color(0xFFD32F2F)
    stock <= 5 -> Color(0xFFE65100)
    else       -> Color(0xFF2E7D32)
}