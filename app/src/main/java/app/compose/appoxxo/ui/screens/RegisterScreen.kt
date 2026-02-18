package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.PasswordTextField
import app.compose.appoxxo.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                viewModel.resetState()
                onRegisterSuccess()
            }
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
            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre completo")
            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")
            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(value = password, onValueChange = { password = it }, label = "Contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                isPassword = true,
                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                errorMessage = "Las contraseñas no coinciden"
            )
            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = "Registrarse",
                onClick = {
                    if (password == confirmPassword) {
                        viewModel.register(email, password, name)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                enabled = password == confirmPassword,
                isLoading = uiState is UiState.Loading
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onGoToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}