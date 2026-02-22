package app.compose.appoxxo.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import app.compose.appoxxo.R

@Composable
fun ImagePickerSection(
    currentImageUrl: String = "",
    selectedUri: Uri? = null,
    onImageSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onImageSelected(it) } }

    val hasImage = selectedUri != null || currentImageUrl.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Imagen del producto",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasImage) {
                TextButton(
                    onClick      = { launcher.launch("image/*") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Cambiar",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (hasImage) Color.Transparent
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = if (hasImage) 1.5.dp else 1.dp,
                    color = if (selectedUri != null)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else if (currentImageUrl.isNotEmpty())
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedUri != null -> {
                    AsyncImage(
                        model              = selectedUri,
                        contentDescription = "Imagen seleccionada",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
                currentImageUrl.isNotEmpty() -> {
                    AsyncImage(
                        model              = currentImageUrl,
                        contentDescription = "Imagen actual",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint     = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Toca para agregar imagen",
                            style    = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Opcional · JPG, PNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedUri != null,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    "Imagen lista para subir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}