package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class SceneRoute(
    route: String,
    val navTransition: NavTransition?,
    content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute(route, content)
