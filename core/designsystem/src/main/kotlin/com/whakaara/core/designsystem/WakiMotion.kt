package com.whakaara.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset

object WakiMotion {
    val gentleSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    val snappySpring = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 520f
    )

    val softSpring = spring<Float>(
        dampingRatio = 1f,
        stiffness = 300f
    )

    val offsetSpring = spring<IntOffset>(
        dampingRatio = 0.86f,
        stiffness = 420f
    )

    fun enterForward(): androidx.compose.animation.EnterTransition =
        fadeIn(gentleSpring) + slideInHorizontally(offsetSpring) { fullWidth -> fullWidth / 5 }

    fun exitForward(): androidx.compose.animation.ExitTransition =
        fadeOut(softSpring) + slideOutHorizontally(offsetSpring) { fullWidth -> -fullWidth / 5 }

    fun enterBack(): androidx.compose.animation.EnterTransition =
        fadeIn(gentleSpring) + slideInHorizontally(offsetSpring) { fullWidth -> -fullWidth / 5 }

    fun exitBack(): androidx.compose.animation.ExitTransition =
        fadeOut(softSpring) + slideOutHorizontally(offsetSpring) { fullWidth -> fullWidth / 5 }

    fun tabEnter(): androidx.compose.animation.EnterTransition =
        fadeIn(gentleSpring) + scaleIn(initialScale = 0.94f, animationSpec = gentleSpring)

    fun tabExit(): androidx.compose.animation.ExitTransition =
        fadeOut(softSpring) + scaleOut(targetScale = 1.04f, animationSpec = softSpring)

    fun listItemEnter(): androidx.compose.animation.EnterTransition =
        fadeIn(gentleSpring) + slideInVertically(offsetSpring) { height -> height / 4 }

    fun listItemExit(): androidx.compose.animation.ExitTransition =
        fadeOut(softSpring) + slideOutVertically(offsetSpring) { height -> -height / 4 }
}

fun Modifier.wakiPressScale(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = WakiMotion.gentleSpring,
        label = "wakiPressScale"
    )
    scale(scale)
}

@Composable
fun WakiAnimatedListItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        modifier = modifier,
        enter = WakiMotion.listItemEnter(),
        exit = WakiMotion.listItemExit()
    ) {
        content()
    }
}
