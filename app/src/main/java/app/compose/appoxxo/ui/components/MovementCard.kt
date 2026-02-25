package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MovementCard(
    movement: Movement,
    onDelete: (() -> Unit)? = null
) {
    val isEntrada   = movement.type == MovementType.ENTRADA

    // Colores fijos: Verde para entrada, Rojo para salida
    val accentColor = if (isEntrada) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val backgroundAlpha = 0.1f

    val dateStr = remember(movement.date) {
        try {
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("es"))
                .format(movement.date.toDate())
        } catch (_: Exception) { "—" }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.12f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono tipo movimiento
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = backgroundAlpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEntrada) R.drawable.ic_arrow_downward
                        else R.drawable.ic_arrow_upward
                    ),
                    contentDescription = if (isEntrada) "Entrada" else "Salida",
                    tint               = accentColor,
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Text(
                    dateStr,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (movement.userName.isNotEmpty()) {
                    Text(
                        "Por: ${movement.userName}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "${if (isEntrada) "+" else "-"}${movement.quantity}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accentColor,
                    fontSize   = 20.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = backgroundAlpha)
                ) {
                    Text(
                        if (isEntrada) "ENTRADA" else "SALIDA",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 10.sp,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                // Botón eliminar — solo si se pasa el callback
                onDelete?.let {
                    IconButton(
                        onClick  = it,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Eliminar movimiento",
                            tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier           = Modifier.size(16.dp)
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
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEntrada) R.drawable.ic_arrow_downward
                        else R.drawable.ic_arrow_upward
                    ),
                    contentDescription = null,
                    tint               = accentColor.copy(alpha = 0.45f),
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize   = 15.sp
                )
                Text(
                    "Registrado: $dateStr",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                if (movement.userName.isNotEmpty()) {
                    Text(
                        "Por: ${movement.userName}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Eliminado: $deletedAtStr",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
                if (movement.deletedBy.isNotEmpty()) {
                    Text(
                        "Eliminado por: ${movement.deletedBy}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "${if (isEntrada) "+" else "-"}${movement.quantity}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accentColor.copy(alpha = 0.45f),
                    fontSize   = 20.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.07f)
                ) {
                    Text(
                        if (isEntrada) "ENTRADA" else "SALIDA",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = accentColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 10.sp,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}