package app.compose.appoxxo.ui.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.auth.screens.LoginScreen
import app.compose.appoxxo.ui.auth.screens.RegisterScreen
import app.compose.appoxxo.ui.auth.screens.SplashScreen
import app.compose.appoxxo.viewmodel.AuthViewModel

@Composable
fun AuthNavGraph(
    authViewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState     by authViewModel.uiState.collectAsState()

    // FIX #2: la race condition ocurría porque currentUser puede llegar
    // DESPUÉS de que uiState cambie a Success.
    // Solución: observamos AMBOS estados y solo navegamos cuando
    // uiState == Success Y currentUser != null (con rol definido).
    LaunchedEffect(uiState, currentUser) {
        if (uiState is UiState.Success) {
            val role = currentUser?.role
            if (role != null) {
                onLoginSuccess(role)
            }
            // Si currentUser aún es null, el LaunchedEffect se re-ejecutará
            // en cuanto currentUser cambie (por la dependencia en la key)
        }
    }

    NavHost(
        navController    = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    currentUser?.role?.let { onLoginSuccess(it) }
                },
                isLoggedIn = currentUser != null,
            )
        }

        composable("login") {
            LoginScreen(
                viewModel      = authViewModel,
                onLoginSuccess = {
                    // Fallback por si el LaunchedEffect no alcanzó a dispararse
                    currentUser?.role?.let { role -> onLoginSuccess(role) }
                },
                onGoToRegister = { navController.navigate("register") },
                onGoogleSignIn = onGoogleSignIn
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel         = authViewModel,
                onRegisterSuccess = {
                    currentUser?.role?.let { role -> onLoginSuccess(role) }
                },
                onGoToLogin    = { navController.popBackStack() },
                onGoogleSignIn = onGoogleSignIn
            )
        }
    }
}