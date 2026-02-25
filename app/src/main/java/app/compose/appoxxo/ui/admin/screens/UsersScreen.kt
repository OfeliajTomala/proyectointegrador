package app.compose.appoxxo.ui.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppConfirmDialog
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.viewmodel.UserViewModel

@Composable
fun UsersScreen(viewModel: UserViewModel) {
    val users             by viewModel.users.collectAsState()
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }
    val userToDelete       = remember { mutableStateOf<User?>(null) }

    val userToDeleteSnapshot = userToDelete.value
    if (userToDeleteSnapshot != null) {
        AppConfirmDialog(
            title        = "Eliminar usuario",
            message      = "¿Estás seguro que deseas eliminar a ${userToDeleteSnapshot.name}?",
            confirmLabel = "Eliminar",
            onConfirm    = {
                viewModel.deleteUser(userToDeleteSnapshot.uid)
                userToDelete.value = null
            },
            onDismiss    = { userToDelete.value = null }
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        when {
            uiState is UiState.Loading && users.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            users.isEmpty() -> {
                AppEmptyState(
                    iconRes  = R.drawable.ic_person,
                    title    = "Sin usuarios registrados",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top    = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                        start  = 16.dp,
                        end    = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${users.size} usuario${if (users.size != 1) "s" else ""} registrados",
                            style    = MaterialTheme.typography.labelLarge,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }

                    items(users, key = { it.uid }) { user ->
                        UserCard(
                            user         = user,
                            isLoading    = uiState is UiState.Loading,
                            onRoleChange = { newRole -> viewModel.updateRole(user.uid, newRole) },
                            onDelete     = { userToDelete.value = user }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    isLoading: Boolean,
    onRoleChange: (UserRole) -> Unit,
    onDelete: () -> Unit
) {
    val roleColor = when (user.role) {
        UserRole.ADMIN     -> Color(0xFFFF0000)
        UserRole.ENCARGADO -> Color(0xFFFFC107)
        UserRole.CAJERO    -> Color(0xFF0E675F)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con inicial
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = roleColor,
                        fontSize   = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.name,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                    Text(
                        user.email,
                        style  = MaterialTheme.typography.bodySmall,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Badge de rol
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = roleColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, roleColor.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        user.role.name,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = roleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de rol
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rol:",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier   = Modifier.padding(end = 8.dp)
                )

                UserRole.entries.forEach { role ->
                    val isSelected = user.role == role

                    FilterChip(
                        selected = isSelected,
                        onClick  = { if (!isSelected) onRoleChange(role) },
                        label    = { Text(role.name, fontSize = 11.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            containerColor              = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor      = roleColor.copy(alpha = 0.1f),
                            labelColor                  = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedLabelColor          = roleColor
                        ),
                        modifier = Modifier.padding(end = 5.dp),
                        enabled  = !isLoading
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick  = onDelete,
                    enabled  = !isLoading,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Eliminar usuario",
                        tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}