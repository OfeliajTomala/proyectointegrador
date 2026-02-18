package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.viewmodel.ProductViewModel
import app.compose.appoxxo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementScreen(
    productId: String,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val product = products.find { it.id == productId }

    var quantity by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MovementType.ENTRADA) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                quantity = ""
                viewModel.resetState()
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
                title = { Text("Movimientos - ${product?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Stock actual
            product?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Stock actual: ${it.stock}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Selector de tipo de movimiento
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MovementType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                isError = quantity.isNotEmpty() && quantity.toIntOrNull() == null,
                supportingText = if (quantity.isNotEmpty() && quantity.toIntOrNull() == null) {
                    { Text("Ingresa un número entero") }
                } else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            val qty = quantity.toIntOrNull() ?: 0

            AppButton(
                text = "Registrar ${selectedType.name}",
                onClick = {
                    viewModel.registerMovement(
                        productId = productId,
                        type = selectedType,
                        quantity = qty
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (selectedType == MovementType.ENTRADA)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                enabled = qty > 0,
                isLoading = uiState is UiState.Loading
            )
        }
    }
}