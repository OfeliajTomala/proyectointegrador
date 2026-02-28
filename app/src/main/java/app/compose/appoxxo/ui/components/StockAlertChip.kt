package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R

@Composable
fun StockAlertChip(
    stock: Int,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    // Colores sólidos para máxima visibilidad
    val (containerColor, contentColor, label, iconRes) = when {
        stock == 0 -> StockLevel(
            Color(0xFFFF0000), // rojo brillante
            Color.White,       // texto blanco
            "Sin stock",
            R.drawable.ic_warning
        )
        stock <= 5 -> StockLevel(
            Color(0xFFFFA500), // naranja sólido
            Color.Black,       // texto negro
            "Stock bajo: $stock",
            R.drawable.ic_warning
        )
        else -> StockLevel(
            Color(0xFF4CAF50), // verde sólido
            Color.White,       // texto blanco
            "Stock: $stock",
            R.drawable.ic_check_circle
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)  // fondo sólido
            .padding(horizontal = 16.dp, vertical = 8.dp) // padding más grande
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showIcon) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp) // icono más grande
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,       // texto más grande
                letterSpacing = 0.2.sp
            )
        }
    }
}
private data class StockLevel(
    val containerColor: Color,
    val contentColor: Color,
    val label: String,
    val iconRes: Int
)