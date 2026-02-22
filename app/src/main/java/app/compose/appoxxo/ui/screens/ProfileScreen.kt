package app.compose.appoxxo.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.ui.components.AppConfirmDialog
import app.compose.appoxxo.ui.components.AppOutlinedButton
import app.compose.appoxxo.ui.theme.AppPalettes
import app.compose.appoxxo.ui.theme.ThemeConfig
import app.compose.appoxxo.viewmodel.AuthViewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit,
    onEditName: () -> Unit,
    onEditEmail: () -> Unit,
    onChangePassword: () -> Unit,
    onAddPassword: () -> Unit,
    onSecurityPolicy: () -> Unit
) {
    val currentUser           by viewModel.currentUser.collectAsState()
    val showLogoutDialog       = remember { mutableStateOf(false) }
    val showThemeSheet         = remember { mutableStateOf(false) }
    val showDeletePhotoDialog  = remember { mutableStateOf(false) }

    // Detecta proveedores activos
    val firebaseUser      = FirebaseAuth.getInstance().currentUser
    val hasEmailProvider  = firebaseUser?.providerData?.any { it.providerId == "password" } == true
    val hasGoogleProvider = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateProfileImage(it) } }

    if (showLogoutDialog.value) {
        AppConfirmDialog(
            title        = "Cerrar sesión",
            message      = "¿Estás seguro que deseas cerrar sesión?",
            confirmLabel = "Cerrar sesión",
            onConfirm    = { showLogoutDialog.value = false; onLogout() },
            onDismiss    = { showLogoutDialog.value = false }
        )
    }

    if (showDeletePhotoDialog.value) {
        AppConfirmDialog(
            title        = "Eliminar foto",
            message      = "¿Estás seguro que deseas eliminar tu foto de perfil?",
            confirmLabel = "Eliminar",
            onConfirm    = { showDeletePhotoDialog.value = false; viewModel.deleteProfileImage() },
            onDismiss    = { showDeletePhotoDialog.value = false }
        )
    }

    if (showThemeSheet.value) {
        ThemeSelectorDialog(onDismiss = { showThemeSheet.value = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        val roleColor = when (currentUser?.role) {
            UserRole.ADMIN     -> MaterialTheme.colorScheme.error
            UserRole.ENCARGADO -> MaterialTheme.colorScheme.tertiary
            else               -> MaterialTheme.colorScheme.primary
        }

        // ── Avatar ────────────────────────────────────────────────
        Box(
            modifier         = Modifier.size(100.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.15f))
                    .border(2.dp, roleColor.copy(alpha = 0.4f), CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.photoUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model              = currentUser!!.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier           = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Text(
                        text       = currentUser?.name?.firstOrNull()
                            ?.uppercaseChar()?.toString() ?: "?",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color      = roleColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Cambiar foto",
                    tint               = Color.White,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }

        // Botón eliminar foto
        if (currentUser?.photoUrl?.isNotEmpty() == true) {
            TextButton(onClick = { showDeletePhotoDialog.value = true }) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_delete),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Eliminar foto",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // ── Info ──────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                currentUser?.name ?: "Usuario",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Badge(containerColor = roleColor) {
                Text(
                    currentUser?.role?.name ?: "—",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }

        HorizontalDivider()

        // ── Apariencia ────────────────────────────────────────────
        Text(
            "Apariencia",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo oscuro", fontWeight = FontWeight.Medium)
                    Text(
                        "Cambiar entre claro y oscuro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked         = ThemeConfig.isDarkMode,
                    onCheckedChange = { ThemeConfig.isDarkMode = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeSheet.value = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(ThemeConfig.selectedPalette.primary)
                            .align(Alignment.TopStart)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(ThemeConfig.selectedPalette.secondary)
                            .align(Alignment.BottomEnd)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Paleta de colores", fontWeight = FontWeight.Medium)
                    Text(
                        ThemeConfig.selectedPalette.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter            = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Cuenta ────────────────────────────────────────────────
        Text(
            "Cuenta",
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp)
        ) {
            ProfileEditRow(
                label   = "Nombre",
                value   = currentUser?.name ?: "—",
                onClick = onEditName
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileEditRow(
                label   = "Correo",
                value   = currentUser?.email ?: "—",
                onClick = onEditEmail
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Contraseña — según proveedor
            when {
                hasEmailProvider -> {
                    ProfileEditRow(
                        label   = "Contraseña",
                        value   = "••••••••",
                        onClick = onChangePassword
                    )
                }
                hasGoogleProvider && !hasEmailProvider -> {
                    ProfileEditRow(
                        label   = "Contraseña",
                        value   = "Agregar contraseña",
                        onClick = onAddPassword
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Rol — no editable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rol",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.width(90.dp)
                )
                Text(
                    currentUser?.role?.name ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Políticas de seguridad
            ProfileEditRow(
                label   = "Políticas",
                value   = "Políticas de seguridad",
                onClick = onSecurityPolicy
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppOutlinedButton(
            text     = "Cerrar sesión",
            onClick  = { showLogoutDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            color    = MaterialTheme.colorScheme.error,
            leadingIcon = {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_exittoapp),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileEditRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.width(90.dp)
        )
        Text(
            value,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter            = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = "Editar",
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ThemeSelectorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paleta de colores", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppPalettes.all.forEach { palette ->
                    val isSelected = ThemeConfig.selectedPalette.name == palette.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else Color.Transparent
                            )
                            .clickable { ThemeConfig.selectedPalette = palette }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                                    .align(Alignment.TopStart)
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(palette.secondary)
                                    .align(Alignment.BottomEnd)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            palette.name,
                            style      = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = "Seleccionado",
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Listo") }
        }
    )
}