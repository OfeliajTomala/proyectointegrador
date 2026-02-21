package app.compose.appoxxo.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val currentUser      by viewModel.currentUser.collectAsState()
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showThemeSheet   = remember { mutableStateOf(false) }

    if (showLogoutDialog.value) {
        AppConfirmDialog(
            title        = "Cerrar sesión",
            message      = "¿Estás seguro que deseas cerrar sesión?",
            confirmLabel = "Cerrar sesión",
            onConfirm    = { showLogoutDialog.value = false; onLogout() },
            onDismiss    = { showLogoutDialog.value = false }
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

        // ── Avatar ────────────────────────────────────────────────
        val roleColor = when (currentUser?.role) {
            UserRole.ADMIN     -> MaterialTheme.colorScheme.error
            UserRole.ENCARGADO -> MaterialTheme.colorScheme.tertiary
            else               -> MaterialTheme.colorScheme.primary
        }

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(roleColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = currentUser?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = roleColor
            )
        }

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
                    painter           = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = null,
                    tint              = MaterialTheme.colorScheme.primary,
                    modifier          = Modifier.size(24.dp)
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
                    painter           = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint              = MaterialTheme.colorScheme.onSurfaceVariant
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
            // FIX: uid sin .plus() — string template directo
            val uidPreview = currentUser?.uid?.let {
                if (it.length > 12) "${it.take(12)}…" else it
            } ?: "—"

            ProfileInfoRow(label = "Nombre", value = currentUser?.name  ?: "—")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(label = "Correo", value = currentUser?.email ?: "—")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(label = "Rol",    value = currentUser?.role?.name ?: "—")
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(label = "UID",    value = uidPreview)
        }

        // FIX: Spacer sin weight — dentro de Column con spacedBy weight no funciona
        Spacer(modifier = Modifier.height(8.dp))

        // ── Cerrar sesión ─────────────────────────────────────────
        AppOutlinedButton(
            text     = "Cerrar sesión",
            onClick  = { showLogoutDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            color    = MaterialTheme.colorScheme.error,
            leadingIcon = {
                // FIX: tint explícito para que coincida con el color del botón
                Icon(
                    painter           = painterResource(id = R.drawable.ic_exittoapp),
                    contentDescription = null,
                    tint              = MaterialTheme.colorScheme.error
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Selector de tema ─────────────────────────────────────────────────────────
@Composable
private fun ThemeSelectorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paleta de colores", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppPalettes.all.forEach { palette ->
                    // FIX: comparar por nombre en lugar de referencia para evitar
                    // problemas si AppColorPalette no implementa equals()
                    val isSelected = ThemeConfig.selectedPalette.name == palette.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else
                                    Color.Transparent
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
                                painter           = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = "Seleccionado",
                                tint              = MaterialTheme.colorScheme.primary,
                                modifier          = Modifier.size(20.dp)
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