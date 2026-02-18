package app.compose.appoxxo.data.repository

import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    // Caché en memoria del usuario actual para evitar lecturas extra a Firestore
    private var cachedUser: User? = null

    suspend fun register(
        email: String,
        password: String,
        name: String = "",
        role: UserRole = UserRole.CAJERO
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID nulo tras registro")
            val user = User(uid = uid, name = name, email = email, role = role)
            usersRef.document(uid).set(user).await()
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID nulo tras login")
            val snapshot = usersRef.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
                ?: throw Exception("Usuario no encontrado en Firestore")
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        cachedUser = null
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserName(): String? = cachedUser?.name
}