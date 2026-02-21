package app.compose.appoxxo.data.repository

import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java



class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    private var cachedUser: User? = null

    // ─── Email / Password ────────────────────────────────────────

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
            val data = mapOf(
                "uid"   to uid,
                "name"  to name,
                "email" to email,
                "role"  to role.name
            )
            usersRef.document(uid).set(data).await()
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

    // ─── Google Sign-In ──────────────────────────────────────────

    // Recibe el idToken extraído por Credential Manager y autentica con Firebase
    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Usuario nulo tras Google Sign-In")

            // Si el documento ya existe en Firestore lo leemos; si no, lo creamos
            val snapshot = usersRef.document(firebaseUser.uid).get().await()
            val user = if (snapshot.exists()) {
                snapshot.toObject(User::class.java)
                    ?: throw Exception("Error al deserializar usuario")
            } else {
                // Primer login con Google — crear documento con rol CAJERO
                val newUser = User(
                    uid   = firebaseUser.uid,
                    name  = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    role  = UserRole.CAJERO
                )
                val data = mapOf(
                    "uid"   to newUser.uid,
                    "name"  to newUser.name,
                    "email" to newUser.email,
                    "role"  to newUser.role.name
                )
                usersRef.document(firebaseUser.uid).set(data).await()
                newUser
            }
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Session ─────────────────────────────────────────────────

    fun logout() {
        cachedUser = null
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserName(): String? = cachedUser?.name
    fun getCurrentUser(): User? = cachedUser

    // ─── Administración de usuarios (solo ADMIN) ─────────────────

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val users = usersRef.get().await().toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit> {
        return try {
            usersRef.document(uid).update("role", role.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            usersRef.document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
