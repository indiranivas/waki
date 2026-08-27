package com.whakaara.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whakaara.core.designsystem.theme.LocalWakiDarkTheme

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    surfaceAlpha: Float = 0.78f,
    borderAlpha: Float = 0.55f,
    elevation: Dp = 10.dp
): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalWakiDarkTheme.current
    val surfaceColor = colorScheme.surface
    val borderColor = colorScheme.outlineVariant

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.06f),
            spotColor = Color.Black.copy(alpha = if (isDark) 0.25f else 0.10f)
        )
        .clip(shape)
        .background(surfaceColor.copy(alpha = surfaceAlpha))
        .border(
            width = 1.dp,
            color = borderColor.copy(alpha = borderAlpha),
            shape = shape
        )
}

@Composable
fun WakiScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalWakiDarkTheme.current

    val gradient = Brush.verticalGradient(
        colors = listOf(
            colorScheme.background,
            colorScheme.surfaceContainerLow,
            if (isDark) colorScheme.background else colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = if (isDark) 0.10f else 0.07f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )
        content()
    }
}
