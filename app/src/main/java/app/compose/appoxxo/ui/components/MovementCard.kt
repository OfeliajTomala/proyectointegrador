package app.compose.appoxxo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Locale
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.R

@Composable
fun MovementCard(
    movement: Movement,
    onDelete: (() -> Unit)? = null   // null = Cajero no ve el botón
) {
    val isEntrada   = movement.type == MovementType.ENTRADA
    val accentColor = if (isEntrada) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error

    val dateStr = remember(movement.date) {
        try {
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("es"))
                .format(movement.date.toDate())
        } catch (_: Exception) { "—" }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono entrada/salida
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEntrada) R.drawable.ic_arrow_downward
                        else R.drawable.ic_arrow_upward
                    ),
                    contentDescription = null,
                    tint     = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                // Fecha y hora del movimiento
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Usuario que registró
                if (movement.userName.isNotEmpty()) {
                    Text(
                        "Registrado por: ${movement.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isEntrada) "+" else "-"}${movement.quantity}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
                Text(
                    if (isEntrada) "ENTRADA" else "SALIDA",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
                // Botón eliminar — solo si se pasa el callback
                onDelete?.let {
                    IconButton(onClick = it) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Eliminar movimiento",
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DeletedMovementCard(movement: Movement) {
    val isEntrada   = movement.type == MovementType.ENTRADA
    val accentColor = MaterialTheme.colorScheme.onSurfaceVariant

    val dateStr = remember(movement.date) {
        try {
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("es"))
                .format(movement.date.toDate())
        } catch (_: Exception) { "—" }
    }

    val deletedAtStr = remember(movement.deletedAt) {
        try {
            movement.deletedAt?.let {
                SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("es"))
                    .format(it.toDate())
            } ?: "—"
        } catch (_: Exception) { "—" }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono con opacidad para indicar eliminado
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEntrada) R.drawable.ic_arrow_downward
                        else R.drawable.ic_arrow_upward
                    ),
                    contentDescription = null,
                    tint     = accentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Fecha y hora del movimiento original
                Text(
                    "Registrado: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Usuario que registró el movimiento
                if (movement.userName.isNotEmpty()) {
                    Text(
                        "Por: ${movement.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(6.dp))

                // Información de eliminación
                Text(
                    "Eliminado: $deletedAtStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
                if (movement.deletedBy.isNotEmpty()) {
                    Text(
                        "Eliminado por: ${movement.deletedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isEntrada) "+" else "-"}${movement.quantity}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor.copy(alpha = 0.5f)
                )
                Text(
                    if (isEntrada) "ENTRADA" else "SALIDA",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}