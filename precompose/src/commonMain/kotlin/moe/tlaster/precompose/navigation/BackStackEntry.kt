package moe.tlaster.precompose.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStoreOwner
import moe.tlaster.precompose.navigation.route.GroupRoute
import moe.tlaster.precompose.navigation.route.Route
import moe.tlaster.precompose.navigation.route.toSceneRoute
import moe.tlaster.precompose.navigation.transition.NavTransition

class BackStackEntry internal constructor(
    internal val stateId: String,
    internal var routeInternal: Route,
    val path: String,
    val pathMap: Map<String, String>,
    private val provider: ViewModelStoreProvider,
    val queryString: QueryString? = null,
) : LifecycleOwner,
    ViewModelStoreOwner {
    val route: Route
        get() = routeInternal
    internal var uiClosable: UiClosable? = null
    private var _destroyAfterTransition = false
    internal val swipeProperties: SwipeProperties?
        get() = route.toSceneRoute()?.swipeProperties

    internal val navTransition: NavTransition?
        get() = route.toSceneRoute()?.navTransition

    private val lifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    init {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun active() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun inActive() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        if (_destroyAfterTransition) {
            destroy()
        }
    }

    fun destroy() {
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            _destroyAfterTransition = true
        } else {
            destroyDirectly()
        }
    }

    internal fun destroyDirectly() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        provider.clear(stateId)
        uiClosable?.close(stateId)
    }

    fun hasRoute(route: String): Boolean {
        return this.route.route == route || (this.route as? GroupRoute)?.hasRoute(route) == true
    }

    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                destroy()
            }
            else -> {
                lifecycleRegistry.handleLifecycleEvent(event)
            }
        }
    }

    override val viewModelStore by lazy {
        provider.getViewModelStore(stateId)
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
