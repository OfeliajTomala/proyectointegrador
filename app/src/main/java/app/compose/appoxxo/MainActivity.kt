package app.compose.appoxxo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import app.compose.appoxxo.ui.navigation.MainScreen
import app.compose.appoxxo.ui.theme.AppOxxoTheme
import app.compose.appoxxo.ui.theme.ThemeConfig
import app.compose.appoxxo.viewmodel.AppViewModelFactory
import app.compose.appoxxo.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // ← agrega antes de super.onCreate
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory       = AppViewModelFactory(context = applicationContext)
        authViewModel     = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        credentialManager = CredentialManager.create(this)

        setContent {
            AppOxxoTheme(darkTheme = ThemeConfig.isDarkMode) {
                MainScreen(
                    authViewModel  = authViewModel,
                    onGoogleSignIn = { launchGoogleSignIn() }
                )
            }
        }
    }

    private fun launchGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result     = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authViewModel.loginWithGoogle(googleCredential.idToken)
                } else {
                    authViewModel.loginWithGoogle("")
                }
            } catch (_: GetCredentialException) {
                authViewModel.loginWithGoogle("")
            }
        }
    }
}