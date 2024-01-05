package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder

/**
 * Creates or returns an existing [Navigator] that controls the [NavHost].
 * @param name: Identify the navigator so you can have as many navigator instances as you need.
 * @return Returns an instance of Navigator.
 */
@Composable
fun rememberNavigator(name: String = ""): Navigator {
    val stateHolder = LocalStateHolder.current
    return stateHolder.getOrPut("${name}Navigator") {
        Navigator()
    }
}

class Navigator {
    // FIXME: 2021/4/1 Temp workaround for deeplink
    private var _pendingNavigation: String? = null
    private var _initialized = false
    internal val stackManager = BackStackManager()

    /**
     * Initializes the navigator with a set parameters.
     * @param routeGraph: destination's navigation graph
     * @param stateHolder: stateHolder object
     * @param savedStateHolder: savedStateHolder object
     * @param lifecycleOwner: lifecycleOwner object
     * @param persistNavState: if true, navigation state will persist across configuration changes
     */
    internal fun init(
        routeGraph: RouteGraph,
        stateHolder: StateHolder,
        savedStateHolder: SavedStateHolder,
        lifecycleOwner: LifecycleOwner,
        persistNavState: Boolean,
    ) {
        if (_initialized) {
            return
        }
        _initialized = true
        stackManager.init(
            routeGraph = routeGraph,
            stateHolder = stateHolder,
            savedStateHolder = savedStateHolder,
            lifecycleOwner = lifecycleOwner,
            persistNavState = persistNavState,
        )
        _pendingNavigation?.let {
            stackManager.push(it)
            _pendingNavigation = null
        }
    }

    /**
     * Navigate to a route in the current RouteGraph.
     * @param route: route for the destination
     * @param options: navigation options for the destination
     */
    fun navigate(route: String, options: NavOptions? = null) {
        if (!_initialized) {
            _pendingNavigation = route
            return
        }

        stackManager.push(route, options)
    }

    /**
     * Navigate to a route in the current RouteGraph and wait for result.
     * @param route: route for the destination
     * @param options: navigation options for the destination
     * @return: result from the destination
     */
    suspend fun navigateForResult(route: String, options: NavOptions? = null): Any? {
        if (!_initialized) {
            _pendingNavigation = route
            return null
        }
        return stackManager.pushForResult(route, options)
    }

    /**
     * Attempts to navigate up in the navigation hierarchy.
     */
    fun goBack() {
        if (!_initialized) {
            return
        }
        stackManager.pop()
    }

    /**
     * Go back with a specific result.
     * @param result: result to be returned when moved back.
     */
    fun goBackWith(result: Any? = null) {
        if (!_initialized) {
            return
        }
        stackManager.pop(result)
    }

    /**
     * Go back to a specific destination.
     * @param popUpTo: the destination to pop back to.
     * @param inclusive: includes the destination to pop back to in the back stack.
     */
    fun goBack(
        popUpTo: PopUpTo,
        inclusive: Boolean,
    ) {
        if (!_initialized) {
            return
        }
        stackManager.popWithOptions(popUpTo, inclusive)
    }

    /**
     * Compatibility layer for Jetpack Navigation.
     */
    fun popBackStack() {
        if (!_initialized) {
            return
        }
        goBack()
    }

    /**
     * Check if navigator can navigate up
     * @return Returns true if navigator can navigate up, false otherwise.
     */
    val canGoBack = stackManager.canGoBack

    /**
     * Current route
     * @ return Returns the current navigation back stack entry.
     */
    val currentEntry = stackManager.currentBackStackEntry

    /**
     * Check if navigator can navigate, it will be false when performing navigation animation.
     * @return Returns true if navigator can perform navigation, false otherwise.
     */
    val canNavigate = snapshotFlow { stackManager.canNavigate }
}
