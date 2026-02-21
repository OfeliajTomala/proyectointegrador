package app.compose.appoxxo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tarjeta base con elevación y borde opcional.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 0.dp,
    border: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        border    = if (border) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Tarjeta de estadística con icono, valor grande y etiqueta.
 */
@Composable
fun AppStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    icon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Tarjeta de sección con título y contenido.
 */
@Composable
fun AppSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                trailing?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}