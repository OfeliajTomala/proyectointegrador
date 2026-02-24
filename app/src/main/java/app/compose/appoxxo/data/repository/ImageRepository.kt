package app.compose.appoxxo.data.repository

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

/**
 * Sube y elimina imágenes de producto usando Supabase Storage.
 * La URL pública resultante se guarda en el campo imageUrl de Firestore.
 *
 * Bucket recomendado: "product-images"  (créalo en Supabase Dashboard → Storage
 *                      con política pública de lectura)
 */
class ImageRepository(
    supabase: SupabaseClient,
    context: Context
) {
    private val context = context.applicationContext
    private val bucket = supabase.storage["product-images"]

    /**
     * Sube la imagen al path  products/{productId}/{uuid}.jpg
     * y devuelve la URL pública permanente.
     */
    suspend fun uploadProductImage(productId: String, imageUri: Uri): String {
        val bytes    = readBytes(imageUri)
        val filename = "${UUID.randomUUID()}.jpg"
        val path     = "products/$productId/$filename"

        bucket.upload(path, bytes) { upsert = false }

        // URL pública — no caduca (bucket público)
        return bucket.publicUrl(path)
    }

    /**
     * Elimina la imagen del producto en Storage.
     * Llama a esto antes de borrar el documento de Firestore.
     *
     * @param imageUrl  URL almacenada en Firestore para extraer el path
     */
    suspend fun deleteProductImage(imageUrl: String) {
        if (imageUrl.isBlank()) return
        runCatching {
            // Extraemos el path relativo desde la URL pública
            // Ej: ".../storage/v1/object/public/product-images/products/abc/uuid.jpg"
            val marker = "product-images/"
            val idx    = imageUrl.indexOf(marker)
            if (idx != -1) {
                val path = imageUrl.substring(idx + marker.length)
                bucket.delete(listOf(path))
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun readBytes(uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("No se pudo leer el archivo: $uri")
}