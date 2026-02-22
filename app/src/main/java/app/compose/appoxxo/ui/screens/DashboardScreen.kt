package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.components.AppSectionCard
import app.compose.appoxxo.ui.components.AppStatCard
import app.compose.appoxxo.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val stats   by viewModel.stats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Refresca cada vez que se entra al Dashboard
    LaunchedEffect(Unit) { viewModel.loadStats() }

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─── Header con botón refrescar ───────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Resumen General",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.loadStats() }) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_swap_vert),
                        contentDescription = "Actualizar",
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ─── Estadísticas generales ───────────────────────────────
        item {
            AppSectionCard(
                title    = "",
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is app.compose.appoxxo.data.util.UiState.Loading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppStatCard(
                            title       = "Total Productos",
                            value       = stats.totalProducts.toString(),
                            modifier    = Modifier.weight(1f),
                            accentColor = MaterialTheme.colorScheme.primary,
                            icon = {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_inventory),
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        )
                        AppStatCard(
                            title       = "Stock Total",
                            value       = stats.totalStock.toString(),
                            modifier    = Modifier.weight(1f),
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            icon = {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_layers),
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.tertiary,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppStatCard(
                            title       = "Stock Bajo",
                            value       = stats.lowStockCount.toString(),
                            modifier    = Modifier.weight(1f),
                            accentColor = MaterialTheme.colorScheme.error,
                            icon = {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_warning),
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.error,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        )
                        AppStatCard(
                            title       = "Valor Inventario",
                            value       = "$${"%.0f".format(stats.totalInventoryValue)}",
                            modifier    = Modifier.weight(1f),
                            accentColor = Color(0xFF2E7D32),
                            icon = {
                                Icon(
                                    painter            = painterResource(R.drawable.ic_attach_money),
                                    contentDescription = null,
                                    tint               = Color(0xFF2E7D32),
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // ─── Productos con stock bajo ─────────────────────────────
        item {
            AppSectionCard(
                title    = "Productos con Bajo Stock",
                modifier = Modifier.fillMaxWidth(),
                trailing = {
                    TextButton(onClick = onNavigateToAlerts) { Text("Ver todos") }
                }
            ) {
                if (stats.lowStockProducts.isEmpty()) {
                    Text(
                        "Todos los productos tienen stock suficiente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val preview = stats.lowStockProducts.take(3)
                    preview.forEachIndexed { index, product ->
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                product.name,
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(
                                    "${product.stock} uds",
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                        if (index < preview.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // ─── Productos recientes ──────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Productos Recientes",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToProducts) { Text("Ver todos") }
            }
        }

        if (stats.recentProducts.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay productos aún",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(stats.recentProducts) { product ->
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProducts() },
                    shape     = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border    = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_inventory),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Código: ${product.codigo.ifEmpty { product.id.take(8).uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$${"%.2f".format(product.price)}",
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Stock: ${product.stock}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (product.stock <= 5)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}