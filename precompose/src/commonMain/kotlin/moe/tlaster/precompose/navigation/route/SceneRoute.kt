package moe.tlaster.precompose.navigation.route

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class SceneRoute(
    override val route: String,
    val deepLinks: List<String>,
    val navTransition: NavTransition?,
    val swipeProperties: SwipeProperties?,
    override val content: @Composable (BackStackEntry) -> Unit,
) : ComposeRoute, ComposeSceneRoute
