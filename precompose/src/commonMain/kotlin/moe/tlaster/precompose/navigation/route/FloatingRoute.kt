package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class FloatingRoute(
    route: String,
    navTransition: NavTransition?,
    content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute(route, navTransition, content)
