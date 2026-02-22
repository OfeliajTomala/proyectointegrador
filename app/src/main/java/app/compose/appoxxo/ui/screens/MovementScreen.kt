package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(viewModel: ProductViewModel) {
    val allMovements by viewModel.allMovements.collectAsState()
    val dateFrom     by viewModel.dateFrom.collectAsState()
    val dateTo       by viewModel.dateTo.collectAsState()

    var selectedFilter     by remember { mutableStateOf<MovementType?>(null) }
    var searchQuery        by remember { mutableStateOf("") }
    val showDateFromPicker = remember { mutableStateOf(false) }
    val showDateToPicker   = remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es")) }

    // Cargar todos los movimientos al entrar
    LaunchedEffect(Unit) { viewModel.loadAllMovements() }

    // ── Date pickers ──────────────────────────────────────────────
    if (showDateFromPicker.value) {
        AppDatePickerDialog(
            initialDate    = dateFrom,
            title          = "Desde",
            onDateSelected = {
                viewModel.setDateFrom(it)
                showDateFromPicker.value = false
            },
            onDismiss = { showDateFromPicker.value = false }
        )
    }
    if (showDateToPicker.value) {
        AppDatePickerDialog(
            initialDate    = dateTo,
            title          = "Hasta",
            onDateSelected = {
                viewModel.setDateTo(it)
                showDateToPicker.value = false
            },
            onDismiss = { showDateToPicker.value = false }
        )
    }

    // ── Filtrado ──────────────────────────────────────────────────
    val filteredMovements = allMovements.filter { m ->
        (selectedFilter == null || m.type == selectedFilter) &&
                (searchQuery.isBlank() || m.productName.contains(searchQuery, ignoreCase = true)) &&
                (dateFrom == null || !m.date.toDate().before(dateFrom)) &&
                (dateTo   == null || !m.date.toDate().after(endOfDay(dateTo!!)))
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Buscador ──────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Buscar producto…") },
                leadingIcon   = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_inventory),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Limpiar",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Filtros de tipo ───────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick  = { selectedFilter = null },
                    label    = { Text("Todos") }
                )
                FilterChip(
                    selected = selectedFilter == MovementType.ENTRADA,
                    onClick  = { selectedFilter = MovementType.ENTRADA },
                    label    = { Text("Entradas") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_downward),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
                FilterChip(
                    selected = selectedFilter == MovementType.SALIDA,
                    onClick  = { selectedFilter = MovementType.SALIDA },
                    label    = { Text("Salidas") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_upward),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Filtros de fecha ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón DESDE
                OutlinedButton(
                    onClick = { showDateFromPicker.value = true },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = if (dateFrom != null) dateFormatter.format(dateFrom!!) else "Desde",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Botón HASTA
                OutlinedButton(
                    onClick = { showDateToPicker.value = true },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text  = if (dateTo != null) dateFormatter.format(dateTo!!) else "Hasta",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Botón limpiar fechas (solo visible si hay algún filtro activo)
                if (dateFrom != null || dateTo != null) {
                    IconButton(onClick = { viewModel.clearDateFilter() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Limpiar fechas",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Indicador de rango activo
            if (dateFrom != null || dateTo != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = buildString {
                        append("Mostrando: ")
                        if (dateFrom != null) append("desde ${dateFormatter.format(dateFrom!!)} ")
                        if (dateTo   != null) append("hasta ${dateFormatter.format(dateTo!!)}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider()

        // ── Lista ─────────────────────────────────────────────────
        if (allMovements.isEmpty()) {
            AppEmptyState(
                iconRes  = R.drawable.ic_list,
                title    = "Sin movimientos registrados",
                subtitle = "Los movimientos aparecerán aquí cuando\nregistres entradas o salidas.",
                modifier = Modifier.fillMaxSize()
            )        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "${filteredMovements.size} movimiento${if (filteredMovements.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (filteredMovements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin resultados para los filtros aplicados",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredMovements, key = { it.id }) { movement ->
                        MovementCard(movement = movement)
                    }
                }
            }
        }
    }
}

// ─── Date Picker Dialog ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDatePickerDialog(
    initialDate: Date?,
    title: String,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time
            ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onDateSelected(Date(it)) }
            }) { Text(title) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = state)
    }
}

// ─── MovementCard ─────────────────────────────────────────────────────────────
@Composable
fun MovementCard(movement: Movement) {
    val isEntrada   = movement.type == MovementType.ENTRADA
    val accentColor = if (isEntrada)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    val dateStr = remember(movement.date) {
        try {
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("es")).format(movement.date.toDate())
        } catch (_: Exception) { "—" }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEntrada) R.drawable.ic_arrow_downward
                        else R.drawable.ic_arrow_upward
                    ),
                    contentDescription = null,
                    tint     = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (movement.userName.isNotEmpty()) {
                    Text(
                        "Por: ${movement.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isEntrada) "+" else "-"}${movement.quantity}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
                Text(
                    if (isEntrada) "ENTRADA" else "SALIDA",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
        }
    }
}

// ─── MovementDetailScreen ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDetailScreen(
    productId: String,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val products  by viewModel.products.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val product    = products.find { it.id == productId }

    var quantity     by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MovementType.ENTRADA) }
    val uiState      by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) { viewModel.loadMovements(productId) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                quantity = ""
                viewModel.resetState()
                viewModel.loadMovements(productId)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Movimientos — ${product?.name ?: ""}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
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
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Info del producto
            item {
                product?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(it.name, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Código: ${it.id.take(8).uppercase()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "$${"%.2f".format(it.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${it.stock}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (it.stock <= 5)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "en stock",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Formulario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Registrar movimiento",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MovementType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick  = { selectedType = type },
                                    label    = { Text(type.name) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (type == MovementType.ENTRADA)
                                                    R.drawable.ic_arrow_downward
                                                else R.drawable.ic_arrow_upward
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        OutlinedTextField(
                            value         = quantity,
                            onValueChange = { value ->
                                // Solo acepta números enteros positivos
                                if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                                    quantity = value
                                }
                            },
                            label         = { Text("Cantidad") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError       = quantity.isNotEmpty() && quantity.toIntOrNull() == null,
                            supportingText = if (quantity.isNotEmpty() && quantity.toIntOrNull() == null) {
                                { Text("Ingresa un número entero") }
                            } else null
                        )
                        val qty = quantity.toIntOrNull() ?: 0
                        Button(
                            onClick = {
                                viewModel.registerMovement(
                                    productId = productId,
                                    type      = selectedType,
                                    quantity  = qty
                                )
                            },
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(12.dp),
                            enabled   = qty > 0 && uiState !is UiState.Loading,
                            colors    = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == MovementType.ENTRADA)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        ) {
                            if (uiState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color    = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Registrar ${selectedType.name}")
                            }
                        }
                    }
                }
            }

            // Historial del producto
            item {
                Text(
                    "Historial",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (movements.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin movimientos aún",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(movements, key = { it.id }) { movement ->
                    MovementCard(movement = movement)
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun endOfDay(date: Date): Date {
    val cal = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.time
}