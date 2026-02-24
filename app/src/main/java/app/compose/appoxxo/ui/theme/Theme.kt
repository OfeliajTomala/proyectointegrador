package app.compose.appoxxo.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─── Paletas adicionales seleccionables ──────────────────────────────────────

object AppPalettes {
    val RedYellow = AppColorPalette(
        name          = "Rojo & Amarillo",
        primary       = Color(0xFFD32F2F),
        secondary     = Color(0xFFFBC02D),
        primaryDark   = Color(0xFFFF6659),
        secondaryDark = Color(0xFFFFEB3B)
    )
    val BlueTeal = AppColorPalette(
        name          = "Azul & Verde Agua",
        primary       = Color(0xFF1565C0),
        secondary     = Color(0xFF00897B),
        primaryDark   = Color(0xFF5E92F3),
        secondaryDark = Color(0xFF4DB6AC)
    )
    val PurpleOrange = AppColorPalette(
        name          = "Morado & Naranja",
        primary       = Color(0xFF6A1B9A),
        secondary     = Color(0xFFE65100),
        primaryDark   = Color(0xFFCE93D8),
        secondaryDark = Color(0xFFFFAB91)
    )
    val GreenAmber = AppColorPalette(
        name          = "Verde & Ámbar",
        primary       = Color(0xFF2E7D32),
        secondary     = Color(0xFFFF6F00),
        primaryDark   = Color(0xFF81C784),
        secondaryDark = Color(0xFFFFCA28)
    )
    val IndigoRose = AppColorPalette(
        name          = "Índigo & Rosa",
        primary       = Color(0xFF283593),
        secondary     = Color(0xFFC2185B),
        primaryDark   = Color(0xFF7986CB),
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
    var isDarkMode      by mutableStateOf(false)
}

// ─── Construcción dinámica de ColorScheme ────────────────────────────────────

private fun buildLightColorScheme(palette: AppColorPalette): ColorScheme = lightColorScheme(
    primary              = palette.primary,
    onPrimary            = Color.White,
    primaryContainer     = palette.primary.copy(alpha = 0.1f),
    onPrimaryContainer   = palette.primary,

    secondary            = palette.secondary,
    onSecondary          = Color.Black,
    secondaryContainer   = palette.secondary.copy(alpha = 0.12f),
    onSecondaryContainer = palette.secondary.copy(alpha = 0.85f),

    // Tertiary usado para "stock bajo" — amarillo/acento cálido
    tertiary             = palette.secondary.copy(alpha = 0.85f),
    onTertiary           = Color.Black,
    tertiaryContainer    = palette.secondary.copy(alpha = 0.15f),
    onTertiaryContainer  = palette.secondary.copy(alpha = 0.9f),

    background           = Color(0xFFF7F7F8),   // Gris muy claro, no blanco puro
    surface              = Color(0xFFFFFFFF),
    surfaceVariant       = Color(0xFFF0F0F2),
    onSurface            = Color(0xFF1A1A1A),
    onSurfaceVariant     = Color(0xFF6B6B72),

    error                = Color(0xFFB00020),
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF8B0000),

    outline              = Color(0xFFBBBBC0),
    outlineVariant       = Color(0xFFE2E2E6),    // Borde muy sutil para cards
    scrim                = Color(0xFF000000)
)

private fun buildDarkColorScheme(palette: AppColorPalette): ColorScheme = darkColorScheme(
    primary              = palette.primaryDark,
    onPrimary            = Color.Black,
    primaryContainer     = palette.primaryDark.copy(alpha = 0.18f),
    onPrimaryContainer   = palette.primaryDark,

    secondary            = palette.secondaryDark,
    onSecondary          = Color.Black,
    secondaryContainer   = palette.secondaryDark.copy(alpha = 0.18f),
    onSecondaryContainer = palette.secondaryDark,

    tertiary             = palette.secondaryDark.copy(alpha = 0.85f),
    onTertiary           = Color.Black,
    tertiaryContainer    = palette.secondaryDark.copy(alpha = 0.2f),
    onTertiaryContainer  = palette.secondaryDark,

    background           = Color(0xFF111113),    // Casi negro pero no puro
    surface              = Color(0xFF1C1C1F),
    surfaceVariant       = Color(0xFF26262A),
    onSurface            = Color(0xFFE8E8EC),
    onSurfaceVariant     = Color(0xFFA0A0A8),

    error                = Color(0xFFCF6679),
    onError              = Color.Black,
    errorContainer       = Color(0xFF5C0A16),
    onErrorContainer     = Color(0xFFFFB4BC),

    outline              = Color(0xFF505056),
    outlineVariant       = Color(0xFF38383E),    // Borde sutil en dark
    scrim                = Color(0xFF000000)
)

// ─── Composable principal del tema ────────────────────────────────────────────

@Composable
fun AppOxxoTheme(
    darkTheme: Boolean    = ThemeConfig.isDarkMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = ThemeConfig.selectedPalette

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
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