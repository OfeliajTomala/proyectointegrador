package app.compose.appoxxo.data.repository

import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db          = FirebaseFirestore.getInstance()
    private val productRef  = db.collection("products")
    private val movementRef = db.collection("movements")

    // ─── Productos ───────────────────────────────────────────────

    suspend fun getProducts(): List<Product> =
        productRef.get().await().toObjects(Product::class.java)

    suspend fun getProductById(id: String): Product? =
        productRef.document(id).get().await().toObject(Product::class.java)

    suspend fun validateUniqueFields(
        name: String,
        codigo: String,
        excludeId: String = ""
    ): String? {
        val products = getProducts()
        val nameTaken = products.any {
            it.name.trim().equals(name.trim(), ignoreCase = true) && it.id != excludeId
        }
        if (nameTaken) return "Ya existe un producto con ese nombre"

        if (codigo.isNotBlank()) {
            val codigoTaken = products.any {
                it.codigo.trim().equals(codigo.trim(), ignoreCase = true) && it.id != excludeId
            }
            if (codigoTaken) return "Ya existe un producto con ese código"
        }
        return null
    }

    suspend fun addProductAndGetId(product: Product): String {
        val error = validateUniqueFields(product.name, product.codigo)
        if (error != null) throw Exception(error)
        val doc = productRef.document()
        productRef.document(doc.id).set(product.copy(id = doc.id)).await()
        return doc.id
    }

    suspend fun updateProduct(product: Product) {
        val error = validateUniqueFields(product.name, product.codigo, product.id)
        if (error != null) throw Exception(error)
        productRef.document(product.id).set(product).await()
    }

    suspend fun deleteProduct(productId: String) {
        productRef.document(productId).delete().await()
    }

    // ─── Movimientos ─────────────────────────────────────────────

    suspend fun registerMovement(movement: Movement) {
        val doc = movementRef.document()
        movementRef.document(doc.id).set(movement.copy(id = doc.id)).await()

        val product  = getProductById(movement.productId) ?: return
        val newStock = when (movement.type) {
            MovementType.ENTRADA -> product.stock + movement.quantity
            MovementType.SALIDA  -> (product.stock - movement.quantity).coerceAtLeast(0)
        }
        productRef.document(movement.productId).update("stock", newStock).await()
    }

    // Fix — sin orderBy para evitar error de índice compuesto en Firestore
    suspend fun getMovementsForProduct(productId: String): List<Movement> {
        return try {
            movementRef
                .whereEqualTo("productId", productId)
                .get()
                .await()
                .toObjects(Movement::class.java)
                .sortedByDescending { it.date.seconds } // ordenamos en memoria
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getAllMovements(): List<Movement> {
        return try {
            movementRef
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Movement::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }
}