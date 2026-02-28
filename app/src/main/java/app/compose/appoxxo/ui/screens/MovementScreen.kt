package app.compose.appoxxo.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppDatePickerDialog
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.ui.components.DeletedMovementCard
import app.compose.appoxxo.ui.components.MovementCard
import app.compose.appoxxo.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(
    viewModel: ProductViewModel,
    canDelete: Boolean = false,
    onAddMovement: (() -> Unit)? = null
) {
    val allMovements by viewModel.allMovements.collectAsState()
    val deletedMovements by viewModel.deletedMovements.collectAsState()
    val dateFrom by viewModel.dateFrom.collectAsState()
    val dateTo by viewModel.dateTo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedFilter by remember { mutableStateOf<MovementType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleted by remember { mutableStateOf(false) }
    val showDateFromPicker = remember { mutableStateOf(false) }
    val showDateToPicker = remember { mutableStateOf(false) }
    val movementToDelete = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es"))
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllMovements()
        viewModel.loadDeletedMovements()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState }
            .collect { state ->
                if (state is UiState.Success) {
                    if (showDeleted) viewModel.loadDeletedMovements()
                    else viewModel.loadAllMovements()
                    viewModel.resetState()
                }
            }
    }

    // Diálogo confirmación eliminación
    movementToDelete.value?.let { movementId ->
        AlertDialog(
            onDismissRequest = { movementToDelete.value = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Eliminar movimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "¿Estás seguro? Podrás verlo en la pestaña Eliminados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMovement(movementId); movementToDelete.value = null
                }) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { movementToDelete.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Filtrado
    val filteredMovements = if (showDeleted) {
        deletedMovements.filter { m ->
            searchQuery.isBlank() || m.productName.contains(searchQuery, ignoreCase = true)
        }
    } else {
        allMovements.filter { m ->
            (selectedFilter == null || m.type == selectedFilter) &&
                    (searchQuery.isBlank() || m.productName.contains(
                        searchQuery,
                        ignoreCase = true
                    )) &&
                    (dateFrom == null || !m.date.toDate().before(dateFrom)) &&
                    (dateTo == null || !m.date.toDate().after(endOfDay(dateTo!!)))
        }
    }

    if (showDateFromPicker.value) {
        AppDatePickerDialog(
            initialDate = dateFrom,
            title = "Desde",
            onDateSelected = { viewModel.setDateFrom(it); showDateFromPicker.value = false },
            onDismiss = { showDateFromPicker.value = false }
        )
    }
    if (showDateToPicker.value) {
        AppDatePickerDialog(
            initialDate = dateTo,
            title = "Hasta",
            onDateSelected = { viewModel.setDateTo(it); showDateToPicker.value = false },
            onDismiss = { showDateToPicker.value = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            onAddMovement?.let { action ->
                FloatingActionButton(
                    onClick = action,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Agregar movimiento"
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            // ── Tabs Activos / Eliminados ─────────────────────────────
            PrimaryTabRow(
                selectedTabIndex = if (showDeleted) 1 else 0,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = !showDeleted,
                    onClick = { showDeleted = false; viewModel.loadAllMovements() },
                    text = { Text("Activos", fontSize = 14.sp) }
                )
                Tab(
                    selected = showDeleted,
                    onClick = { showDeleted = true; viewModel.loadDeletedMovements() },
                    text = { Text("Eliminados", fontSize = 14.sp) }
                )
            }

            // ── Filtros ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar producto…", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_inventory),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFD32F2F).copy(alpha = 0.1f)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(17.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
                )

                // Filtros de tipo (solo en pestaña activos)
                if (!showDeleted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Todos", fontSize = 13.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == MovementType.ENTRADA,
                            onClick = { selectedFilter = MovementType.ENTRADA },
                            label = { Text("Entradas", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_arrow_downward),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFF2E7D32),
                                selectedLeadingIconColor = Color(0xFF2E7D32),
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == MovementType.SALIDA,
                            onClick = { selectedFilter = MovementType.SALIDA },
                            label = { Text("Salidas", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_arrow_upward),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFFD32F2F),
                                selectedLeadingIconColor = Color(0xFFD32F2F),
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        )
                    }

                    // Filtro de fechas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDateFromPicker.value = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (dateFrom != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_notifications),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (dateFrom != null)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dateFrom != null) dateFormatter.format(dateFrom!!) else "Desde",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (dateFrom != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { showDateToPicker.value = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (dateTo != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_notifications),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (dateTo != null)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dateTo != null) dateFormatter.format(dateTo!!) else "Hasta",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (dateTo != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        if (dateFrom != null || dateTo != null) {
                            IconButton(
                                onClick = { viewModel.clearDateFilter() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Limpiar fechas",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Etiqueta de rango activo
                    if (dateFrom != null || dateTo != null) {
                        Text(
                            text = buildString {
                                append("Mostrando: ")
                                if (dateFrom != null) append("desde ${dateFormatter.format(dateFrom!!)} ")
                                if (dateTo != null) append("hasta ${dateFormatter.format(dateTo!!)}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ── Lista ─────────────────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "${filteredMovements.size} movimiento${if (filteredMovements.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                        fontSize = 12.sp
                    )
                }

                if (filteredMovements.isEmpty()) {
                    item {
                        AppEmptyState(
                            iconRes = R.drawable.ic_list,
                            title = if (showDeleted) "Sin movimientos eliminados"
                            else "Sin movimientos registrados",
                            subtitle = if (showDeleted) "No hay movimientos eliminados aún"
                            else "Los movimientos aparecerán aquí cuando\nregistres entradas o salidas."
                        )
                    }
                } else {
                    items(filteredMovements, key = { it.id }) { movement ->
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
}
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