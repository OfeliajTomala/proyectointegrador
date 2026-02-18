package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.ui.components.StockAlertChip
import app.compose.appoxxo.viewmodel.DashboardViewModel
import app.compose.appoxxo.R
import app.compose.appoxxo.ui.components.AppStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProducts: () -> Unit,
    onLogout: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_exittoapp),
                            contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppStatCard(
                    title = "Productos",
                    value = stats.totalProducts.toString(),
                    modifier = Modifier.weight(1f)
                )
                AppStatCard(
                    title = "Valor total",
                    value = "$${"%.2f".format(stats.totalInventoryValue)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (stats.lowStockProducts.isNotEmpty()) {
                Text("⚠️ Productos con stock bajo", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                stats.lowStockProducts.forEach { product ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(product.name, modifier = Modifier.weight(1f))
                        StockAlertChip(stock = product.stock)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = onNavigateToProducts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_inventory),
                    contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver inventario")
            }
        }
    }
}