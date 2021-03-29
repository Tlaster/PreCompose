package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Creates a [Navigator] that controls the [NavHost].
 *
 * @see NavHost
 */
@Composable
fun rememberNavigator(): Navigator {
    return remember { Navigator() }
}

class Navigator {
    internal lateinit var stackManager: RouteStackManager

    /**
     * Navigate to a route in the current RouteGraph.
     *
     * @param route route for the destination
     */
    fun navigate(route: String, option: NavOption? = null) {
        stackManager.navigate(route, option)
    }

    /**
     * Attempts to navigate up in the navigation hierarchy. Suitable for when the
     * user presses the "Up" button marked with a left (or start)-facing arrow in the upper left
     * (or starting) corner of the app UI.
     */
    fun goBack() {
        stackManager.goBack()
    }

    /**
     * Check if navigator can navigate up
     */
    val canGoBack: Boolean
        get() = stackManager.canGoBack
}

data class NavOption(
    val launchSingleTop: Boolean = false,
    val popUpTo: PopUpTo? = null,
)

data class PopUpTo(
    val route: String,
    val inclusive: Boolean = false
)
