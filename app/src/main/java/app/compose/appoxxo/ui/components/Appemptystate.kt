package app.compose.appoxxo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado vacío reutilizable con icono, título, subtítulo y acción opcional.
 */
@Composable
fun AppEmptyState(
    iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onSurface
        )

        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}