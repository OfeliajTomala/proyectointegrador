package app.compose.appoxxo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.data.model.Product
import app.compose.appoxxo.data.model.UserRole
import coil.compose.AsyncImage

@Composable
fun ProductCard(
    product: Product,
    role: UserRole?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("Precio: $${product.price}")
            Text("Stock: ${product.stock}")

            StockAlertChip(stock = product.stock)

            Spacer(modifier = Modifier.height(8.dp))

            Row {

                // Encargado y Admin pueden editar
                if (role == UserRole.ADMIN || role == UserRole.ENCARGADO) {
                    TextButton(onClick = onEdit) {
                        Text("Editar")
                    }
                }

                // Solo Admin puede eliminar
                if (role == UserRole.ADMIN) {
                    TextButton(onClick = onDelete) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
