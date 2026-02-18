package app.compose.appoxxo.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StockAlertChip(
    stock: Int,
    modifier: Modifier = Modifier
) {
    val isLow = stock <= 5
    AssistChip(
        onClick = {},
        label = { Text(if (isLow) "Stock bajo: $stock" else "Stock OK: $stock") },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isLow)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    )
}