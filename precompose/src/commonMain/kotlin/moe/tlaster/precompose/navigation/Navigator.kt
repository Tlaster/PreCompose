package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.map

/**
 * Creates or returns an existing [Navigator] that controls the [NavHost].
 * @param name: Identify the navigator so you can have as many navigator instances as you need.
 * @return Returns an instance of Navigator.
 */
@Composable
fun rememberNavigator(key: String? = null): Navigator {
    val viewModel = viewModel<NavigatorViewModel>(key = key)
    return viewModel.navigator
}

internal class NavigatorViewModel : ViewModel() {
    val navigator by lazy {
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
     * @param lifecycleOwner: lifecycleOwner object
     * @param viewModelStoreOwner: viewModelStoreOwner object
     */
    internal fun init(
        lifecycleOwner: LifecycleOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
    ) {
        if (_initialized) {
            return
        }
        _initialized = true
        stackManager.init(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = viewModelStoreOwner,
        )
    }

    /**
     * Set the RouteGraph for the navigator.
     * @param routeGraph: destination's navigation graph
     */
    internal fun setRouteGraph(routeGraph: RouteGraph) {
        stackManager.setRouteGraph(routeGraph)
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
     */
    fun goBack(
        popUpTo: PopUpTo,
    ) {
        if (!_initialized) {
            return
        }
        stackManager.popWithOptions(popUpTo)
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
     * Previous route
     * @ return Returns the previous navigation back stack entry.
     */
    val previousEntry = stackManager.prevBackStackEntry

    /**
     * Number of routes in the back stack
     */
    val backStackCount = stackManager.backStacks.map { it.size }
}
