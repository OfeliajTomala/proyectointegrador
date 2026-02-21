package app.compose.appoxxo.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Diálogo de confirmación reutilizable.
 * Usado para eliminar, cerrar sesión, etc.
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    dismissLabel: String = "Cancelar",
    confirmColor: Color = MaterialTheme.colorScheme.error,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text       = confirmLabel,
                    color      = confirmColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = dismissLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}
