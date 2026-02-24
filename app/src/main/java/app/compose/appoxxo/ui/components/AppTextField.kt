package app.compose.appoxxo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.compose.appoxxo.R

// ─── Reglas de contraseña segura ─────────────────────────────────────────────

data class PasswordRule(
    val label: String,
    val isMet: (String) -> Boolean
)

val passwordRules = listOf(
    PasswordRule("Mínimo 8 caracteres")                { it.length >= 8 },
    PasswordRule("Una letra mayúscula")                { it.any { c -> c.isUpperCase() } },
    PasswordRule("Una letra minúscula")                { it.any { c -> c.isLowerCase() } },
    PasswordRule("Un número")                          { it.any { c -> c.isDigit() } },
    PasswordRule("Un carácter especial ( ! @ # % * )") {
        it.any { c -> c in """!@#%^&*()-_=+[]{}|;:,.<>?""" }
    }
)

@Suppress("unused")
fun isPasswordStrong(password: String) = passwordRules.all { it.isMet(password) }

private fun strengthLevel(password: String): Int = passwordRules.count { it.isMet(password) }

private fun strengthColor(level: Int): Color = when (level) {
    0, 1 -> Color(0xFFD32F2F)
    2    -> Color(0xFFE65100)
    3    -> Color(0xFFF9A825)
    4    -> Color(0xFF43A047)
    else -> Color(0xFF1B5E20)
}

private fun strengthLabel(level: Int): String = when (level) {
    0, 1 -> "Muy débil"
    2    -> "Débil"
    3    -> "Media"
    4    -> "Fuerte"
    else -> "Muy fuerte"
}

// ─── AppTextField ─────────────────────────────────────────────────────────────

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label, fontSize = 14.sp) },
            modifier      = Modifier.fillMaxWidth(),
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            isError         = isError,
            singleLine      = singleLine,
            leadingIcon     = leadingIcon,
            shape           = RoundedCornerShape(14.dp),
            keyboardOptions = keyboardOptions,
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor         = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor       = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor           = MaterialTheme.colorScheme.error,
                focusedLabelColor          = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor        = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor      = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                errorContainerColor        = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        )
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Text(
                text     = errorMessage ?: "",
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ─── PasswordTextField ────────────────────────────────────────────────────────

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    showStrengthIndicator: Boolean = false
) {
    val passwordVisible = remember { mutableStateOf(false) }
    val level           = strengthLevel(value)
    val barColor        = if (value.isNotEmpty()) strengthColor(level)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Column(modifier = modifier) {

        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label, fontSize = 14.sp) },
            modifier      = Modifier.fillMaxWidth(),
            visualTransformation = if (!passwordVisible.value) PasswordVisualTransformation()
            else VisualTransformation.None,
            isError    = isError,
            singleLine = singleLine,
            shape      = RoundedCornerShape(14.dp),
            colors     = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    showStrengthIndicator && value.isNotEmpty() -> barColor
                    else -> MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor        = MaterialTheme.colorScheme.error,
                focusedLabelColor       = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible.value) R.drawable.ic_visibility
                            else R.drawable.ic_visibility_off
                        ),
                        contentDescription = if (passwordVisible.value) "Ocultar" else "Mostrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Text(
                text     = errorMessage ?: "",
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        AnimatedVisibility(
            visible = showStrengthIndicator && value.isNotEmpty(),
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 2.dp, end = 2.dp)
            ) {
                // Barra segmentada
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(5) { index ->
                        val filled = index < level
                        val shape  = when (index) {
                            0    -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                            4    -> RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(shape)
                                .background(
                                    if (filled) barColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text     = "Fortaleza",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text       = strengthLabel(level),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = barColor,
                        fontSize   = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                passwordRules.forEach { rule ->
                    val met = rule.isMet(value)
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (met) R.drawable.ic_check_circle
                                else R.drawable.ic_warning
                            ),
                            contentDescription = null,
                            tint     = if (met) Color(0xFF43A047)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text       = rule.label,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (met) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            fontSize   = 11.5.sp,
                            fontWeight = if (met) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}