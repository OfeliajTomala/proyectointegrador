package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // ── Imagen — FIX #1: solo se muestra si el producto tiene imagen ──
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                        StockAlertChip(stock = product.stock)
                    }
                }
            }

            // ── Info ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(14.dp)) {

                // Si no hay imagen, el chip de stock va junto al nombre
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = product.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        modifier   = Modifier.weight(1f)
                    )
                    // FIX #1: el chip siempre visible, pero cuando no hay imagen
                    // se posiciona aquí en lugar del Box superior
                    if (!hasImage) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StockAlertChip(stock = product.stock)
                    }
                }

                if (product.codigo.isNotEmpty()) {
                    Text(
                        text  = product.codigo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text       = "$${"%.2f".format(product.price)}",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    fontSize   = 20.sp
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                // ── Acciones ──────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    onViewMovements?.let {
                        IconButton(onClick = it) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_swap_vert),
                                contentDescription = "Movimientos",
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (role == UserRole.ADMIN || role == UserRole.ENCARGADO) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Editar",
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Eliminar",
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}