package app.compose.appoxxo.data.model

import com.google.firebase.Timestamp
data class Movement(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val type: MovementType = MovementType.ENTRADA,
    val date: Timestamp = Timestamp.now(),
    val userId: String = "",
    val userName: String = ""

)

enum class MovementType {
    ENTRADA,
    SALIDA
}
