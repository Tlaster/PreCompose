package moe.tlaster.precompose.navigation.route

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry

interface ComposeRoute : Route {
    val content: @Composable AnimatedContentScope.(BackStackEntry) -> Unit
}

interface ComposeSceneRoute : ComposeRoute

interface ComposeFloatingRoute : ComposeRoute
