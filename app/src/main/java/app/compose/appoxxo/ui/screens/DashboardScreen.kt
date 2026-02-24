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
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.util.UiState
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

    LaunchedEffect(Unit) { viewModel.loadStats() }

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ─── Header ───────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dashboard",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 22.sp
                    )
                    Text(
                        "Resumen del inventario",
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { viewModel.loadStats() }) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_update),
                        contentDescription = "Actualizar",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ─── Loading indicator ────────────────────────────────────
        if (uiState is UiState.Loading) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color            = MaterialTheme.colorScheme.primary,
                    trackColor       = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }

        // ─── Productos: stats principales ─────────────────────────
        item {
            AppSectionCard(
                title    = "Productos",
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppStatCard(
                        title       = "Activos",
                        value       = stats.totalProducts.toString(),
                        modifier    = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        icon = {
                            Icon(
                                painter            = painterResource(R.drawable.ic_inventory),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                    AppStatCard(
                        title       = "Eliminados",
                        value       = stats.totalDeletedProducts.toString(),
                        modifier    = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.error,
                        icon = {
                            Icon(
                                painter            = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
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
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Valor del inventario — card ancho completo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color(0xFF2E7D32).copy(alpha = 0.07f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF2E7D32).copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Color(0xFF2E7D32).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_attach_money),
                                contentDescription = null,
                                tint               = Color(0xFF2E7D32),
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Valor del inventario",
                                style  = MaterialTheme.typography.labelMedium,
                                color  = Color(0xFF2E7D32).copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "$${"%.0f".format(stats.totalInventoryValue)}",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color(0xFF2E7D32),
                                fontSize   = 26.sp
                            )
                        }
                    }
                }
            }
        }

        // ─── Movimientos ──────────────────────────────────────────
        item {
            AppSectionCard(
                title    = "Movimientos",
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppStatCard(
                        title       = "Total",
                        value       = stats.totalMovements.toString(),
                        modifier    = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        icon = {
                            Icon(
                                painter            = painterResource(R.drawable.ic_list),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                    AppStatCard(
                        title       = "Eliminados",
                        value       = stats.totalDeletedMovements.toString(),
                        modifier    = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.error,
                        icon = {
                            Icon(
                                painter            = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }

        // ─── Bajo stock ───────────────────────────────────────────
        item {
            AppSectionCard(
                title    = "Bajo Stock",
                modifier = Modifier.fillMaxWidth(),
                trailing = {
                    TextButton(
                        onClick        = onNavigateToAlerts,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "Ver todos",
                            fontSize   = 13.sp,
                            color      = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            ) {
                if (stats.lowStockProducts.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint               = Color(0xFF43A047),
                            modifier           = Modifier.size(16.dp)
                        )
                        Text(
                            "Todos los productos tienen stock suficiente",
                            style  = MaterialTheme.typography.bodyMedium,
                            color  = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val preview = stats.lowStockProducts.take(3)
                    preview.forEachIndexed { index, product ->
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (product.stock == 0)
                                                MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.tertiary,
                                            RoundedCornerShape(50)
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    product.name,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                            Badge(
                                containerColor = if (product.stock == 0)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(
                                    if (product.stock == 0) "Sin stock" else "${product.stock} uds",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        if (index < preview.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 1.dp),
                                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
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
                    "Productos recientes",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                TextButton(
                    onClick        = onNavigateToProducts,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        "Ver todos",
                        fontSize   = 13.sp,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (stats.recentProducts.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
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
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border    = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
                        Box(
                            modifier         = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_inventory),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp
                            )
                            Text(
                                "Cód: ${product.codigo.ifEmpty { product.id.take(8).uppercase() }}",
                                style  = MaterialTheme.typography.bodySmall,
                                color  = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$${"%.2f".format(product.price)}",
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color      = MaterialTheme.colorScheme.primary,
                                fontSize   = 16.sp
                            )
                            Text(
                                "Stock: ${product.stock}",
                                style  = MaterialTheme.typography.bodySmall,
                                color  = if (product.stock <= 5)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}