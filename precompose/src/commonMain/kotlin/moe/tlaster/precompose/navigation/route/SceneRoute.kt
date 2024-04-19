package moe.tlaster.precompose.navigation.route

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

internal class SceneRoute(
    override val route: String,
    val deepLinks: List<String>,
    val navTransition: NavTransition?,
    val swipeProperties: SwipeProperties?,
    override val content: @Composable AnimatedContentScope.(BackStackEntry) -> Unit,
) : ComposeRoute, ComposeSceneRoute

@Deprecated(
    message = """
    Used as a backwards compatible for the old RouteBuilder APIs which do not expect the content to
    be an extension function on AnimatedContentScope        
    """,
    level = DeprecationLevel.WARNING,
)
internal fun sceneRouteWithoutAnimatedContent(
    route: String,
    deepLinks: List<String>,
    navTransition: NavTransition?,
    swipeProperties: SwipeProperties?,
    content: @Composable (BackStackEntry) -> Unit,
): SceneRoute {
    return SceneRoute(
        route,
        deepLinks,
        navTransition,
        swipeProperties,
        content = { entry -> content(entry) },
    )
}
