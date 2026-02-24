package app.compose.appoxxo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R

// ─── HelpScreen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual de usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Selecciona una sección para ver su guía de uso.",
                style  = MaterialTheme.typography.bodyMedium,
                color  = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp),
                fontSize = 14.sp
            )

            HelpItem(iconRes = R.drawable.ic_home,          title = "Dashboard",             content = "El Dashboard muestra un resumen general del inventario:\n\n• Total de productos registrados.\n• Stock total acumulado.\n• Cantidad de productos con stock bajo (5 unidades o menos).\n• Valor total del inventario (precio × stock de cada producto).\n• Lista de productos con stock bajo para atención inmediata.\n• Últimos productos agregados al sistema.\n\nToca 'Ver todos' para navegar directamente a Productos o Alertas.")
            HelpItem(iconRes = R.drawable.ic_shopping_cart, title = "Productos",              content = "Aquí puedes ver todos los productos del inventario.\n\n• Toca el ícono ✏️ para editar un producto.\n• Toca el ícono 🗑️ para eliminar un producto (se pedirá confirmación).\n• Toca el ícono de movimientos para ver el historial de entradas y salidas.\n• Usa el botón + (esquina inferior derecha) para agregar un nuevo producto.\n\nNota: El botón + solo está disponible para ADMIN y ENCARGADO.")
            HelpItem(iconRes = R.drawable.ic_add,           title = "Agregar / Editar",       content = "Al agregar o editar un producto:\n\n• Imagen — opcional, toca el recuadro para seleccionar una foto.\n• Nombre — obligatorio.\n• Código — opcional, identificador interno.\n• Precio — solo números, acepta hasta 2 decimales.\n• Stock — solo números enteros.\n\nEl botón Guardar se activa solo cuando los campos obligatorios son válidos.")
            HelpItem(iconRes = R.drawable.ic_list,          title = "Movimientos",            content = "Registra y consulta las entradas y salidas de productos.\n\n• Usa los filtros de tipo (Todos / Entradas / Salidas) para filtrar.\n• Busca por nombre de producto con la barra de búsqueda.\n• Filtra por rango de fechas con los botones Desde / Hasta.\n• Toca el ícono X para limpiar el filtro de fechas.\n\nPara registrar un movimiento en un producto específico, ve a Productos y toca el ícono de movimientos en la tarjeta del producto.")
            HelpItem(iconRes = R.drawable.ic_notifications, title = "Alertas",                content = "Muestra todos los productos que necesitan atención por stock bajo.\n\n• Rojo — Sin stock (0 unidades).\n• Amarillo — Stock bajo (1 a 5 unidades).\n\nLos productos aparecen ordenados de menor a mayor stock para priorizar los más urgentes. Si todos los productos tienen stock suficiente, verás el mensaje '¡Todo en orden!'.")
            HelpItem(iconRes = R.drawable.ic_person,        title = "Perfil",                 content = "Desde tu perfil puedes:\n\n• Cambiar tu foto de perfil tocando el avatar o el ícono ✏️.\n• Eliminar tu foto de perfil con el botón 'Eliminar foto'.\n• Cambiar tu nombre desde la fila 'Nombre'.\n• Cambiar tu correo electrónico (requiere contraseña actual).\n• Cambiar tu contraseña (requiere contraseña actual).\n• Alternar entre modo claro y modo oscuro.\n• Cambiar la paleta de colores de la aplicación.\n• Cerrar sesión.")
            HelpItem(iconRes = R.drawable.ic_person,        title = "Usuarios (solo ADMIN)",  content = "Disponible solo para el rol ADMIN desde el menú lateral.\n\n• Muestra todos los usuarios registrados en el sistema.\n• Puedes cambiar el rol de cualquier usuario tocando los chips ADMIN / ENCARGADO / CAJERO.\n• Puedes eliminar un usuario tocando el ícono 🗑️ (se pedirá confirmación).\n\nTen cuidado al cambiar roles — afecta inmediatamente los permisos del usuario.")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpItem(iconRes: Int, title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter            = painterResource(id = iconRes),
                        contentDescription = null,
                        tint               = if (expanded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(20.dp)
                    )
                }
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                    fontSize   = 14.sp,
                    color      = if (expanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_arrow_upward
                        else R.drawable.ic_arrow_downward
                    ),
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint               = if (expanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Text(
                        text     = content,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 14.dp,
                            bottom = 18.dp
                        )
                    )
                }
            }
        }
    }
}
