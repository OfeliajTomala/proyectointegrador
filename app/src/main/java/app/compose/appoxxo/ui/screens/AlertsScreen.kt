package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.viewmodel.ProductViewModel

@Composable
fun AlertsScreen(viewModel: ProductViewModel) {
    val products         by viewModel.products.collectAsState()
    val lowStockProducts  = products.filter { it.stock <= 5 }
    val outOfStock        = products.filter { it.stock == 0 }
    val warningStock      = products.filter { it.stock in 1..5 }

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Resumen en dos cards
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sin stock
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.07f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_warning),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            outOfStock.size.toString(),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.error,
                            fontSize   = 30.sp
                        )
                        Text(
                            "Sin stock",
                            style  = MaterialTheme.typography.labelMedium,
                            color  = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stock bajo
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.07f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.tertiary,
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            warningStock.size.toString(),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.tertiary,
                            fontSize   = 30.sp
                        )
                        Text(
                            "Stock bajo",
                            style  = MaterialTheme.typography.labelMedium,
                            color  = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Lista de productos
        if (lowStockProducts.isEmpty()) {
            item {
                AppEmptyState(
                    iconRes  = R.drawable.ic_check_circle,
                    title    = "¡Todo en orden!",
                    subtitle = "Ningún producto tiene stock bajo."
                )
            }
        } else {
            item {
                Text(
                    "Requieren atención",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            items(lowStockProducts.sortedBy { it.stock }) { product ->
                val isOutOfStock = product.stock == 0
                val accentColor  = if (isOutOfStock)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.tertiary

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.05f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        accentColor.copy(alpha = 0.15f)
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_warning),
                                contentDescription = null,
                                tint               = accentColor,
                                modifier           = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp
                            )
                            if (product.codigo.isNotEmpty()) {
                                Text(
                                    product.codigo,
                                    style  = MaterialTheme.typography.bodySmall,
                                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                "$${"%.2f".format(product.price)}",
                                style  = MaterialTheme.typography.bodySmall,
                                color  = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor
                        ) {
                            Text(
                                if (isOutOfStock) "Sin stock" else "${product.stock} uds",
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color      = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}