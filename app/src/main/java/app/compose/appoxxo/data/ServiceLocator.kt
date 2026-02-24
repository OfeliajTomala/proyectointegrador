package app.compose.appoxxo.data

import app.compose.appoxxo.BuildConfig
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object ServiceLocator {

    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    // ─── Supabase ─────────────────────────────────────────────────
    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Storage)
        }
    }

    // ─── ProductRepository ────────────────────────────────────────
    val productRepository: ProductRepository by lazy {
        ProductRepository()
    }

    // ─── AuthRepository — Singleton sin Context ───────────────────
    // Sin Context no hay memory leak
    // El Context para imágenes vive en AuthViewModel (Application)
    val authRepository: AuthRepository by lazy {
        AuthRepository(supabaseClient)
    }

    // ImageRepository NO vive aquí porque necesita Context.
    // Se instancia en AppViewModelFactory con applicationContext.
}