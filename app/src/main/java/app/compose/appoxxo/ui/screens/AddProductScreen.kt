package app.compose.appoxxo.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.ImagePickerSection
import app.compose.appoxxo.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductViewModel,
    onProductSaved: () -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var code     by remember { mutableStateOf("") }
    var price    by remember { mutableStateOf("") }
    var stock    by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onProductSaved() }
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
                title = {
                    Text("Agregar producto", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onProductSaved) {
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
                selectedUri   = imageUri,
                onImageSelected = { imageUri = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ── Datos del producto ────────────────────────────────
            Text(
                "Información del producto",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre del producto"
            )
            AppTextField(
                value         = code,
                onValueChange = { code = it },
                label         = "Código",
                singleLine    = false
            )
            AppTextField(
                value         = price,
                onValueChange = { price = it },
                label         = "Precio",
                isError       = price.isNotEmpty() && price.toDoubleOrNull() == null,
                errorMessage  = "Ingresa un número válido"
            )
            AppTextField(
                value         = stock,
                onValueChange = { stock = it },
                label         = "Stock inicial",
                isError       = stock.isNotEmpty() && stock.toIntOrNull() == null,
                errorMessage  = "Ingresa un número entero"
            )

            Spacer(modifier = Modifier.height(4.dp))

            val isFormValid = name.isNotBlank()
                    && price.toDoubleOrNull() != null
                    && stock.toIntOrNull() != null

            AppButton(
                text           = if (imageUri != null) "Guardar y subir imagen" else "Guardar producto",
                onClick        = {
                    viewModel.addProduct(
                        product  = Product(
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

            // Nota sobre almacenamiento
            if (imageUri != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_inventory),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "La imagen se subirá a Firebase Storage. " +
                                    "El enlace se guardará en Realtime Database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}