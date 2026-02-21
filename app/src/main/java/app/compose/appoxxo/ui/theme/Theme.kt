package app.compose.appoxxo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


// ─── Paletas adicionales seleccionables ──────────────────────────────────────
object AppPalettes {
    val RedYellow = AppColorPalette(
        name = "Rojo & Amarillo",
        primary = Color(0xFFD32F2F),
        secondary = Color(0xFFFBC02D),
        primaryDark = Color(0xFFFF6659),
        secondaryDark = Color(0xFFFFEB3B)
    )
    val BlueTeal = AppColorPalette(
        name = "Azul & Verde Agua",
        primary = Color(0xFF1565C0),
        secondary = Color(0xFF00897B),
        primaryDark = Color(0xFF5E92F3),
        secondaryDark = Color(0xFF4DB6AC)
    )
    val PurpleOrange = AppColorPalette(
        name = "Morado & Naranja",
        primary = Color(0xFF6A1B9A),
        secondary = Color(0xFFE65100),
        primaryDark = Color(0xFFCE93D8),
        secondaryDark = Color(0xFFFFAB91)
    )
    val GreenAmber = AppColorPalette(
        name = "Verde & Ámbar",
        primary = Color(0xFF2E7D32),
        secondary = Color(0xFFFF6F00),
        primaryDark = Color(0xFF81C784),
        secondaryDark = Color(0xFFFFCA28)
    )
    val IndigoRose = AppColorPalette(
        name = "Índigo & Rosa",
        primary = Color(0xFF283593),
        secondary = Color(0xFFC2185B),
        primaryDark = Color(0xFF7986CB),
        secondaryDark = Color(0xFFF48FB1)
    )

    val all = listOf(RedYellow, BlueTeal, PurpleOrange, GreenAmber, IndigoRose)
}

data class AppColorPalette(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val primaryDark: Color,
    val secondaryDark: Color
)

// ─── Estado global del tema ───────────────────────────────────────────────────
object ThemeConfig {
    var selectedPalette by mutableStateOf(AppPalettes.RedYellow)
    var isDarkMode by mutableStateOf(false)
}

// ─── Construcción dinámica de ColorScheme ────────────────────────────────────
private fun buildLightColorScheme(palette: AppColorPalette): ColorScheme = lightColorScheme(
    primary            = palette.primary,
    onPrimary          = Color.White,
    primaryContainer   = palette.primary.copy(alpha = 0.12f),
    onPrimaryContainer = palette.primary,
    secondary          = palette.secondary,
    onSecondary        = Color.Black,
    secondaryContainer = palette.secondary.copy(alpha = 0.15f),
    onSecondaryContainer = palette.secondary.copy(alpha = 0.9f),
    tertiary           = palette.secondary.copy(alpha = 0.8f),
    background         = Color(0xFFF8F8F8),
    surface            = Color.White,
    onSurface          = Color(0xFF1C1B1F),
    onSurfaceVariant   = Color(0xFF6B6B6B),
    error              = Color(0xFFB00020),
    errorContainer     = Color(0xFFFFDAD6),
    outline            = Color(0xFFBDBDBD)
)

private fun buildDarkColorScheme(palette: AppColorPalette): ColorScheme = darkColorScheme(
    primary            = palette.primaryDark,
    onPrimary          = Color.Black,
    primaryContainer   = palette.primaryDark.copy(alpha = 0.2f),
    onPrimaryContainer = palette.primaryDark,
    secondary          = palette.secondaryDark,
    onSecondary        = Color.Black,
    secondaryContainer = palette.secondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = palette.secondaryDark,
    tertiary           = palette.secondaryDark.copy(alpha = 0.8f),
    background         = Color(0xFF121212),
    surface            = Color(0xFF1E1E1E),
    onSurface          = Color(0xFFE6E1E5),
    onSurfaceVariant   = Color(0xFFAAAAAA),
    error              = Color(0xFFCF6679),
    errorContainer     = Color(0xFF8B0000),
    outline            = Color(0xFF555555)
)

// ─── Composable principal del tema ────────────────────────────────────────────
@Composable
fun AppOxxoTheme(
    darkTheme: Boolean = ThemeConfig.isDarkMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = ThemeConfig.selectedPalette
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(palette)
        else      -> buildLightColorScheme(palette)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
