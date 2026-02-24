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
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.data.model.UserRole
import app.compose.appoxxo.data.util.UiState
import app.compose.appoxxo.ui.components.AppConfirmDialog
import app.compose.appoxxo.ui.components.AppEmptyState
import app.compose.appoxxo.viewmodel.UserViewModel


@Composable
fun UsersScreen(viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val userToDelete = remember { mutableStateOf<User?>(null) }

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
                    modifier = Modifier.fillMaxSize(),
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
                            modifier = Modifier.padding(vertical = 4.dp)
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
    //var expanded by remember { mutableStateOf(false) }

    val roleColor = when (user.role) {
        UserRole.ADMIN     -> MaterialTheme.colorScheme.error
        UserRole.ENCARGADO -> MaterialTheme.colorScheme.tertiary
        UserRole.CAJERO    -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con inicial
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge de rol
                Badge(containerColor = roleColor) {
                    Text(
                        user.role.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de rol
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rol:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                UserRole.entries.forEach { role ->
                    FilterChip(
                        selected = user.role == role,
                        onClick = { if (user.role != role) onRoleChange(role) },
                        label = {
                            Text(
                                role.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.padding(end = 6.dp),
                        enabled = !isLoading
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón eliminar
                IconButton(
                    onClick = onDelete,
                    enabled = !isLoading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Eliminar usuario",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}