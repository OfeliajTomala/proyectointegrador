package app.compose.appoxxo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.auth.navigation.AuthNavGraph
import app.compose.appoxxo.ui.theme.AppOxxoTheme
import app.compose.appoxxo.ui.theme.ThemeConfig
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // ─── ViewModels ───────────────────────────────────────────────────────────
    private lateinit var authViewModel: AuthViewModel

    // ─── Google Credential Manager ────────────────────────────────────────────
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa el ViewModel usando la factory personalizada
        val factory   = AppViewModelFactory(applicationContext)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Mantiene el splash nativo visible mientras el auth carga
        splashScreen.setKeepOnScreenCondition {
            !authViewModel.isAuthReady.value
        }

        // Inicializa el gestor de credenciales de Google
        credentialManager = CredentialManager.create(this)

        setContent {
            AppOxxoTheme(darkTheme = ThemeConfig.isDarkMode) {
                // MainActivity solo maneja Auth: Splash, Login, Register
                // Una vez autenticado redirige a la Activity del rol correspondiente
                AuthNavGraph(
                    authViewModel  = authViewModel,
                    onLoginSuccess = { role -> navigateToRoleActivity(role) },
                    onGoogleSignIn = { launchGoogleSignIn() }
                )
            }
        }
    }

    // ─── Navegación por Rol ───────────────────────────────────────────────────

    /**
     * Redirige a la Activity correspondiente según el rol del usuario.
     *
     * FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_CLEAR_TASK garantiza que:
     * - MainActivity queda fuera del back stack
     * - El usuario NO puede volver al Login presionando Atrás
     * - Solo existe una instancia de la Activity de destino
     */
    private fun navigateToRoleActivity(role: UserRole) {
        val targetClass = when (role) {
            UserRole.ADMIN     -> AdminActivity::class.java
            UserRole.ENCARGADO -> EncargadoActivity::class.java
            UserRole.CAJERO    -> CajeroActivity::class.java
        }

        val intent = Intent(this, targetClass).apply {
            // Limpia el back stack completamente
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasa el rol como validación extra en la Activity destino
            putExtra("USER_ROLE", role.name)
        }

        startActivity(intent)
    }

    // ─── Google Sign In ───────────────────────────────────────────────────────

    /**
     * Lanza el flujo de autenticación con Google usando Credential Manager.
     * setFilterByAuthorizedAccounts(false) permite seleccionar cualquier cuenta Google
     * no solo las previamente autorizadas.
     */
    private fun launchGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // context va primero, request va segundo
                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )

                // Verifica que la credencial recibida sea de tipo Google
                when (val credential = result.credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            // Extrae el idToken y lo pasa al ViewModel
                            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            authViewModel.loginWithGoogle(googleCredential.idToken)
                        } else {
                            // Tipo de credencial no reconocido
                            authViewModel.loginWithGoogle("")
                        }
                    }
                    else -> authViewModel.loginWithGoogle("")
                }

            } catch (_: GetCredentialException) {
                // El usuario canceló o hubo un error en el flujo de Google
                authViewModel.loginWithGoogle("")
            }
        }
    }
}