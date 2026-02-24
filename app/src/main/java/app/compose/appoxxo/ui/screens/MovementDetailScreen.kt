package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.ui.components.DeletedMovementCard
import app.compose.appoxxo.ui.components.MovementCard
import app.compose.appoxxo.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDetailScreen(
    productId: String,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    canDelete: Boolean = false
) {
    val products         by viewModel.products.collectAsState()
    val movements        by viewModel.movements.collectAsState()
    val deletedMovements by viewModel.deletedMovementsForProduct.collectAsState()
    val uiState          by viewModel.uiState.collectAsState()
    val product           = products.find { it.id == productId }

    var quantity         by remember { mutableStateOf("") }
    var selectedType     by remember { mutableStateOf(MovementType.ENTRADA) }
    var showDeleted      by remember { mutableStateOf(false) }
    val movementToDelete  = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        viewModel.loadMovements(productId)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState }
            .collect { state ->
                when (state) {
                    is UiState.Success -> {
                        quantity = ""
                        if (showDeleted) viewModel.loadDeletedMovementsForProduct(productId)
                        else viewModel.loadMovements(productId)
                        viewModel.resetState()
                    }
                    is UiState.Error -> {
                        snackbarHostState.showSnackbar(state.message)
                        viewModel.resetState()
                    }
                    else -> {}
                }
            }
    }

    // Diálogo confirmación
    movementToDelete.value?.let { movementId ->
        AlertDialog(
            onDismissRequest = { movementToDelete.value = null },
            shape            = RoundedCornerShape(20.dp),
            title = {
                Text("Eliminar movimiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Estás seguro? Podrás verlo en la pestaña Eliminados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMovement(movementId, productId); movementToDelete.value = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { movementToDelete.value = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = product?.name ?: "Movimientos",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Info del producto ─────────────────────────────────
            item {
                product?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    it.name,
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontSize   = 17.sp
                                )
                                Text(
                                    "Cód: ${it.codigo.ifEmpty { it.id.take(8).uppercase() }}",
                                    style  = MaterialTheme.typography.bodySmall,
                                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "$${"%.2f".format(it.price)}",
                                    style      = MaterialTheme.typography.bodySmall,
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize   = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${it.stock}",
                                    style      = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = if (it.stock <= 5)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                    fontSize = 36.sp
                                )
                                Text(
                                    "en stock",
                                    style  = MaterialTheme.typography.labelSmall,
                                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Formulario registro ───────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Registrar movimiento",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )

                        // Selector de tipo
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MovementType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick  = { selectedType = type },
                                    label    = { Text(type.name, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (type == MovementType.ENTRADA)
                                                    R.drawable.ic_arrow_downward
                                                else
                                                    R.drawable.ic_arrow_upward
                                            ),
                                            contentDescription = null,
                                            modifier           = Modifier.size(15.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // Campo cantidad
                        OutlinedTextField(
                            value         = quantity,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                                    quantity = value
                                }
                            },
                            label           = { Text("Cantidad", fontSize = 14.sp) },
                            modifier        = Modifier.fillMaxWidth(),
                            shape           = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError         = quantity.isNotEmpty() && quantity.toIntOrNull() == null,
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            supportingText = if (quantity.isNotEmpty() && quantity.toIntOrNull() == null) {
                                { Text("Ingresa un número entero", fontSize = 11.sp) }
                            } else null
                        )

                        // Botón registrar
                        val qty = quantity.toIntOrNull() ?: 0
                        val accentColor = if (selectedType == MovementType.ENTRADA)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error

                        Button(
                            onClick  = {
                                viewModel.registerMovement(
                                    productId = productId,
                                    type      = selectedType,
                                    quantity  = qty
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape    = RoundedCornerShape(14.dp),
                            enabled  = qty > 0 && uiState !is UiState.Loading,
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            )
                        ) {
                            if (uiState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    color       = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Registrar ${selectedType.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Tabs Historial / Eliminados ───────────────────────
            item {
                PrimaryTabRow(
                    selectedTabIndex = if (showDeleted) 1 else 0,
                    containerColor   = MaterialTheme.colorScheme.surface,
                    contentColor     = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = !showDeleted,
                        onClick  = {
                            showDeleted = false
                            viewModel.loadMovements(productId)
                        },
                        text = { Text("Historial", fontSize = 14.sp) }
                    )
                    if (canDelete) {
                        Tab(
                            selected = showDeleted,
                            onClick  = {
                                showDeleted = true
                                viewModel.loadDeletedMovementsForProduct(productId)
                            },
                            text = { Text("Eliminados", fontSize = 14.sp) }
                        )
                    }
                }
            }

            // ── Lista de movimientos ──────────────────────────────
            val listaActual = if (showDeleted) deletedMovements else movements

            if (listaActual.isEmpty()) {
                item {
                    AppEmptyState(
                        iconRes  = R.drawable.ic_list,
                        title    = if (showDeleted) "Sin movimientos eliminados"
                        else "Sin movimientos aún",
                        subtitle = if (showDeleted) "No hay movimientos eliminados para este producto"
                        else "Registra una entrada o salida para verla aquí"
                    )
                }
            } else {
                items(listaActual, key = { it.id }) { movement ->
                    if (showDeleted) {
                        DeletedMovementCard(movement = movement)
                    } else {
                        MovementCard(
                            movement = movement,
                            onDelete = if (canDelete) {
                                { movementToDelete.value = movement.id }
                            } else null
                        )
                    }
                }
            }
        }
    }
}