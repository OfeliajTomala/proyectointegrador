package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.UserRole
import coil.compose.AsyncImage

@Composable
fun ProductCard(
    product: Product,
    role: UserRole?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewMovements: (() -> Unit)? = null
) {
    val hasImage = product.imageUrl.isNotEmpty()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // ── Imagen ─────────────────────────────────────────────
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                    // Chip de stock superpuesto
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        StockAlertChip(stock = product.stock)
                    }
                }
            }

            // ── Info ───────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        modifier   = Modifier.weight(1f),
                        fontSize   = 16.sp
                    )
                    if (!hasImage) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StockAlertChip(stock = product.stock)
                    }
                }

                if (product.codigo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text  = product.codigo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "$${"%.2f".format(product.price)}",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.primary,
                        fontSize   = 22.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Acciones en fila con el precio
                    onViewMovements?.let {
                        IconButton(
                            onClick  = it,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_swap_vert),
                                contentDescription = "Movimientos",
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }
                    if (role == UserRole.ADMIN || role == UserRole.ENCARGADO) {
                        IconButton(
                            onClick  = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Editar",
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick  = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Eliminar",
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}