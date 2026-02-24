package app.compose.appoxxo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.cajero.navigation.CajeroNavGraph
import app.compose.appoxxo.ui.theme.AppOxxoTheme
import app.compose.appoxxo.ui.theme.ThemeConfig
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CajeroActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = AppViewModelFactory(applicationContext)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        lifecycleScope.launch {
            // Espera a que el ViewModel termine de cargar el usuario desde Firestore
            authViewModel.isAuthReady.first { it }

            // Recarga el usuario real desde Firestore (no confía en caché ni en el Intent extra)
            authViewModel.syncUser()

            // Válida el rol real — si no es CAJERO, cierra la Activity
            val user = authViewModel.currentUser.value
            if (user == null || user.role != UserRole.CAJERO) {
                finish()
                return@launch
            }

            setContent {
                AppOxxoTheme(darkTheme = ThemeConfig.isDarkMode) {
                    val navController = rememberNavController()
                    CajeroNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout()
                            val intent = android.content.Intent(
                                this@CajeroActivity,
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