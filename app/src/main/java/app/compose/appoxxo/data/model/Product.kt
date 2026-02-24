package app.compose.appoxxo.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

//Producto en Firestore.
@IgnoreExtraProperties

data class Product(
    //Identificacion
    val id: String = "",
    val name: String = "",
    val codigo: String = "",
    //Datos comerciales
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",

    //  Creación
    val createdBy: String = "",
    val createdByName: String = "",         // nombre usuario que creó
    val createdAt: Timestamp = Timestamp.now(), // fecha/hora creación

    // Edición
    val updatedById: String = "",
    val updatedByName: String = "",          // nombre usuario que editó
    val updatedAt: Timestamp? = null,       // fecha/hora edición

    // Eliminación
    val deletedBy: String = "",             // nombre usuario que eliminó
    val deletedById: String = "",
    val deletedAt: Timestamp? = null,       // fecha/hora eliminación

    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false          // flag de eliminado
)
