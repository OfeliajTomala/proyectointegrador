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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {

        // ─── Header con gradiente rojo ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text       = currentUser?.name ?: "Usuario",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text  = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badge de rol
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text     = currentUser?.role?.name ?: "—",
                        color    = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
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
                            contentDescription = item.label,
                            modifier           = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text       = item.label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize   = 14.sp
                        )
                    },
                    selected = selected,
                    onClick  = { onNavigate(item.navItem.route) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                    colors   = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectedIconColor      = MaterialTheme.colorScheme.primary,
                        selectedTextColor      = MaterialTheme.colorScheme.primary
                    )
                )
            }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ─── Cerrar sesión ────────────────────────────────────────
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_exittoapp),
                    contentDescription = "Cerrar sesión",
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text       = "Cerrar sesión",
                    color      = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
            },
            selected = false,
            onClick  = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}