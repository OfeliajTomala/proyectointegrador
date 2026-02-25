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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R

@Composable
fun HelpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📚 Manual de usuario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = Color(0xFFD32F2F)
        )

        Text(
            "Selecciona una sección para aprender cómo usar la app.",
            style  = MaterialTheme.typography.bodyMedium,
            color  = Color(0xFF6B6B72),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        HelpItemNew(
            emoji = "🏠",
            title = "Dashboard",
            content = "El Dashboard muestra un resumen general del inventario.\n\n• Total de productos registrados\n• Stock total acumulado\n• Productos con stock bajo (≤5 unidades)\n• Valor total del inventario\n• Últimos productos agregados"
        )

        HelpItemNew(
            emoji = "📦",
            title = "Productos",
            content = "Aquí puedes gestionar todos los productos.\n\n• Toca ✏️ para editar\n• Toca 🗑️ para eliminar\n• Toca el ícono de movimientos para ver historial\n• Usa el botón + para agregar nuevos productos"
        )

        HelpItemNew(
            emoji = "➕",
            title = "Agregar/Editar",
            content = "Al crear o editar un producto:\n\n• Imagen: Opcional\n• Nombre: Requerido\n• Código: Opcional (5-10 caracteres)\n• Precio: Número con hasta 2 decimales\n• Stock: Número entero\n\nEl botón Guardar se activa solo con campos válidos."
        )

        HelpItemNew(
            emoji = "📋",
            title = "Movimientos",
            content = "Registra entradas y salidas de inventario.\n\n• Filtra por tipo (Entrada/Salida)\n• Busca por nombre de producto\n• Usa filtros de fecha (Desde/Hasta)\n• Limpia filtros con el botón X"
        )

        HelpItemNew(
            emoji = "⚠️",
            title = "Alertas",
            content = "Monitorea productos con stock bajo.\n\n🔴 Rojo = Sin stock (0 unidades)\n🟠 Naranja = Stock bajo (1-5 unidades)\n\nLos productos se ordenan de menor a mayor stock para priorizar lo más urgente."
        )

        HelpItemNew(
            emoji = "👤",
            title = "Perfil",
            content = "Personaliza tu cuenta:\n\n• Cambiar foto de perfil\n• Editar nombre y correo\n• Cambiar contraseña\n• Alternar modo oscuro\n• Cambiar paleta de colores"
        )

        HelpItemNew(
            emoji = "👥",
            title = "Usuarios (Solo Admin)",
            content = "Gestiona los usuarios del sistema desde el menú lateral.\n\n• Ver todos los usuarios\n• Cambiar rol (Admin/Encargado/Cajero)\n• Eliminar usuarios\n\n⚠️ Ten cuidado: los cambios afectan inmediatamente los permisos del usuario."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HelpItemNew(emoji: String, title: String, content: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
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
                Text(
                    emoji,
                    fontSize = 24.sp
                )
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f),
                    fontSize   = 15.sp,
                    color      = Color(0xFF1A1A1A)
                )
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_arrow_upward
                        else R.drawable.ic_arrow_downward
                    ),
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint               = Color(0xFFD32F2F),
                    modifier           = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = Color(0xFFE2E2E6))
                    Text(
                        text     = content,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = Color(0xFF6B6B72),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}