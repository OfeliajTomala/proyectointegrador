package app.compose.appoxxo.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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

@Composable
fun AddMovementScreen(
    viewModel: ProductViewModel,
    onMovementSaved: () -> Unit
) {
    val products     by viewModel.products.collectAsState()
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHost  = remember { SnackbarHostState() }

    // ─── Estado local ──────────────────────────────────────────────
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var selectedProduct  by remember { mutableStateOf<Product?>(null) }
    var movementType     by remember { mutableStateOf(MovementType.ENTRADA) }
    var quantity         by remember { mutableStateOf("") }
    var quantityError    by remember { mutableStateOf<String?>(null) }

    // ─── Productos filtrados por categoría ─────────────────────────
    val displayedProducts = remember(products, selectedCategory) {
        if (selectedCategory == null) products
        else products.filter { it.category == selectedCategory }
    }

    // ─── Limpiar selección si el producto filtrado desaparece ──────
    LaunchedEffect(displayedProducts) {
        if (selectedProduct != null && selectedProduct !in displayedProducts) {
            selectedProduct = null
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
                snackbarHost.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {

            // ── Sección 1: Chips de categoría ──────────────────────
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { cat ->
                    selectedCategory = if (selectedCategory == cat) null else cat
                }
            )

            HorizontalDivider(
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── Sección 2: Grid de productos ───────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (displayedProducts.isEmpty()) {
                    EmptyProductsMessage(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns             = GridCells.Fixed(2),
                        contentPadding      = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedProducts, key = { it.id }) { product ->
                            SelectableProductCard(
                                product    = product,
                                isSelected = product.id == selectedProduct?.id,
                                onClick    = {
                                    selectedProduct = if (selectedProduct?.id == product.id) null
                                    else product
                                    quantity      = ""
                                    quantityError = null
                                }
                            )
                        }
                    }
                }
            }

            // ── Sección 3: Panel inferior (animado) ────────────────
            AnimatedVisibility(
                visible = selectedProduct != null,
                enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                selectedProduct?.let { product ->
                    MovementInputPanel(
                        product       = product,
                        movementType  = movementType,
                        quantity      = quantity,
                        quantityError = quantityError,
                        isLoading     = uiState is UiState.Loading,
                        onTypeChange  = { movementType = it },
                        onQuantityChange = { value ->
                            quantity      = value
                            quantityError = null
                        },
                        onRegister = {
                            val qty = quantity.toIntOrNull()
                            when {
                                qty == null || qty <= 0 ->
                                    quantityError = "Ingresa una cantidad válida"
                                movementType == MovementType.SALIDA && qty > product.stock ->
                                    quantityError = "No hay suficiente stock (disponible: ${product.stock})"
                                else -> viewModel.registerMovement(
                                    productId = product.id,
                                    type      = movementType,
                                    quantity  = qty
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─── CategoryFilterRow ─────────────────────────────────────────────────────────

@Composable
private fun CategoryFilterRow(
    selectedCategory: ProductCategory?,
    onCategorySelected: (ProductCategory) -> Unit
) {
    LazyRow(
        contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ProductCategory.entries) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick  = { onCategorySelected(category) },
                label    = { Text(category.label, fontSize = 12.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ─── SelectableProductCard ─────────────────────────────────────────────────────

@Composable
private fun SelectableProductCard(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val bgColor     = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // Ícono de check cuando está seleccionado
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    text       = product.category.label,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.primary,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                AnimatedVisibility(visible = isSelected) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text       = product.name,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // Stock con chip de color
            StockIndicator(stock = product.stock)
        }
    }
}

// ─── StockIndicator ────────────────────────────────────────────────────────────

@Composable
private fun StockIndicator(stock: Int) {
    val (color, label) = when {
        stock == 0 -> Color(0xFFD32F2F) to "Sin stock"
        stock <= 5 -> Color(0xFFE65100) to "Stock: $stock"
        else       -> Color(0xFF2E7D32) to "Stock: $stock"
    }
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── MovementInputPanel ────────────────────────────────────────────────────────

@Composable
private fun MovementInputPanel(
    product: Product,
    movementType: MovementType,
    quantity: String,
    quantityError: String?,
    isLoading: Boolean,
    onTypeChange: (MovementType) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRegister: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Producto seleccionado
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_product),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = "Stock actual: ${product.stock} unidades",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

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
                            .clickable { onTypeChange(type) }
                            .padding(vertical = 12.dp),
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
                            text       = type.name.lowercase().replaceFirstChar { it.uppercase() },
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
                    onValueChange = onQuantityChange,
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
                text           = "Registrar ${movementType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                onClick        = onRegister,
                modifier       = Modifier.fillMaxWidth(),
                enabled        = quantity.isNotBlank() && !isLoading,
                isLoading      = isLoading,
                containerColor = if (movementType == MovementType.ENTRADA) Color(0xFF2E7D32)
                else Color(0xFFC62828)
            )
        }
    }
}

// ─── EmptyProductsMessage ──────────────────────────────────────────────────────

@Composable
private fun EmptyProductsMessage(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_shopping_cart),
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier           = Modifier.size(48.dp)
        )
        Text(
            text  = "No hay productos en esta categoría",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}