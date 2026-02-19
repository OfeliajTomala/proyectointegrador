package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.viewmodel.ProductViewModel
import app.compose.appoxxo.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductViewModel,
    onProductSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onProductSaved() }
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
                title = { Text("Agregar producto") },
                navigationIcon = {
                    IconButton(onClick = onProductSaved) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre del producto")
            AppTextField(
                value = code, onValueChange = { code = it },
                label = "Código", singleLine = false
            )
            AppTextField(
                value = price, onValueChange = { price = it }, label = "Precio",
                isError = price.isNotEmpty() && price.toDoubleOrNull() == null,
                errorMessage = "Ingresa un número válido"
            )
            AppTextField(
                value = stock, onValueChange = { stock = it }, label = "Stock inicial",
                isError = stock.isNotEmpty() && stock.toIntOrNull() == null,
                errorMessage = "Ingresa un número entero"
            )
            Spacer(modifier = Modifier.height(8.dp))
            val isFormValid = name.isNotBlank()
                    && price.toDoubleOrNull() != null
                    && stock.toIntOrNull() != null
            AppButton(
                text = "Guardar producto",
                onClick = {
                    viewModel.addProduct(
                        Product(
                            name = name.trim(),
                            codigo = code.trim(),
                            price = price.toDouble(),
                            stock = stock.toInt()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled = isFormValid,
                isLoading = uiState is UiState.Loading
            )
        }
    }
}