package app.compose.appoxxo.data.repository

import android.content.Context
import android.net.Uri
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(
    private val supabase: SupabaseClient,
    private val context: Context
) {
    private val auth     = FirebaseAuth.getInstance()
    private val db       = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val bucket   get() = supabase.storage["profile-images"]

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
            val uid    = result.user?.uid ?: throw Exception("UID nulo tras registro")
            val user   = User(uid = uid, name = name, email = email, role = role)
            usersRef.document(uid).set(userToMap(user)).await()
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result   = auth.signInWithEmailAndPassword(email, password).await()
            val uid      = result.user?.uid ?: throw Exception("UID nulo")
            val snapshot = usersRef.document(uid).get().await()
            val user     = snapshot.toObject(User::class.java)
                ?: throw Exception("Usuario no encontrado")
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Google ──────────────────────────────────────────────────

    suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential   = GoogleAuthProvider.getCredential(idToken, null)
            val result       = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Usuario nulo")
            val snapshot     = usersRef.document(firebaseUser.uid).get().await()
            val user = if (snapshot.exists()) {
                snapshot.toObject(User::class.java) ?: throw Exception("Error al deserializar")
            } else {
                val newUser = User(
                    uid      = firebaseUser.uid,
                    name     = firebaseUser.displayName ?: "",
                    email    = firebaseUser.email ?: "",
                    role     = UserRole.CAJERO,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                usersRef.document(firebaseUser.uid).set(userToMap(newUser)).await()
                newUser
            }
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Edición de perfil ───────────────────────────────────────

    //Actualizar nombre
    suspend fun updateName(name: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("No autenticado")
            usersRef.document(uid).update("name", name).await()
            cachedUser = cachedUser?.copy(name = name)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    //Cambiar correo
    @Suppress("DEPRECATION")
    suspend fun updateEmail(newEmail: String, currentPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No autenticado")

            val credential = EmailAuthProvider.getCredential(
                user.email ?: throw Exception("No se encontró el correo"),
                currentPassword
            )
            user.reauthenticate(credential).await()

            // Solo envía verificación — NO actualiza Firestore todavía
            user.verifyBeforeUpdateEmail(newEmail).await()

            Result.success(Unit)
        } catch (e: Exception) {
            val mensajeError = when {
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
                        e.message?.contains("wrong-password") == true ->
                    "La contraseña actual es incorrecta"
                e.message?.contains("email-already-in-use") == true ->
                    "Este correo ya está en uso"
                e.message?.contains("invalid-email") == true ->
                    "El correo ingresado no es válido"
                e.message?.contains("network") == true ->
                    "Error de conexión. Verifica tu internet"
                else -> e.message ?: "Error al actualizar correo"
            }
            Result.failure(Exception(mensajeError))
        }
    }

    suspend fun syncEmailFromFirebase(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.success(Unit)

            // Recarga el usuario para obtener el email actualizado de Firebase Auth
            user.reload().await()

            val currentEmail = user.email ?: return Result.success(Unit)
            val uid          = user.uid

            // Actualiza Firestore con el email real de Firebase Auth
            usersRef.document(uid).update("email", currentEmail).await()
            cachedUser = cachedUser?.copy(email = currentEmail)

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    //Cambiar contraseña

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No autenticado")

            val isEmailProvider  = user.providerData.any {
                it.providerId == EmailAuthProvider.PROVIDER_ID
            }
            val isGoogleProvider = user.providerData.any {
                it.providerId == "google.com"
            }

            when {
                isEmailProvider -> {
                    val credential = EmailAuthProvider.getCredential(
                        user.email ?: throw Exception("No se encontró el correo"),
                        currentPassword
                    )
                    user.reauthenticate(credential).await()
                    user.updatePassword(newPassword).await()
                }
                isGoogleProvider && !isEmailProvider -> {
                    throw Exception(
                        "Tu cuenta usa Google. Agrega una contraseña desde tu perfil."
                    )
                }
                else -> throw Exception("Proveedor de autenticación no reconocido.")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            val mensajeError = when {
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
                        e.message?.contains("wrong-password") == true ->
                    "La contraseña actual es incorrecta"
                e.message?.contains("REQUIRES_RECENT_LOGIN") == true ||
                        e.message?.contains("requires-recent-login") == true ->
                    "Por seguridad, cierra sesión e inicia sesión nuevamente antes de cambiar la contraseña"
                e.message?.contains("weak-password") == true ->
                    "La nueva contraseña es muy débil"
                e.message?.contains("network") == true ->
                    "Error de conexión. Verifica tu internet"
                else -> e.message ?: "Error al cambiar la contraseña"
            }
            Result.failure(Exception(mensajeError))
        }
    }

    // Agregar contraseña a cuenta Google
    suspend fun addPasswordToGoogleAccount(newPassword: String): Result<Unit> {
        return try {
            val user  = auth.currentUser ?: throw Exception("No autenticado")
            val email = user.email ?: throw Exception("No se encontró el correo")
            val credential = EmailAuthProvider.getCredential(email, newPassword)
            user.linkWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    // ─── Imagen de perfil ─────────────────────────────────────────

    suspend fun updateProfileImage(imageUri: Uri): Result<String> {
        return try {
            val uid   = auth.currentUser?.uid ?: throw Exception("No autenticado")
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: throw Exception("No se pudo leer la imagen")

            // Borra la imagen anterior si existe
            val currentPhotoUrl = cachedUser?.photoUrl ?: ""
            if (currentPhotoUrl.isNotEmpty()) {
                runCatching {
                    val marker = "profile-images/"
                    val idx    = currentPhotoUrl.indexOf(marker)
                    if (idx != -1) {
                        val oldPath = currentPhotoUrl.substring(idx + marker.length)
                        bucket.delete(listOf(oldPath))
                    }
                }
            }

            // Sube la nueva imagen
            val path = "profiles/$uid/${UUID.randomUUID()}.jpg"
            bucket.upload(path, bytes) { upsert = false }
            val url = bucket.publicUrl(path)

            usersRef.document(uid).update("photoUrl", url).await()
            cachedUser = cachedUser?.copy(photoUrl = url)
            Result.success(url)
        } catch (e: Exception) { Result.failure(e) }
    }
    suspend fun deleteProfileImage(): Result<Unit> {
        return try {
            val uid             = auth.currentUser?.uid ?: throw Exception("No autenticado")
            val currentPhotoUrl = cachedUser?.photoUrl ?: ""

            if (currentPhotoUrl.isNotEmpty()) {
                val marker = "profile-images/"
                val idx    = currentPhotoUrl.indexOf(marker)
                if (idx != -1) {
                    val path = currentPhotoUrl.substring(idx + marker.length)
                    bucket.delete(listOf(path))
                }
            }

            usersRef.document(uid).update("photoUrl", "").await()
            cachedUser = cachedUser?.copy(photoUrl = "")
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Session ─────────────────────────────────────────────────

    fun logout()                  { cachedUser = null; auth.signOut() }
    fun getCurrentUserId(): String?   = auth.currentUser?.uid
    fun getCurrentUserName(): String? = cachedUser?.name
    fun getCurrentUser(): User?       = cachedUser

    // ─── Admin ───────────────────────────────────────────────────

    suspend fun getUsers(): Result<List<User>> {
        return try {
            Result.success(usersRef.get().await().toObjects(User::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit> {
        return try {
            // Verifica que el usuario actual sea ADMIN
            val currentUid = auth.currentUser?.uid
                ?: throw Exception("No autenticado")
            val currentSnapshot = usersRef.document(currentUid).get().await()
            val currentRole = currentSnapshot.getString("role")
            if (currentRole != UserRole.ADMIN.name) {
                throw Exception("No tienes permisos para cambiar roles")
            }

            usersRef.document(uid).update("role", role.name).await()

            // Actualiza caché si es el mismo usuario
            if (uid == currentUid) {
                cachedUser = cachedUser?.copy(role = role)
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            // Verifica que el usuario actual sea ADMIN
            val currentUid = auth.currentUser?.uid
                ?: throw Exception("No autenticado")
            val currentSnapshot = usersRef.document(currentUid).get().await()
            val currentRole = currentSnapshot.getString("role")
            if (currentRole != UserRole.ADMIN.name) {
                throw Exception("No tienes permisos para eliminar usuarios")
            }

            // No puede eliminarse a sí mismo
            if (uid == currentUid) {
                throw Exception("No puedes eliminar tu propia cuenta desde aquí")
            }

            // Elimina documento de Firestore
            usersRef.document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Helper ──────────────────────────────────────────────────

    private fun userToMap(user: User) = mapOf(
        "uid"      to user.uid,
        "name"     to user.name,
        "email"    to user.email,
        "role"     to user.role.name,
        "photoUrl" to user.photoUrl
    )
}