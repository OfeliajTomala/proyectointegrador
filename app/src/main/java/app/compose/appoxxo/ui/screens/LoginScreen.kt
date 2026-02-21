package app.compose.appoxxo.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.PasswordTextField
import app.compose.appoxxo.viewmodel.AuthViewModel
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.components.AppOutlinedButton


@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onLoginSuccess() }
            is UiState.Error -> {
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
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico"
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
            )
            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = "Entrar",
                onClick = { viewModel.login(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                isLoading = uiState is UiState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Divisor "O"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "  O  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Google

            AppOutlinedButton(
                text     = "Continuar con Google",
                onClick  = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth(),
                color    = MaterialTheme.colorScheme.onSurface,
                enabled  = uiState !is UiState.Loading,
                leadingIcon = {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint               = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onGoToRegister) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
