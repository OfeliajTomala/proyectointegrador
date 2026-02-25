package app.compose.appoxxo.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.ProductCategory
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.*
import app.compose.appoxxo.viewmodel.ProductViewModel
import androidx.compose.material3.ExposedDropdownMenuAnchorType


// ─── AddProductScreen ──────────────────────────────────────────────────────────

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
    var category         by remember { mutableStateOf(ProductCategory.OTROS) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    fun isValidPrice(value: String): Boolean {
        if (value.isEmpty()) return true
        return value.matches(Regex("""^\d{0,10}([.,]\d{0,2})?$"""))
    }

    fun parsePrice(value: String): Double {
        if (value.isEmpty()) return 0.0
        val normalized = value.replace(",", ".")
        return when {
            normalized.startsWith(".") -> ("0$normalized").toDouble()
            else                       -> normalized.toDouble()
        }
    }

    fun isValidStock(value: String): Boolean {
        if (value.isEmpty()) return true
        return value.matches(Regex("""^\d+$"""))
    }

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

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen
            ImagePickerSection(
                selectedUri     = imageUri,
                onImageSelected = { imageUri = it }
            )

            HorizontalDivider(
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(
                "Información del producto",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize   = 12.sp
            )

            AppTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Nombre del producto",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_product),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            ExposedDropdownMenuBox(
                expanded         = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value         = category.label,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Categoría") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    colors        = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded         = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ProductCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.label) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }


            AppTextField(
                value         = code,
                onValueChange = { if (it.length <= 10) code = it.uppercase() },
                label         = "Código",
                isError       = code.isNotEmpty() && code.length < 5,
                errorMessage  = "El código debe tener entre 5 y 10 caracteres",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_code),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            AppTextField(
                value           = price,
                onValueChange   = { if (isValidPrice(it)) price = it },
                label           = "Precio",
                isError         = price.isNotEmpty() && parsePrice(price) < 0,
                errorMessage    = "Ingresa un precio válido",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_attach_money),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            AppTextField(
                value           = stock,
                onValueChange   = { if (isValidStock(it)) stock = it },
                label           = "Stock inicial",
                isError         = stock.isNotEmpty() && stock.toIntOrNull() == null,
                errorMessage    = "Ingresa un número entero",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_inventory),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            val isFormValid = name.isNotBlank()
                    && parsePrice(price) >= 0
                    && stock.toIntOrNull() != null
                    && (code.isEmpty() || code.length >= 5)

            AppButton(
                text           = "Guardar producto",
                onClick        = {
                    viewModel.addProduct(
                        product  = Product(
                            name   = name.trim(),
                            codigo = code.trim(),
                            price  = parsePrice(price),
                            stock  = stock.toInt(),
                            category = category

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

