package app.compose.appoxxo.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

// FIX: Kotlin genera getter isDeleted() y setter setDeleted() para campos booleanos
// con prefijo "is". Firestore usa el nombre del setter para el campo → lo llama "deleted".
// Con @PropertyName forzamos que Firestore use "isDeleted" como nombre del campo
// tanto al leer como al escribir.
//
// IMPORTANTE: el campo debe ser "var" (no "val") para que Firestore pueda
// llamar al setter durante la deserialización.
data class Movement(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val type: MovementType = MovementType.ENTRADA,
    val date: Timestamp = Timestamp.now(),
    val userId: String = "",
    val userName: String = "",
    val deletedAt: Timestamp? = null,
    val deletedBy: String = "",
    val deletedById: String = "",

    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false
)

enum class MovementType {
    ENTRADA,
    SALIDA
}
