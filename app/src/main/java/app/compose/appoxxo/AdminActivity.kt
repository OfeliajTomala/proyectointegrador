// ═══════════════════════════════════════════════════════
// AdminActivity.kt  — FIX #1: validación de rol real
// ═══════════════════════════════════════════════════════
package app.compose.appoxxo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.admin.navigation.AdminNavGraph
import app.compose.appoxxo.ui.theme.AppOxxoTheme
import app.compose.appoxxo.ui.theme.ThemeConfig
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdminActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = AppViewModelFactory(applicationContext)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // FIX #1: no confiar solo en el extra del Intent.
        // Esperamos a que isAuthReady sea true y luego validamos
        // el rol REAL desde Firestore (vía syncUser / reloadCurrentUser).
        lifecycleScope.launch {
            // Espera a que el ViewModel termine de cargar el usuario
            authViewModel.isAuthReady.first { it }

            authViewModel.syncUser()

            // Valida el rol real del usuario cargado desde Firestore
            val user = authViewModel.currentUser.value
            if (user == null || user.role != UserRole.ADMIN) {
                finish()
                return@launch
            }

            setContent {
                AppOxxoTheme(darkTheme = ThemeConfig.isDarkMode) {
                    val navController = rememberNavController()
                    AdminNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout()
                            val intent = android.content.Intent(
                                this@AdminActivity,
                                MainActivity::class.java
                            )
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}