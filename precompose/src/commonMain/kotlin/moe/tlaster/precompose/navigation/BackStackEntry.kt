package moe.tlaster.precompose.navigation

import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.navigation.route.GroupRoute
import moe.tlaster.precompose.navigation.route.Route
import moe.tlaster.precompose.navigation.route.toSceneRoute
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder

class BackStackEntry internal constructor(
    internal val stateId: String,
    internal var routeInternal: Route,
    val path: String,
    val pathMap: Map<String, String>,
    private val parentStateHolder: StateHolder,
    parentSavedStateHolder: SavedStateHolder,
    val queryString: QueryString? = null,
) : LifecycleOwner {
    val route: Route
        get() = routeInternal
    internal var uiClosable: UiClosable? = null
    private var _destroyAfterTransition = false
    val stateHolder: StateHolder = parentStateHolder.getOrPut(stateId) {
        StateHolder()
    }
    val savedStateHolder: SavedStateHolder = parentSavedStateHolder.child(stateId)
    internal val swipeProperties: SwipeProperties?
        get() = route.toSceneRoute()?.swipeProperties

    internal val navTransition: NavTransition?
        get() = route.toSceneRoute()?.navTransition

    private val lifecycleRegistry by lazy {
        LifecycleRegistry()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun active() {
        lifecycleRegistry.updateState(Lifecycle.State.Active)
    }

    fun inActive() {
        lifecycleRegistry.updateState(Lifecycle.State.InActive)
        if (_destroyAfterTransition) {
            destroy()
        }
    }

    fun destroy() {
        if (lifecycleRegistry.currentState != Lifecycle.State.InActive) {
            _destroyAfterTransition = true
        } else {
            destroyDirectly()
        }
    }

    internal fun destroyDirectly() {
        lifecycleRegistry.updateState(Lifecycle.State.Destroyed)
        stateHolder.close()
        parentStateHolder.remove(stateId)
        savedStateHolder.close()
        uiClosable?.close(stateId)
    }

    fun hasRoute(route: String): Boolean {
        return this.route.route == route || (this.route as? GroupRoute)?.hasRoute(route) == true
    }
}

internal fun BackStackEntry.hasRoute(route: String, path: String, includePath: Boolean): Boolean {
    return if (includePath) {
        hasRoute(route = route) && this.path == path
    } else {
        hasRoute(route = route)
    }
}

inline fun <reified T> BackStackEntry.path(path: String, default: T? = null): T? {
    val value = pathMap[path] ?: return default
    return convertValue(value)
}

inline fun <reified T> BackStackEntry.query(name: String, default: T? = null): T? {
    return queryString?.query(name, default)
}

inline fun <reified T> BackStackEntry.queryList(name: String): List<T?> {
    val value = queryString?.map?.get(name) ?: return emptyList()
    return value.map { convertValue(it) }
}

inline fun <reified T> convertValue(value: String): T? {
    return when (T::class) {
        Int::class -> value.toIntOrNull()
        Long::class -> value.toLongOrNull()
        String::class -> value
        Boolean::class -> value.toBooleanStrictOrNull()
        Float::class -> value.toFloatOrNull()
        Double::class -> value.toDoubleOrNull()
        else -> throw NotImplementedError()
    } as T
}
