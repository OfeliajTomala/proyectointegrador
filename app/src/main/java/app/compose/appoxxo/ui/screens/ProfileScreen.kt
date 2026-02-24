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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val roleColor = when (currentUser?.role) {
        UserRole.ADMIN     -> MaterialTheme.colorScheme.error
        UserRole.ENCARGADO -> MaterialTheme.colorScheme.tertiary
        else               -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header con fondo degradado ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            roleColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(top = 28.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier         = Modifier.size(96.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.12f))
                            .border(2.dp, roleColor.copy(alpha = 0.35f), CircleShape)
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
                    // Botón editar foto
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    currentUser?.name ?: "Usuario",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    currentUser?.email ?: "",
                    style  = MaterialTheme.typography.bodyMedium,
                    color  = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = roleColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, roleColor.copy(alpha = 0.25f)
                    )
                ) {
                    Text(
                        currentUser?.role?.name ?: "—",
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        color      = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Botón eliminar foto
                if (currentUser?.photoUrl?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick        = { showDeletePhotoDialog.value = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.error,
                            modifier           = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Eliminar foto",
                            color  = MaterialTheme.colorScheme.error,
                            style  = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ── Contenido de secciones ─────────────────────────────────
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Apariencia ────────────────────────────────────────
            ProfileSectionLabel("Apariencia")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            ) {
                // Modo oscuro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Modo oscuro",
                            fontWeight = FontWeight.Medium,
                            fontSize   = 15.sp
                        )
                        Text(
                            "Claro / oscuro",
                            style  = MaterialTheme.typography.bodySmall,
                            color  = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked         = ThemeConfig.isDarkMode,
                        onCheckedChange = { ThemeConfig.isDarkMode = it }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // Paleta de colores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeSheet.value = true }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(38.dp)) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(ThemeConfig.selectedPalette.primary)
                                .align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(ThemeConfig.selectedPalette.secondary)
                                .align(Alignment.BottomEnd)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Paleta de colores",
                            fontWeight = FontWeight.Medium,
                            fontSize   = 15.sp
                        )
                        Text(
                            ThemeConfig.selectedPalette.name,
                            style  = MaterialTheme.typography.bodySmall,
                            color  = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }

            // ── Cuenta ────────────────────────────────────────────
            ProfileSectionLabel("Cuenta")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            ) {
                ProfileEditRow(
                    icon    = R.drawable.ic_edit,
                    label   = "Nombre",
                    value   = currentUser?.name ?: "—",
                    onClick = onEditName
                )
                ProfileRowDivider()
                ProfileEditRow(
                    icon    = R.drawable.ic_edit,
                    label   = "Correo",
                    value   = currentUser?.email ?: "—",
                    onClick = onEditEmail
                )
                ProfileRowDivider()

                when {
                    hasEmailProvider -> {
                        ProfileEditRow(
                            icon    = R.drawable.ic_visibility_off,
                            label   = "Contraseña",
                            value   = "••••••••",
                            onClick = onChangePassword
                        )
                    }
                    hasGoogleProvider && !hasEmailProvider -> {
                        ProfileEditRow(
                            icon    = R.drawable.ic_add,
                            label   = "Contraseña",
                            value   = "Agregar contraseña",
                            onClick = onAddPassword
                        )
                    }
                }

                ProfileRowDivider()

                // Rol — no clickeable
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                roleColor.copy(alpha = 0.08f),
                                RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter            = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint               = roleColor,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        "Rol",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier   = Modifier.width(80.dp)
                    )
                    Text(
                        currentUser?.role?.name ?: "—",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                }

                ProfileRowDivider()

                ProfileEditRow(
                    icon    = R.drawable.ic_notifications,
                    label   = "Políticas",
                    value   = "Seguridad",
                    onClick = onSecurityPolicy
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            AppOutlinedButton(
                text     = "Cerrar sesión",
                onClick  = { showLogoutDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                color    = MaterialTheme.colorScheme.error,
                leadingIcon = {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_exittoapp),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize   = 12.sp,
        modifier   = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ProfileRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
private fun ProfileEditRow(icon: Int, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                    RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter            = painterResource(id = icon),
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.width(80.dp),
            fontSize   = 14.sp
        )
        Text(
            value,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Icon(
            painter            = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = "Editar",
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier           = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun ThemeSelectorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(22.dp),
        title = { Text("Paleta de colores", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppPalettes.all.forEach { palette ->
                    val isSelected = ThemeConfig.selectedPalette.name == palette.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable { ThemeConfig.selectedPalette = palette }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                                    .align(Alignment.TopStart)
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
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
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize   = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(
                                painter            = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}