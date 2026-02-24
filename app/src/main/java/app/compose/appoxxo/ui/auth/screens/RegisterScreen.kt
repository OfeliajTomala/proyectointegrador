package app.compose.appoxxo.ui.auth.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.*
import app.compose.appoxxo.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onRegisterSuccess() }
            is UiState.Error   -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Crear cuenta",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Completa los datos para registrarte",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            AppTextField(
                value         = name,
                onValueChange = { name = it },
                label         = "Nombre completo"
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "Correo electrónico"
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Contraseña con indicador de fortaleza
            PasswordTextField(
                value                 = password,
                onValueChange         = { password = it },
                label                 = "Contraseña",
                showStrengthIndicator = true          // ← activa el indicador
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Confirmar contraseña (sin indicador)
            PasswordTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = "Confirmar contraseña",
                isError       = confirmPassword.isNotEmpty() && confirmPassword != password,
                errorMessage  = "Las contraseñas no coinciden"
            )

            Spacer(modifier = Modifier.height(24.dp))

            val isFormValid = name.isNotBlank() &&
                    email.isNotBlank() &&
                    isPasswordStrong(password) &&
                    password == confirmPassword

            AppButton(
                text           = "Registrarse",
                onClick        = { viewModel.register(email.trim(), password, name.trim()) },
                modifier       = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled        = isFormValid,
                isLoading      = uiState is UiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "  O  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick  = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                enabled  = uiState !is UiState.Loading
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    tint               = Color.Unspecified,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Continuar con Google", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onGoToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}