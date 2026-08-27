package com.whakaara.core.designsystem.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.whakaara.core.designsystem.WakiMotion

fun NavGraphBuilder.wakiComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { WakiMotion.enterForward() },
        exitTransition = { WakiMotion.exitForward() },
        popEnterTransition = { WakiMotion.enterBack() },
        popExitTransition = { WakiMotion.exitBack() },
        content = content
    )
}

fun NavGraphBuilder.wakiTabComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { WakiMotion.tabEnter() },
        exitTransition = { WakiMotion.tabExit() },
        popEnterTransition = { WakiMotion.tabEnter() },
        popExitTransition = { WakiMotion.tabExit() },
        content = content
    )
}
