package app.compose.appoxxo.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.PasswordTextField
import app.compose.appoxxo.ui.components.isPasswordStrong
import app.compose.appoxxo.viewmodel.AuthViewModel

// ─── Cambiar Nombre ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser       by viewModel.currentUser.collectAsState()
    var name              by remember { mutableStateOf(currentUser?.name ?: "") }
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onBack() }
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
                title = { Text("Cambiar nombre", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), "Volver")
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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        "Nombre actual: ${currentUser?.name ?: "—"}",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            AppTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Nuevo nombre"
            )

            AppButton(
                text           = "Guardar cambios",
                onClick        = { viewModel.updateName(name.trim()) },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = name.isNotBlank() && name.trim() != currentUser?.name,
                isLoading      = uiState is UiState.Loading
            )
        }
    }
}

// ─── Cambiar Correo ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmailScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser       by viewModel.currentUser.collectAsState()
    var email             by remember { mutableStateOf("") }
    var password          by remember { mutableStateOf("") }
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    val emailIsValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onBack() }
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
                title = { Text("Cambiar correo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), "Volver")
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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_edit),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        "Correo actual: ${currentUser?.email ?: "—"}",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            AppTextField(
                value           = email,
                onValueChange   = { email = it },
                label           = "Nuevo correo electrónico",
                isError         = !emailIsValid,
                errorMessage    = "Correo inválido",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            PasswordTextField(
                value         = password,
                onValueChange = { password = it },
                label         = "Contraseña actual"
            )

            // Aviso informativo
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_notifications),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                    Text(
                        "Se enviará un correo de verificación a la nueva dirección. El cambio se aplicará después de verificarlo.",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            val isFormValid = email.isNotBlank()
                    && emailIsValid
                    && email != currentUser?.email
                    && password.isNotBlank()

            AppButton(
                text           = "Guardar cambios",
                onClick        = { viewModel.updateEmail(email.trim(), password) },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = isFormValid,
                isLoading      = uiState is UiState.Loading
            )
        }
    }
}

// ─── Cambiar Contraseña ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var currentPassword  by remember { mutableStateOf("") }
    var newPassword      by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val passwordsMatch = confirmPassword.isEmpty() || confirmPassword == newPassword

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onBack() }
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
                title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), "Volver")
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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PasswordTextField(
                value         = currentPassword,
                onValueChange = { currentPassword = it },
                label         = "Contraseña actual"
            )

            PasswordTextField(
                value                 = newPassword,
                onValueChange         = { newPassword = it },
                label                 = "Nueva contraseña",
                showStrengthIndicator = true
            )

            PasswordTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = "Confirmar nueva contraseña",
                isError       = !passwordsMatch,
                errorMessage  = "Las contraseñas no coinciden"
            )

            val isFormValid = currentPassword.isNotBlank()
                    && isPasswordStrong(newPassword)
                    && newPassword == confirmPassword

            AppButton(
                text           = "Cambiar contraseña",
                onClick        = { viewModel.updatePassword(currentPassword, newPassword) },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = isFormValid,
                isLoading      = uiState is UiState.Loading
            )
        }
    }
}

// ─── Agregar Contraseña ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var newPassword      by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val passwordsMatch = confirmPassword.isEmpty() || confirmPassword == newPassword

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onBack() }
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
                title = { Text("Agregar contraseña", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), "Volver")
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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aviso Google
            Card(
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier              = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_google),
                        contentDescription = null,
                        tint               = Color.Unspecified,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        "Tu cuenta usa Google. Agrega una contraseña para también poder iniciar sesión con tu correo y contraseña.",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            PasswordTextField(
                value                 = newPassword,
                onValueChange         = { newPassword = it },
                label                 = "Nueva contraseña",
                showStrengthIndicator = true
            )

            PasswordTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = "Confirmar contraseña",
                isError       = !passwordsMatch,
                errorMessage  = "Las contraseñas no coinciden"
            )

            val isFormValid = isPasswordStrong(newPassword) && newPassword == confirmPassword

            AppButton(
                text           = "Agregar contraseña",
                onClick        = { viewModel.addPasswordToGoogleAccount(newPassword) },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = isFormValid,
                isLoading      = uiState is UiState.Loading
            )
        }
    }
}