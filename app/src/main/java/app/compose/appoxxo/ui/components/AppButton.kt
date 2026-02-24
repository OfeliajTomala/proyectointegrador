package app.compose.appoxxo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val animatedColor by animateColorAsState(
        targetValue = if (enabled && !isLoading) containerColor
        else containerColor.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonColor"
    )

    Button(
        onClick   = onClick,
        modifier  = modifier.height(54.dp),
        enabled   = enabled && !isLoading,
        shape     = RoundedCornerShape(16.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor         = animatedColor,
            contentColor           = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor   = contentColor.copy(alpha = 0.45f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
            hoveredElevation  = 2.dp
        ),
        contentPadding = PaddingValues(horizontal = 28.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                color       = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                leadingIcon?.let {
                    it()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text          = text,
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 15.sp,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(54.dp),
        enabled  = enabled,
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) color.copy(alpha = 0.5f) else color.copy(alpha = 0.18f)
        )
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text       = text,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
        }
    }
}