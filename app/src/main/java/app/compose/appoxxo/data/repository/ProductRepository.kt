package app.compose.appoxxo.data.repository

import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productRef = db.collection("products")
    private val movementRef = db.collection("movements")

    // ─── Productos ───────────────────────────────────────────────

    suspend fun getProducts(): List<Product> {
        return productRef.get().await().toObjects(Product::class.java)
    }

    suspend fun getProductById(id: String): Product? {
        return productRef.document(id).get().await().toObject(Product::class.java)
    }

    suspend fun addProduct(product: Product) {
        val doc = productRef.document()
        productRef.document(doc.id).set(product.copy(id = doc.id)).await()
    }

    suspend fun updateProduct(product: Product) {
        productRef.document(product.id).set(product).await()
    }

    suspend fun deleteProduct(productId: String) {
        productRef.document(productId).delete().await()
    }

    // ─── Movimientos ─────────────────────────────────────────────

    suspend fun registerMovement(movement: Movement) {
        val doc = movementRef.document()
        movementRef.document(doc.id).set(movement.copy(id = doc.id)).await()

        val product = getProductById(movement.productId) ?: return
        val newStock = when (movement.type) {
            MovementType.ENTRADA -> product.stock + movement.quantity
            MovementType.SALIDA  -> (product.stock - movement.quantity).coerceAtLeast(0)
        }
        productRef.document(movement.productId).update("stock", newStock).await()
    }

    suspend fun getMovementsForProduct(productId: String): List<Movement> {
        return movementRef
            .whereEqualTo("productId", productId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Movement::class.java)
    }
}
