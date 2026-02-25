package app.compose.appoxxo.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppButton
import app.compose.appoxxo.ui.components.AppOutlinedButton
import app.compose.appoxxo.ui.components.AppTextField
import app.compose.appoxxo.ui.components.PasswordTextField
import app.compose.appoxxo.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> { viewModel.resetState(); onLoginSuccess() }
            is UiState.Error   -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Acento visual superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 28.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // ── Marca ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_store),
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Bienvenido",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Inicia sesión para continuar",
                    style  = MaterialTheme.typography.bodyMedium,
                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Formulario ─────────────────────────────────────
                AppTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(14.dp))
                PasswordTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = "Contraseña"
                )

                Spacer(modifier = Modifier.height(28.dp))

                AppButton(
                    text           = "Iniciar sesión",
                    onClick        = { viewModel.login(email.trim(), password) },
                    modifier       = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primary,
                    isLoading      = uiState is UiState.Loading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divisor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        "  o continúa con  ",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            tint               = Color.Unspecified,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = onGoToRegister) {
                    Text(
                        "¿No tienes cuenta? ",
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        "Regístrate",
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}