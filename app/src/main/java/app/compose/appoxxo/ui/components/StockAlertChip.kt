package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R

/**
 * Chip de estado de stock con tres niveles.
 * Rediseño: bordes redondeados más pronunciados, tamaño ligeramente mayor,
 * sin cambios en la lógica de colores ni en los niveles.
 */
@Composable
fun StockAlertChip(
    stock: Int,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (containerColor, contentColor, label, iconRes) = when {
        stock == 0 -> StockLevel(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
            "Sin stock",
            R.drawable.ic_warning
        )
        stock <= 5 -> StockLevel(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary,
            "Stock bajo: $stock",
            R.drawable.ic_warning
        )
        else -> StockLevel(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary,
            "Stock: $stock",
            R.drawable.ic_check_circle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (showIcon) {
                Icon(
                    painter            = painterResource(id = iconRes),
                    contentDescription = null,
                    tint               = contentColor,
                    modifier           = Modifier.size(12.dp)
                )
            }
            Text(
                text          = label,
                color         = contentColor,
                fontWeight    = FontWeight.Bold,
                fontSize      = 11.sp,
                letterSpacing = 0.1.sp
            )
        }
    }
}

private data class StockLevel(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val label: String,
    val iconRes: Int
)