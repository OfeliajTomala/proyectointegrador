package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.*
import app.compose.appoxxo.R

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    isLoggedIn: Boolean
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation) // ← tu JSON aquí
    )
    val progress by animateLottieCompositionAsState(
        composition  = composition,
        iterations   = 1,
        isPlaying    = true
    )

    // Cuando termina la animación navega según sesión
    LaunchedEffect(progress) {
        if (progress == 1f) {
            if (isLoggedIn) onNavigateToDashboard()
            else onNavigateToLogin()
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress    = { progress },
            modifier    = Modifier.fillMaxSize(0.6f)
        )
    }
}