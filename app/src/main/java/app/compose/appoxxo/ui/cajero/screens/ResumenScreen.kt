package app.compose.appoxxo.ui.cajero.screens

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
import app.compose.appoxxo.viewmodel.DashboardViewModel

@Composable
fun ResumenScreen(
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
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─── Loading bar ──────────────────────────────────────────
        if (uiState is UiState.Loading) {
            item {
                LinearProgressIndicator(
                    modifier   = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color      = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }

        // ─── Dos cards de resumen ─────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Card: Total Productos
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A6E1D).copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF1A6E1D).copy(alpha = 0.18f)
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
                                .background(
                                    Color(0xFF1A6E1D).copy(alpha = 0.13f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_inventory),
                                contentDescription = null,
                                tint               = Color(0xFF1A6E1D),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Total",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color(0xFF1A6E1D).copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                stats.totalProducts.toString(),
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color(0xFF1A6E1D),
                                fontSize   = 24.sp
                            )
                            Text(
                                "productos",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color(0xFF1A6E1D).copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Card: Stock Bajo
                val hasLow = stats.lowStockCount > 0
                Card(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (hasLow)
                            Color(0xFFFF5722).copy(alpha = 0.08f)
                        else
                            Color(0xFF43A047).copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hasLow) Color(0xFFFF5722).copy(alpha = 0.18f)
                        else        Color(0xFF43A047).copy(alpha = 0.18f)
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
                                .background(
                                    if (hasLow) Color(0xFFFF5722).copy(alpha = 0.13f)
                                    else        Color(0xFF43A047).copy(alpha = 0.13f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(
                                    if (hasLow) R.drawable.ic_warning
                                    else        R.drawable.ic_check_circle
                                ),
                                contentDescription = null,
                                tint               = if (hasLow) Color(0xFFFF5722) else Color(0xFF43A047),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Stock bajo",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = if (hasLow) Color(0xFFFF5722).copy(alpha = 0.7f)
                                else        Color(0xFF43A047).copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                stats.lowStockCount.toString(),
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color      = if (hasLow) Color(0xFFFF5722) else Color(0xFF43A047),
                                fontSize   = 24.sp
                            )
                            Text(
                                "productos",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = if (hasLow) Color(0xFFFF5722).copy(alpha = 0.6f)
                                else        Color(0xFF43A047).copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // ─── Sección: Bajo Stock ──────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        tint               = Color(0xFFFF5722),
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Bajo Stock",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                    if (stats.lowStockCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = Color(0xFFFF5722)) {
                            Text(
                                stats.lowStockCount.toString(),
                                modifier = Modifier.padding(horizontal = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
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
        }

        if (stats.lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color(0xFF43A047).copy(alpha = 0.07f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF43A047).copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint               = Color(0xFF43A047),
                            modifier           = Modifier.size(20.dp)
                        )
                        Text(
                            "Todos los productos tienen stock suficiente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF43A047)
                        )
                    }
                }
            }
        } else {
            items(stats.lowStockProducts) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (product.stock == 0)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        else Color(0xFFFF5722).copy(alpha = 0.2f)
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
                                .size(44.dp)
                                .background(
                                    if (product.stock == 0)
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                    else Color(0xFFFF5722).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (product.stock == 0) R.drawable.ic_delete
                                    else R.drawable.ic_warning
                                ),
                                contentDescription = null,
                                tint     = if (product.stock == 0)
                                    MaterialTheme.colorScheme.error
                                else Color(0xFFFF5722),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp,
                                maxLines   = 1
                            )
                            Text(
                                "Cód: ${product.codigo.ifEmpty { product.id.take(8).uppercase() }}",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Badge(
                            containerColor = if (product.stock == 0)
                                MaterialTheme.colorScheme.error
                            else Color(0xFFFF5722)
                        ) {
                            Text(
                                if (product.stock == 0) "Sin stock" else "${product.stock} uds",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // ─── Divider ──────────────────────────────────────────────
        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }

        // ─── Sección: Productos Recientes ─────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_inventory),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Productos Recientes",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
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
                                fontSize   = 15.sp,
                                maxLines   = 1
                            )
                            Text(
                                "Cód: ${product.codigo.ifEmpty { product.id.take(8).uppercase() }}",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                style    = MaterialTheme.typography.bodySmall,
                                color    = if (product.stock <= 5)
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