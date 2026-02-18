package app.compose.appoxxo.data.model

//Producto en Firestore.
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val createdBy: String = "",
)
