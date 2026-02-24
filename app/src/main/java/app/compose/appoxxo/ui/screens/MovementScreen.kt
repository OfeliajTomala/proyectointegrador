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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(
    viewModel: ProductViewModel,
    canDelete: Boolean = false
) {
    val allMovements     by viewModel.allMovements.collectAsState()
    val deletedMovements by viewModel.deletedMovements.collectAsState()
    val dateFrom         by viewModel.dateFrom.collectAsState()
    val dateTo           by viewModel.dateTo.collectAsState()
    val uiState          by viewModel.uiState.collectAsState()

    var selectedFilter    by remember { mutableStateOf<MovementType?>(null) }
    var searchQuery       by remember { mutableStateOf("") }
    var showDeleted       by remember { mutableStateOf(false) }
    val showDateFromPicker = remember { mutableStateOf(false) }
    val showDateToPicker   = remember { mutableStateOf(false) }
    val movementToDelete   = remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es"))
    }

    // Carga inicial
    LaunchedEffect(Unit) {
        viewModel.loadAllMovements()
        viewModel.loadDeletedMovements()
    }

    // Cuando el ViewModel termina una operación (Success),
    // recarga la lista correcta según la pestaña activa.
    // Se usa snapshotFlow para evitar loops: solo reacciona cuando
    // uiState cambia a Success, no cuando vuelve a Idle tras resetState()
    LaunchedEffect(Unit) {
        snapshotFlow { uiState }
            .collect { state ->
                if (state is UiState.Success) {
                    if (showDeleted) {
                        viewModel.loadDeletedMovements()
                    } else {
                        viewModel.loadAllMovements()
                    }
                    viewModel.resetState()
                }
            }
    }

    // Diálogo confirmación eliminación
    movementToDelete.value?.let { movementId ->
        AlertDialog(
            onDismissRequest = { movementToDelete.value = null },
            shape            = RoundedCornerShape(20.dp),
            title = {
                Text("Eliminar movimiento", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "¿Estás seguro que deseas eliminar este movimiento? " +
                            "Podrás verlo en la pestaña de eliminados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMovement(movementId)
                        movementToDelete.value = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
            searchQuery.isBlank() ||
                    m.productName.contains(searchQuery, ignoreCase = true)
        }
    } else {
        allMovements.filter { m ->
            (selectedFilter == null || m.type == selectedFilter) &&
                    (searchQuery.isBlank() || m.productName.contains(searchQuery, ignoreCase = true)) &&
                    (dateFrom == null || !m.date.toDate().before(dateFrom)) &&
                    (dateTo   == null || !m.date.toDate().after(endOfDay(dateTo!!)))
        }
    }

    // Date pickers
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

    Column(modifier = Modifier.fillMaxSize()) {

        // Toggle Activos / Eliminados
        PrimaryTabRow(selectedTabIndex = if (showDeleted) 1 else 0) {
            Tab(
                selected = !showDeleted,
                onClick  = {
                    showDeleted = false
                    viewModel.loadAllMovements()
                },
                text = { Text("Activos") }
            )
            Tab(
                selected = showDeleted,
                onClick  = {
                    showDeleted = true
                    viewModel.loadDeletedMovements()
                },
                text = { Text("Eliminados") }
            )
        }

        // Filtros
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Buscar producto…") },
                leadingIcon   = {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_inventory),
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Limpiar",
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (!showDeleted) {
                Spacer(modifier = Modifier.height(10.dp))

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
                                painter            = painterResource(id = R.drawable.ic_arrow_downward),
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = selectedFilter == MovementType.SALIDA,
                        onClick  = { selectedFilter = MovementType.SALIDA },
                        label    = { Text("Salidas") },
                        leadingIcon = {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_arrow_upward),
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick        = { showDateFromPicker.value = true },
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text  = if (dateFrom != null) dateFormatter.format(dateFrom!!) else "Desde",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedButton(
                        onClick        = { showDateToPicker.value = true },
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text  = if (dateTo != null) dateFormatter.format(dateTo!!) else "Hasta",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (dateFrom != null || dateTo != null) {
                        IconButton(onClick = { viewModel.clearDateFilter() }) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Limpiar fechas",
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

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
        }

        HorizontalDivider()

        // Lista
        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "${filteredMovements.size} movimiento${if (filteredMovements.size != 1) "s" else ""}",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (filteredMovements.isEmpty()) {
                item {
                    AppEmptyState(
                        iconRes  = R.drawable.ic_list,
                        title    = if (showDeleted) "Sin movimientos eliminados"
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