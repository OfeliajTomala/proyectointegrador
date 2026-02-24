package app.compose.appoxxo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R
import app.compose.appoxxo.data.model.User
import app.compose.appoxxo.ui.NavItem
import coil.compose.AsyncImage

// ─── Items del Drawer ─────────────────────────────────────────────────────────

private data class DrawerItem(
    val navItem: NavItem,
    val label: String,
    val iconRes: Int,
    val adminOnly: Boolean = false
)

private val drawerItems = listOf(
    DrawerItem(NavItem.Dashboard,  "Dashboard",   R.drawable.ic_home),
    DrawerItem(NavItem.Movements,  "Movimientos", R.drawable.ic_list),
    DrawerItem(NavItem.Alerts,     "Alertas",     R.drawable.ic_notifications),
    DrawerItem(NavItem.Users,      "Usuarios",    R.drawable.ic_person, adminOnly = true)
)

// ─── Drawer Content ───────────────────────────────────────────────────────────

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    isAdmin: Boolean,
    currentUser: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {

        // ─── Header rojo con foto y nombre ───────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD32F2F))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth()
            ) {
                // Foto de perfil o inicial del nombre
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser?.photoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model              = currentUser.photoUrl,
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
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre del usuario
                Text(
                    text       = currentUser?.name ?: "Usuario",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email del usuario
                Text(
                    text  = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge del rol
                Badge(
                    containerColor = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text     = currentUser?.role?.name ?: "—",
                        color    = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style    = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── Items de navegación ──────────────────────────────────
        drawerItems
            .filter { !it.adminOnly || isAdmin }
            .forEach { item ->
                val selected = currentRoute == item.navItem.route
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter            = painterResource(id = item.iconRes),
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text       = item.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selected,
                    onClick  = { onNavigate(item.navItem.route) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // ─── Cerrar sesión ────────────────────────────────────────
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_exittoapp),
                    contentDescription = "Cerrar sesión",
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            label = {
                Text(
                    text       = "Cerrar sesión",
                    color      = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick  = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}