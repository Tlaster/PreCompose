package moe.tlaster.precompose.navigation.route

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry

internal class FloatingRoute(
    override val content: @Composable AnimatedContentScope.(BackStackEntry) -> Unit,
    override val route: String,
) : ComposeRoute, ComposeFloatingRoute

@Deprecated(
    message = """
    Used as a backwards compatible for the old RouteBuilder APIs which do not expect the content to
    be an extension function on AnimatedContentScope        
    """,
    level = DeprecationLevel.WARNING,
)
internal fun floatingRouteWithoutAnimatedContent(
    route: String,
    content: @Composable (BackStackEntry) -> Unit,
): FloatingRoute {
    return FloatingRoute(
        route = route,
        content = { entry -> content(entry) },
    )
}
