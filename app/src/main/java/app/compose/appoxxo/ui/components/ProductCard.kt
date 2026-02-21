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
            // ── Imagen ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_inventory),
                        contentDescription = null,
                        modifier           = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    StockAlertChip(stock = product.stock)
                }
            }

            // ── Info ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text       = product.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1
                )
                if (product.codigo.isNotEmpty()) {
                    Text(
                        text  = product.codigo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "$${"%.2f".format(product.price)}",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        fontSize   = 20.sp
                    )
                }

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
                    }
                    if (role == UserRole.ADMIN || role == UserRole.ENCARGADO) {
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