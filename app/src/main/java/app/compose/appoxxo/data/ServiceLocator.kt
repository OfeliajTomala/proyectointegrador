package app.compose.appoxxo.data

import android.content.Context
import app.compose.appoxxo.BuildConfig
import app.compose.appoxxo.data.repository.AuthRepository
import app.compose.appoxxo.data.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object ServiceLocator {

    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_KEY) {
            install(Storage)
        }
    }

    val productRepository: ProductRepository by lazy { ProductRepository() }

    // AuthRepository ahora necesita Context y Supabase — se crea en la factory
    fun createAuthRepository(context: Context): AuthRepository =
        AuthRepository(supabaseClient, context)
}
    // ImageRepository NO vive aquí porque necesita Context.
    // Se instancia en AppViewModelFactory con applicationContext.

