package app.compose.appoxxo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.compose.appoxxo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual de usuario", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Selecciona una sección para ver su guía de uso.",
                style  = MaterialTheme.typography.bodyMedium,
                color  = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HelpItem(
                iconRes = R.drawable.ic_home,
                title   = "Dashboard",
                content = "El Dashboard muestra un resumen general del inventario:\n\n" +
                        "• Total de productos registrados.\n" +
                        "• Stock total acumulado.\n" +
                        "• Cantidad de productos con stock bajo (5 unidades o menos).\n" +
                        "• Valor total del inventario (precio × stock de cada producto).\n" +
                        "• Lista de productos con stock bajo para atención inmediata.\n" +
                        "• Últimos productos agregados al sistema.\n\n" +
                        "Toca 'Ver todos' para navegar directamente a Productos o Alertas."
            )

            HelpItem(
                iconRes = R.drawable.ic_shopping_cart,
                title   = "Productos",
                content = "Aquí puedes ver todos los productos del inventario.\n\n" +
                        "• Toca el ícono ✏️ para editar un producto.\n" +
                        "• Toca el ícono 🗑️ para eliminar un producto (se pedirá confirmación).\n" +
                        "• Toca el ícono de movimientos para ver el historial de entradas y salidas.\n" +
                        "• Usa el botón + (esquina inferior derecha) para agregar un nuevo producto.\n\n" +
                        "Nota: El botón + solo está disponible para ADMIN y ENCARGADO."
            )

            HelpItem(
                iconRes = R.drawable.ic_add,
                title   = "Agregar / Editar producto",
                content = "Al agregar o editar un producto:\n\n" +
                        "• Imagen — opcional, toca el recuadro para seleccionar una foto.\n" +
                        "• Nombre — obligatorio.\n" +
                        "• Código — opcional, identificador interno.\n" +
                        "• Precio — solo números, acepta hasta 2 decimales.\n" +
                        "• Stock — solo números enteros.\n\n" +
                        "El botón Guardar se activa solo cuando los campos obligatorios son válidos."
            )

            HelpItem(
                iconRes = R.drawable.ic_list,
                title   = "Movimientos",
                content = "Registra y consulta las entradas y salidas de productos.\n\n" +
                        "• Usa los filtros de tipo (Todos / Entradas / Salidas) para filtrar.\n" +
                        "• Busca por nombre de producto con la barra de búsqueda.\n" +
                        "• Filtra por rango de fechas con los botones Desde / Hasta.\n" +
                        "• Toca el ícono X para limpiar el filtro de fechas.\n\n" +
                        "Para registrar un movimiento en un producto específico, ve a Productos " +
                        "y toca el ícono de movimientos en la tarjeta del producto."
            )

            HelpItem(
                iconRes = R.drawable.ic_notifications,
                title   = "Alertas",
                content = "Muestra todos los productos que necesitan atención por stock bajo.\n\n" +
                        "• Rojo — Sin stock (0 unidades).\n" +
                        "• Amarillo — Stock bajo (1 a 5 unidades).\n\n" +
                        "Los productos aparecen ordenados de menor a mayor stock para priorizar " +
                        "los más urgentes. Si todos los productos tienen stock suficiente, " +
                        "verás el mensaje '¡Todo en orden!'."
            )

            HelpItem(
                iconRes = R.drawable.ic_person,
                title   = "Perfil",
                content = "Desde tu perfil puedes:\n\n" +
                        "• Cambiar tu foto de perfil tocando el avatar o el ícono ✏️.\n" +
                        "• Eliminar tu foto de perfil con el botón 'Eliminar foto'.\n" +
                        "• Cambiar tu nombre desde la fila 'Nombre'.\n" +
                        "• Cambiar tu correo electrónico (requiere contraseña actual).\n" +
                        "• Cambiar tu contraseña (requiere contraseña actual).\n" +
                        "• Alternar entre modo claro y modo oscuro.\n" +
                        "• Cambiar la paleta de colores de la aplicación.\n" +
                        "• Cerrar sesión."
            )

            HelpItem(
                iconRes = R.drawable.ic_person,
                title   = "Usuarios (solo ADMIN)",
                content = "Disponible solo para el rol ADMIN desde el menú lateral.\n\n" +
                        "• Muestra todos los usuarios registrados en el sistema.\n" +
                        "• Puedes cambiar el rol de cualquier usuario tocando los chips " +
                        "ADMIN / ENCARGADO / CAJERO.\n" +
                        "• Puedes eliminar un usuario tocando el ícono 🗑️ (se pedirá confirmación).\n\n" +
                        "Ten cuidado al cambiar roles — afecta inmediatamente los permisos del usuario."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── HelpItem — sección expandible ───────────────────────────────────────────
@Composable
private fun HelpItem(
    iconRes: Int,
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column {
            // ── Cabecera ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter            = painterResource(id = iconRes),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_arrow_upward
                        else R.drawable.ic_arrow_downward
                    ),
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // ── Contenido expandible ──────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    Text(
                        text     = content,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 12.dp,
                            bottom = 16.dp
                        )
                    )
                }
            }
        }
    }
}