package app.compose.appoxxo.data.model

//Modelo que representa un usuario en Firestore.
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.CAJERO,
    val photoUrl: String = ""

)
