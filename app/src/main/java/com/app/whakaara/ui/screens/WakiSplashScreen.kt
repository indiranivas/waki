package com.app.whakaara.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.app.whakaara.R
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WakiSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val scale = remember { Animatable(0.9f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo scale and fade in
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
        logoAlpha.animateTo(1f, tween(600))

        // Step 2: Text fade in
        delay(200)
        textAlpha.animateTo(1f, tween(600))

        // Step 3: Tagline fade in
        delay(200)
        taglineAlpha.animateTo(1f, tween(600))

        // Final delay to ensure splash is visible for ~1.5s
        delay(500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6A3D), // Primary orange (matched with colors.xml)
                        Color(0xFFFF8E5B)  // Secondary orange
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Abstract rounded shapes in background (subtle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFFFC9B3).copy(alpha = 0.15f),
                radius = 400f,
                center = center.copy(x = center.x - 200f, y = center.y - 800f)
            )
            drawCircle(
                color = Color(0xFFFFC9B3).copy(alpha = 0.1f),
                radius = 600f,
                center = center.copy(x = center.x + 400f, y = center.y + 1000f)
            )
        }

        // Ripple Effect around logo
        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(
                color = Color.White.copy(alpha = rippleAlpha),
                radius = size.minDimension / 2 * rippleScale,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waki Logo (Stylized)
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_waki_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Waki",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Smart alarms.\nRight on time.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }

        // Bottom Loading Indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .width(120.dp)
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
