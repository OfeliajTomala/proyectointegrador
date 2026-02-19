package app.compose.appoxxo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.compose.appoxxo.ui.navigation.MainScreen
import app.compose.appoxxo.ui.theme.AppOxxoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppOxxoTheme(
                darkTheme = false,   // opcional (fuerza modo claro)
                dynamicColor = false // usa tus colores personalizados
            ) {
                MainScreen()
            }
        }
    }
}
