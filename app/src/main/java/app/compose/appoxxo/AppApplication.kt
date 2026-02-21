package app.compose.appoxxo

import android.app.Application

/**
 * Application personalizada.
 * Registra esta clase en AndroidManifest.xml:
 *
 *   <application android:name=".AppApplication" ...>
 *
 * ImageRepository ya no se inicializa aquí — vive en AppViewModelFactory
 * con applicationContext para evitar memory leaks.
 */
class AppApplication : Application()