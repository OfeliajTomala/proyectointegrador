package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R
// ─── SecurityPolicyScreen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Políticas de seguridad", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PolicySection("1. Almacenamiento de datos",     "Toda la información de usuarios, productos y movimientos se almacena en Firebase Firestore, un servicio de base de datos seguro de Google. Las imágenes de productos y de perfil se almacenan en Supabase Storage con acceso controlado mediante políticas de seguridad.")
            PolicySection("2. Autenticación",              "El acceso a la aplicación requiere autenticación mediante correo y contraseña, o mediante cuenta de Google. Las sesiones son gestionadas por Firebase Authentication. Las contraseñas nunca se almacenan en texto plano.")
            PolicySection("3. Roles y permisos",           "La aplicación maneja tres niveles de acceso:\n\n• ADMIN — acceso total: gestión de usuarios, productos y movimientos.\n• ENCARGADO — puede gestionar productos y registrar movimientos.\n• CAJERO — puede consultar productos y registrar movimientos, sin acceso a gestión de usuarios ni agregar productos.")
            PolicySection("4. Imágenes",                   "Las imágenes subidas a la aplicación (fotos de perfil e imágenes de productos) se almacenan en Supabase Storage. Al actualizar una imagen, la versión anterior es eliminada automáticamente. Al eliminar un producto, su imagen asociada también es eliminada del almacenamiento.")
            PolicySection("5. Cambio de credenciales",     "Para cambiar el correo electrónico o la contraseña, se requiere ingresar la contraseña actual como verificación de identidad. Esto protege la cuenta ante accesos no autorizados.")
            PolicySection("6. Cierre de sesión",           "Al cerrar sesión, todos los datos en caché local son eliminados. La sesión queda invalidada de forma inmediata en el dispositivo.")
            PolicySection("7. Eliminación de datos",       "La eliminación de un usuario desde el panel de administración elimina su documento de Firestore. Las imágenes asociadas al perfil deben eliminarse manualmente desde el perfil antes de eliminar la cuenta.")
            PolicySection("8. Responsabilidad",            "El administrador del sistema es responsable de gestionar correctamente los roles y accesos de los usuarios. Se recomienda no compartir credenciales de acceso y cerrar sesión al terminar de usar la aplicación en dispositivos compartidos.")

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(50)
                        )
                )
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    fontSize   = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}
