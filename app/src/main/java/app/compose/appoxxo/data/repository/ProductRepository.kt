package app.compose.appoxxo.data.repository

import app.compose.appoxxo.data.model.Movement
import app.compose.appoxxo.data.model.MovementType
import app.compose.appoxxo.data.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository {

    private val db          = FirebaseFirestore.getInstance()
    private val productRef  = db.collection("products")
    private val movementRef = db.collection("movements")

    // ─── Productos activos ────────────────────────────────────────

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        productRef
            .whereEqualTo("isDeleted", false)
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    suspend fun getDeletedProducts(): List<Product> = withContext(Dispatchers.IO) {
        productRef
            .whereEqualTo("isDeleted", true)
            .orderBy("deletedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        productRef.document(id).get().await().toObject(Product::class.java)
    }

    // ─── Validación de campos únicos ──────────────────────────────

    suspend fun validateUniqueFields(
        name: String,
        codigo: String,
        excludeId: String = ""
    ): String? = withContext(Dispatchers.IO) {

        val nameTaken = productRef
            .whereEqualTo("name", name.trim())
            .whereEqualTo("isDeleted", false)
            .get()
            .await()
            .toObjects(Product::class.java)
            .any { it.id != excludeId }

        if (nameTaken) return@withContext "Ya existe un producto con ese nombre"

        if (codigo.isNotBlank()) {
            val codigoTaken = productRef
                .whereEqualTo("codigo", codigo.trim())
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
                .toObjects(Product::class.java)
                .any { it.id != excludeId }

            if (codigoTaken) return@withContext "Ya existe un producto con ese código"
        }

        null
    }

    // ─── Agregar producto ─────────────────────────────────────────

    suspend fun addProductAndGetId(
        product: Product,
        createdByName: String,
        createdById: String
    ): String = withContext(Dispatchers.IO) {

        val error = validateUniqueFields(product.name, product.codigo)
        if (error != null) throw Exception(error)

        val doc = productRef.document()
        productRef.document(doc.id).set(
            product.copy(
                id            = doc.id,
                createdByName = createdByName,
                createdBy     = createdById,
                createdAt     = Timestamp.now(),
                isDeleted     = false
            )
        ).await()

        doc.id
    }

    // ─── Actualizar producto ──────────────────────────────────────

    suspend fun updateProduct(
        product: Product,
        updatedByName: String = "",
        updatedById: String = ""
    ): String = withContext(Dispatchers.IO) {

        val error = validateUniqueFields(product.name, product.codigo, product.id)
        if (error != null) throw Exception(error)

        val previousImageUrl = getProductById(product.id)?.imageUrl ?: ""

        productRef.document(product.id).set(
            product.copy(
                updatedByName = updatedByName,
                updatedById   = updatedById,
                updatedAt     = Timestamp.now()
            )
        ).await()

        previousImageUrl
    }

    // ─── Soft delete producto ─────────────────────────────────────

    suspend fun deleteProduct(
        productId: String,
        deletedByName: String,
        deletedById: String
    ) = withContext(Dispatchers.IO) {

        productRef.document(productId).update(
            mapOf(
                "isDeleted"   to true,
                "deletedBy"   to deletedByName,
                "deletedById" to deletedById,
                "deletedAt"   to Timestamp.now()
            )
        ).await()
    }

    // ─── Restaurar producto ───────────────────────────────────────

    suspend fun restoreProduct(productId: String) = withContext(Dispatchers.IO) {
        productRef.document(productId).update(
            mapOf(
                "isDeleted"   to false,
                "deletedBy"   to "",
                "deletedById" to "",
                "deletedAt"   to null
            )
        ).await()
    }

    // ─── Actualiza solo la imagen ─────────────────────────────────

    suspend fun updateImageUrl(productId: String, imageUrl: String) =
        withContext(Dispatchers.IO) {
            productRef.document(productId)
                .update("imageUrl", imageUrl)
                .await()
        }

    // ─── Movimientos activos de un producto ───────────────────────

    suspend fun getMovementsForProduct(productId: String): List<Movement> =
        withContext(Dispatchers.IO) {
            movementRef
                .whereEqualTo("productId", productId)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Movement::class.java)
        }

    // ─── Movimientos eliminados de un producto ────────────────────

    suspend fun getDeletedMovementsForProduct(productId: String): List<Movement> =
        withContext(Dispatchers.IO) {
            movementRef
                .whereEqualTo("productId", productId)
                .whereEqualTo("isDeleted", true)
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Movement::class.java)
        }

    // ─── Todos los movimientos activos ────────────────────────────

    suspend fun getAllMovements(): List<Movement> =
        withContext(Dispatchers.IO) {
            movementRef
                .whereEqualTo("isDeleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Movement::class.java)
        }

    // ─── Todos los movimientos eliminados ─────────────────────────

    suspend fun getDeletedMovements(): List<Movement> =
        withContext(Dispatchers.IO) {
            movementRef
                .whereEqualTo("isDeleted", true)
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Movement::class.java)
        }

    // ─── Guarda movimiento sin recalcular stock ───────────────────
    // Usado al crear un producto con stock inicial para evitar
    // que registerMovement valide stock contra el producto recién creado

    suspend fun saveMovementOnly(movement: Movement): String =
        withContext(Dispatchers.IO) {
            val doc = movementRef.document()
            movementRef.document(doc.id).set(
                movement.copy(id = doc.id, isDeleted = false)
            ).await()
            doc.id
        }

    // ─── Registrar movimiento ─────────────────────────────────────

    suspend fun registerMovement(movement: Movement) =
        withContext(Dispatchers.IO) {

            val product = getProductById(movement.productId)
                ?: throw Exception("Producto no encontrado")

            if (movement.type == MovementType.SALIDA &&
                movement.quantity > product.stock
            ) {
                throw Exception("Stock insuficiente. Disponible: ${product.stock}")
            }

            val doc = movementRef.document()
            movementRef.document(doc.id).set(
                movement.copy(id = doc.id, isDeleted = false)
            ).await()

            recalculateStock(movement.productId)
        }

    // ─── Soft delete movimiento ───────────────────────────────────

    suspend fun deleteMovement(
        movementId: String,
        deletedByName: String,
        deletedById: String
    ): String = withContext(Dispatchers.IO) {

        val snap     = movementRef.document(movementId).get().await()
        val movement = snap.toObject(Movement::class.java)
            ?: throw Exception("Movimiento no encontrado")

        movementRef.document(movementId).update(
            mapOf(
                "isDeleted"   to true,
                "deletedBy"   to deletedByName,
                "deletedById" to deletedById,
                "deletedAt"   to Timestamp.now()
            )
        ).await()

        recalculateStock(movement.productId)

        movement.productId
    }

    // ─── Recálculo de stock ───────────────────────────────────────
    // Stock = Σ entradas activas − Σ salidas activas

    suspend fun recalculateStock(productId: String) =
        withContext(Dispatchers.IO) {

            val active = movementRef
                .whereEqualTo("productId", productId)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
                .toObjects(Movement::class.java)

            val entradas = active.filter { it.type == MovementType.ENTRADA }.sumOf { it.quantity }
            val salidas  = active.filter { it.type == MovementType.SALIDA }.sumOf { it.quantity }
            val newStock = (entradas - salidas).coerceAtLeast(0)

            productRef.document(productId)
                .update("stock", newStock)
                .await()
        }
}