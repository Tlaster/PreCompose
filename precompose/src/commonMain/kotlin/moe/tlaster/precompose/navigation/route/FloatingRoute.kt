package moe.tlaster.precompose.navigation.route

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry

internal class FloatingRoute(
    override val content: @Composable AnimatedContentScope.(BackStackEntry) -> Unit,
    override val route: String,
) : ComposeRoute, ComposeFloatingRoute
