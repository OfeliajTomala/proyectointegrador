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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
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
            .background(Color(0xFFF5F5F5)),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ─── Resumen en dos cards ─────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sin stock
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD32F2F).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_warning),
                                contentDescription = null,
                                tint               = Color(0xFFD32F2F),
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            outOfStock.size.toString(),
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFFD32F2F),
                            fontSize   = 24.sp
                        )
                        Text(
                            "Sin stock",
                            style  = MaterialTheme.typography.labelSmall,
                            color  = Color(0xFF6B6B72),
                            fontWeight = FontWeight.Medium,
                            fontSize   = 12.sp
                        )
                    }
                }

                // Stock bajo
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF5722).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_notifications),
                                contentDescription = null,
                                tint               = Color(0xFFFF5722),
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            warningStock.size.toString(),
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFFFF5722),
                            fontSize   = 24.sp
                        )
                        Text(
                            "Stock bajo",
                            style  = MaterialTheme.typography.labelSmall,
                            color  = Color(0xFF6B6B72),
                            fontWeight = FontWeight.Medium,
                            fontSize   = 12.sp
                        )
                    }
                }
            }
        }

        // ─── Lista de productos ────────────────────────────────────
        if (lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color(0xFF43A047).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint               = Color(0xFF43A047),
                            modifier           = Modifier.size(22.dp)
                        )
                        Text(
                            "¡Todo en orden! Todos los productos tienen stock.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF43A047),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    "⚠️ Requieren atención",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A),
                    fontSize   = 16.sp
                )
            }

            items(lowStockProducts.sortedBy { it.stock }) { product ->
                val isOutOfStock = product.stock == 0
                val bgColor      = if (isOutOfStock) Color(0xFFD32F2F) else Color(0xFFFF5722)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_warning),
                                contentDescription = null,
                                tint               = bgColor,
                                modifier           = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                            Text(
                                "$${"%.2f".format(product.price)}",
                                style  = MaterialTheme.typography.labelSmall,
                                color  = Color(0xFF6B6B72),
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor
                        ) {
                            Text(
                                if (isOutOfStock) "Agotado" else "${product.stock} uds",
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}