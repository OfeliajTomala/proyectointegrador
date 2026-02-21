package app.compose.appoxxo.data

import app.compose.appoxxo.BuildConfig
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage



// Inyección de dependencias sencilla sin Hilt/Koin
object ServiceLocator {

    // ── Supabase ──────────────────────────────────────────────────────────────
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Storage)
        }
    }

    // ── Firebase repositories (sin Context, seguros) ──────────────────────────
    val authRepository: AuthRepository       by lazy { AuthRepository() }
    val productRepository: ProductRepository by lazy { ProductRepository() }

    // ImageRepository NO vive aquí porque necesita Context.
    // Se instancia en AppViewModelFactory con applicationContext.
}
