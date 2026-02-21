package app.compose.appoxxo.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.ImagePickerSection
import app.compose.appoxxo.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    viewModel: ProductViewModel,
    onProductUpdated: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val product   = products.find { it.id == productId }

    var name     by remember(product) { mutableStateOf(product?.name ?: "") }
    var code     by remember(product) { mutableStateOf(product?.codigo ?: "") }
    var price    by remember(product) { mutableStateOf(product?.price?.toString() ?: "") }
    var stock    by remember(product) { mutableStateOf(product?.stock?.toString() ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onProductUpdated() }
            is UiState.Error   -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onProductUpdated) {
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
        if (product == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Imagen ────────────────────────────────────────────
            ImagePickerSection(
                currentImageUrl = product.imageUrl,
                selectedUri     = imageUri,
                onImageSelected = { imageUri = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Información del producto",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                value = stock, onValueChange = { stock = it }, label = "Stock",
                isError = stock.isNotEmpty() && stock.toIntOrNull() == null,
                errorMessage = "Ingresa un número entero"
            )

            Spacer(modifier = Modifier.height(4.dp))

            val isFormValid = name.isNotBlank()
                    && price.toDoubleOrNull() != null
                    && stock.toIntOrNull() != null

            AppButton(
                text = if (imageUri != null) "Actualizar con nueva imagen" else "Actualizar producto",
                onClick = {
                    viewModel.updateProduct(
                        product = product.copy(
                            name   = name.trim(),
                            codigo = code.trim(),
                            price  = price.toDouble(),
                            stock  = stock.toInt()
                        ),
                        imageUri = imageUri
                    )
                },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = isFormValid,
                isLoading      = uiState is UiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}